package wardlights.bauble.chest;

import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wardlights.Wardlights;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.ChestBaubleConfig;

/**
 * The event-driven half of the chest bauble bonuses (see {@link ItemChestBauble}):
 * <ul>
 * <li>The bauble's own Resistance, kept topped up while worn.</li>
 * <li>Every hit of at least 1 damage counts; once more than "Required Hits"
 * have counted, the next one is ignored and the counter resets. Indirect hits
 * (projectiles, explosions, magic) are stopped outright in
 * {@link LivingAttackEvent}; direct ones are zeroed in {@link LivingHurtEvent}.</li>
 * <li>Explosion damage is multiplied down.</li>
 * <li>Resistance from other sources adds its level on top of the bauble's, up to
 * a cap.</li>
 * </ul>
 * The hit counter lives in the worn stack's NBT, so it persists and the
 * tooltip can show it.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public final class ChestBaubleHandler {

    private static final String HIT_COUNT_TAG = "HitCount";

    /** Re-applied whenever it lapses, so a short duration keeps removal snappy. */
    private static final int RESISTANCE_DURATION = 200;

    private ChestBaubleHandler() {
    }

    /** From the bauble's onWornTick: keeps its Resistance up. -1 means it grants none. */
    public static void updateResistance(EntityLivingBase wearer, int amplifier) {
        if (wearer.world.isRemote || amplifier < 0) {
            return;
        }
        // Topped up only when missing or weaker, so a stacked (higher) level
        // from onPotionApplicable isn't knocked back down.
        PotionEffect active = wearer.getActivePotionEffect(MobEffects.RESISTANCE);
        if (active == null || active.getAmplifier() < amplifier) {
            wearer.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, RESISTANCE_DURATION, amplifier, false, false));
        }
    }

    /**
     * From the bauble's onUnequipped. Any other Resistance source (e.g. a ring)
     * simply re-applies itself on its next tick.
     */
    public static void removeResistance(EntityLivingBase wearer) {
        if (!wearer.world.isRemote) {
            wearer.removePotionEffect(MobEffects.RESISTANCE);
        }
    }

    /** The chest bauble worn by the entity, or {@link ItemStack#EMPTY}. Baubles are player-only. */
    public static ItemStack getWornChestBauble(EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) {
            return ItemStack.EMPTY;
        }
        IBaublesItemHandler baubles = BaublesApi.getBaublesHandler((EntityPlayer) entity);
        for (int slot = 0; slot < baubles.getSlots(); slot++) {
            ItemStack stack = baubles.getStackInSlot(slot);
            if (stack.getItem() instanceof ItemChestBauble) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public static int getHitCount(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getInteger(HIT_COUNT_TAG) : 0;
    }

    private static void setHitCount(ItemStack stack, int hits) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        stack.getTagCompound().setInteger(HIT_COUNT_TAG, hits);
    }

    /** Damage not dealt by a direct melee hit. */
    private static boolean isIndirect(DamageSource source) {
        return source instanceof EntityDamageSourceIndirect || source.isExplosion() || source.isMagicDamage()
                || source.isProjectile();
    }

    /** Counts a qualifying hit; returns true if this is the hit that gets ignored. */
    private static boolean countHitAndCheckIgnored(EntityLivingBase wearer, ItemStack bauble) {
        ChestBaubleConfig config = WardlightsConfig.server.chestBaubles;
        int hits = getHitCount(bauble) + 1;
        if (hits <= config.requiredHits) {
            setHitCount(bauble, hits);
            return false;
        }
        setHitCount(bauble, 0);
        if (config.ignoredHitFeedback) {
            showFeedback(wearer, "message.wardlights.chest_bauble.ignored_hit");
        }
        return true;
    }

    /** Action bar message + shield-block sound when a bauble stops a hit. */
    public static void showFeedback(EntityLivingBase wearer, String messageKey) {
        if (wearer instanceof EntityPlayer) {
            ((EntityPlayer) wearer).sendStatusMessage(new TextComponentTranslation(messageKey), true);
        }
        wearer.world.playSound(null, wearer.posX, wearer.posY, wearer.posZ, SoundEvents.ITEM_SHIELD_BLOCK,
                SoundCategory.PLAYERS, 0.8F, 1.0F);
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        DamageSource source = event.getSource();
        if (entity.world.isRemote || source.canHarmInCreative() || event.getAmount() < 1 || !isIndirect(source)
                || !WardlightsConfig.server.chestBaubles.ignoreDamage) {
            return;
        }
        ItemStack bauble = getWornChestBauble(entity);
        if (!bauble.isEmpty() && countHitAndCheckIgnored(entity, bauble)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        DamageSource source = event.getSource();
        if (entity.world.isRemote || source.canHarmInCreative() || event.getAmount() == 0) {
            return;
        }
        ItemStack bauble = getWornChestBauble(entity);
        if (bauble.isEmpty()) {
            return;
        }
        ChestBaubleConfig config = WardlightsConfig.server.chestBaubles;
        float damage = event.getAmount();
        if (config.ignoreDamage && damage >= 1 && !isIndirect(source) && countHitAndCheckIgnored(entity, bauble)) {
            damage = 0;
        }
        if (config.explosionResistance && source.isExplosion() && damage >= 1) {
            damage *= (float) config.explosionDamageMultiplier;
        }
        if (damage <= 0) {
            event.setCanceled(true);
        } else {
            event.setAmount(damage);
        }
    }

    @SubscribeEvent
    public static void onPotionApplicable(PotionEvent.PotionApplicableEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        PotionEffect incoming = event.getPotionEffect();
        ChestBaubleConfig config = WardlightsConfig.server.chestBaubles;
        if (entity.world.isRemote || incoming.getPotion() != MobEffects.RESISTANCE || !config.resistanceStacks) {
            return;
        }
        PotionEffect active = entity.getActivePotionEffect(MobEffects.RESISTANCE);
        ItemStack bauble = getWornChestBauble(entity);
        if (active == null || bauble.isEmpty()) {
            return;
        }
        // Amplifiers are level - 1, hence the +1 when adding two of them.
        int baubleAmplifier = ((ItemChestBauble) bauble.getItem()).getResistanceAmplifier();
        int stacked = Math.min(baubleAmplifier + incoming.getAmplifier() + 1, config.maxStackedResistanceLevel - 1);
        if (active.getAmplifier() > stacked) {
            // Already stronger than this would make it; don't let it downgrade.
            event.setResult(Event.Result.DENY);
            return;
        }
        incoming.combine(new PotionEffect(MobEffects.RESISTANCE, incoming.getDuration(), stacked, false, false));
    }
}
