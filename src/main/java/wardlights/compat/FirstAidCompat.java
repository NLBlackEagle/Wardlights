package wardlights.compat;

import ichttt.mods.firstaid.api.damagesystem.AbstractPlayerDamageModel;
import ichttt.mods.firstaid.api.event.FirstAidLivingDamageEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wardlights.bauble.chest.ChestBaubleHandler;
import wardlights.config.WardlightsConfig;
import wardlights.config.WardlightsConfig.ServerConfig.ChestBaubleConfig;

/**
 * Chest bauble headshot save: when a hit would take the head to 0 (which
 * kills in First Aid) while the body survives, there's a 1 in N chance the
 * head damage is undone.
 *
 * <p>Only registered on the event bus when First Aid is loaded (see
 * {@link wardlights.Wardlights#preInit}), since this class references First
 * Aid's API and would crash on class load without it.
 */
public class FirstAidCompat {

    public static final String MODID = "firstaid";

    @SubscribeEvent
    public void onFirstAidDamage(FirstAidLivingDamageEvent event) {
        EntityPlayer player = event.getEntityPlayer();
        ChestBaubleConfig config = WardlightsConfig.server.chestBaubles;
        if (player.world.isRemote || !config.headshotSave || ChestBaubleHandler.getWornChestBauble(player).isEmpty()) {
            return;
        }
        AbstractPlayerDamageModel before = event.getBeforeDamage();
        AbstractPlayerDamageModel after = event.getAfterDamage();
        if (after.HEAD.currentHealth < 1 && after.BODY.currentHealth > 0
                && player.getRNG().nextInt(config.headshotSaveChance) == 0) {
            after.HEAD.currentHealth = before.HEAD.currentHealth;
            after.scheduleResync();
            event.setCanceled(true);
            ChestBaubleHandler.showFeedback(player, "message.wardlights.chest_bauble.headshot_saved");
        }
    }
}
