package wardlights.handlers;

import java.util.IdentityHashMap;
import java.util.Map;

import baubles.api.BaublesApi;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wardlights.Wardlights;
import wardlights.handlers.ForgeConfigHandler.ServerConfig.BaubleConfig;
import wardlights.items.ItemDispatcherCrown;

/**
 * The Dispatcher Crown's adaptation: every time the wearer is hurt by
 * something, they take a little less damage from that same thing next time,
 * up to a cap (5% by default). "The same thing" is the attacking mob's type
 * (every Sim Human counts as one), or the damage type for damage without an
 * attacker (fall, fire, drowning...).
 *
 * <p>The reduction adds onto other reductions instead of multiplying with
 * them: it's a share of the hit's original damage, taken off after everything
 * else has had its turn. So 40% from SRParasites' Living Armor adaptation plus
 * the crown's 5% is 45%. That needs two handlers: the first one (HIGHEST
 * priority) writes down the damage before anyone changes it, the second
 * (LOWEST) takes the crown's share of that off whatever is left.
 */
@Mod.EventBusSubscriber(modid = Wardlights.MODID)
public class DispatcherCrownHandler {

    /**
     * Original damage of hits in progress on crown wearers. Keyed by the event
     * itself, since another hurt event can start while one is being handled
     * (e.g. thorns); the server runs on one thread, so a plain map is fine.
     */
    private static final Map<LivingHurtEvent, Float> ORIGINAL_DAMAGE = new IdentityHashMap<>();

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void recordOriginalDamage(LivingHurtEvent event) {
        if (getWornCrown(event) != null) {
            ORIGINAL_DAMAGE.put(event, event.getAmount());
        }
    }

    // receiveCanceled so the entry is always removed, even if another mod cancels the hit.
    @SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
    public static void applyAdaptation(LivingHurtEvent event) {
        Float original = ORIGINAL_DAMAGE.remove(event);
        ItemStack crown = getWornCrown(event);
        if (original == null || crown == null || event.isCanceled()) {
            return;
        }
        NBTTagCompound tag = ItemDispatcherCrown.getOrCreateTag(crown);
        NBTTagCompound adaptation = tag.getCompoundTag(ItemDispatcherCrown.ADAPTATION_TAG);

        // Hurt first with what's been learned so far, then learn from this hit.
        String key = adaptationKey(event.getSource());
        float reduction = adaptation.getFloat(key);
        event.setAmount(Math.max(0, event.getAmount() - original * reduction));

        BaubleConfig config = ForgeConfigHandler.server.baubles;
        float max = (float) config.dispatcherCrownMaxAdaptation;
        adaptation.setFloat(key, Math.min(max, reduction + (float) config.dispatcherCrownAdaptationStep));
        tag.setTag(ItemDispatcherCrown.ADAPTATION_TAG, adaptation);
    }

    /** The crown worn by the hurt player, or null when this hit isn't one the crown adapts to. */
    private static ItemStack getWornCrown(LivingHurtEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer) || ModRegistry.DISPATCHER_CROWN == null) {
            return null;
        }
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        // canHarmInCreative is only true for the void and /kill, which shouldn't be adapted to.
        if (player.world.isRemote || ForgeConfigHandler.server.baubles.dispatcherCrownMaxAdaptation <= 0
                || event.getSource().canHarmInCreative()) {
            return null;
        }
        int slot = BaublesApi.isBaubleEquipped(player, ModRegistry.DISPATCHER_CROWN);
        return slot < 0 ? null : BaublesApi.getBaublesHandler(player).getStackInSlot(slot);
    }

    private static String adaptationKey(DamageSource source) {
        Entity attacker = source.getTrueSource();
        if (attacker instanceof EntityPlayer) {
            return "player";
        }
        if (attacker != null) {
            ResourceLocation id = EntityList.getKey(attacker);
            if (id != null) {
                return id.toString();
            }
        }
        return source.getDamageType();
    }
}
