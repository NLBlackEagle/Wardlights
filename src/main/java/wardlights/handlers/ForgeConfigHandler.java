package wardlights.handlers;

import fermiumbooter.annotations.MixinConfig;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wardlights.Wardlights;

@Config(modid = Wardlights.MODID)
public class ForgeConfigHandler {

	@Config.Comment("Server-Side Options")
	@Config.Name("Server Options")
	public static final ServerConfig server = new ServerConfig();

	@Config.Comment("Client-Side Options")
	@Config.Name("Client Options")
	public static final ClientConfig client = new ClientConfig();

	@Config.Comment("On/off switches for the code patches (mixins) this mod applies to other mods. Changes require a game restart.")
	@Config.Name("Mixin Toggles")
	public static final MixinToggleConfig mixins = new MixinToggleConfig();

	public static class ServerConfig {

		@Config.Comment("Item registry names (e.g. \"minecraft:iron_ingot\") for the 9 Aegis of Sacrifice crafting "
				+ "grid slots, read left-to-right then top-to-bottom (1=top-left, 9=bottom-right). Leave a slot "
				+ "blank for an empty grid cell. Unknown/unresolvable item ids are treated as blank (see log). "
				+ "Defaults below are placeholders only — replace with real ingredients.")
		@Config.Name("Aegis of Sacrifice Recipe")
		@Config.RequiresMcRestart
		public String[] aegisOfSacrificeRecipe = {
				"minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:iron_ingot",
				"minecraft:gold_ingot", "minecraft:diamond", "minecraft:gold_ingot",
				"minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:iron_ingot"
		};
	}

	public static class ClientConfig {

		@Config.Comment("Distance (in blocks) from the player beyond which placed lanterns stop animating, to save performance. 0 disables the cutoff (always animate).")
		@Config.Name("Lantern Animation Distance")
		@Config.RangeInt(min = 0, max = 128)
		public int lanternAnimationDistance = 16;

		@Config.Comment("Live-tunable offsets for the Aegis of Sacrifice bauble parts, applied on top of the "
				+ "base hand-anchored transform, so position/rotation/scale can be dialed in from the in-game "
				+ "config GUI (or by editing this file + F3+T) without rebuilding the mod. Translation is in "
				+ "blocks, rotation in degrees.")
		@Config.Name("Aegis of Sacrifice Placement")
		public BaublePlacementConfig aegisPlacement = new BaublePlacementConfig();

		@Config.Comment("Live-tunable offsets for the Bulwark of Valor bauble parts, applied on top of the "
				+ "base hand-anchored transform, so position/rotation/scale can be dialed in from the in-game "
				+ "config GUI (or by editing this file + F3+T) without rebuilding the mod. Translation is in "
				+ "blocks, rotation in degrees.")
		@Config.Name("Bulwark of Valor Placement")
		public BaublePlacementConfig bulwarkPlacement = new BaublePlacementConfig();

		public static class BaublePlacementConfig {

			@Config.Name("Badge Offset X")
			@Config.RangeDouble(min = -5, max = 5)
			public double badgeOffsetX = 0;
			@Config.Name("Badge Offset Y")
			@Config.RangeDouble(min = -5, max = 5)
			public double badgeOffsetY = 5;
			@Config.Name("Badge Offset Z")
			@Config.RangeDouble(min = -5, max = 5)
			public double badgeOffsetZ = 0;
			@Config.Name("Badge Rotation X")
			@Config.RangeDouble(min = -180, max = 180)
			public double badgeRotationX = -90;
			@Config.Name("Badge Rotation Y")
			@Config.RangeDouble(min = -180, max = 180)
			public double badgeRotationY = -90;
			@Config.Name("Badge Rotation Z")
			@Config.RangeDouble(min = -180, max = 180)
			public double badgeRotationZ = -180;
			@Config.Name("Badge Scale Multiplier")
			@Config.RangeDouble(min = 0.1, max = 5)
			public double badgeScale = 1;

			@Config.Name("Shoulder Offset X")
			@Config.RangeDouble(min = -5, max = 5)
			public double shoulderOffsetX = -0.05;
			@Config.Name("Shoulder Offset Y")
			@Config.RangeDouble(min = -5, max = 5)
			public double shoulderOffsetY = -0.09;
			@Config.Name("Shoulder Offset Z")
			@Config.RangeDouble(min = -5, max = 5)
			public double shoulderOffsetZ = 0;
			@Config.Name("Shoulder Rotation X")
			@Config.RangeDouble(min = -180, max = 180)
			public double shoulderRotationX = -90;
			@Config.Name("Shoulder Rotation Y")
			@Config.RangeDouble(min = -180, max = 180)
			public double shoulderRotationY = -90;
			@Config.Name("Shoulder Rotation Z")
			@Config.RangeDouble(min = -180, max = 180)
			public double shoulderRotationZ = -180;
			@Config.Name("Shoulder Scale Multiplier")
			@Config.RangeDouble(min = 0.1, max = 5)
			public double shoulderScale = 1;
		}
	}

	/**
	 * FermiumBooter scans this class (by the {@link MixinConfig} annotation) at coremod load time,
	 * reads each toggle's value straight out of {@code config/wardlights.cfg}, runs the
	 * {@link MixinConfig.CompatHandling} mod-presence checks, and only then enqueues the matching
	 * {@code mixins.wardlights.*.json}. All three integrations here target other mods, so each is
	 * gated on that mod being installed and applied as a {@code lateMixin} (after mod discovery).
	 */
	@MixinConfig(name = Wardlights.MODID)
	public static class MixinToggleConfig {

		@Config.Comment("Enable Wardlights to integrate LycanitesMobs allowing for the Lycan Lantern to be crafted.")
		@Config.Name("Lycanites Mobs Integration")
		@Config.RequiresMcRestart
		@MixinConfig.MixinToggle(lateMixin = "mixins.wardlights.lycanitesmobs.json", defaultValue = true)
		@MixinConfig.CompatHandling(modid = "lycanitesmobs", desired = true)
		public boolean lycanitesMobsIntegration = true;

		@Config.Comment("Enable Wardlights to integrate Pale Bloom allowing for the Pale Lantern to be crafted.")
		@Config.Name("Pale Bloom Integration")
		@Config.RequiresMcRestart
		@MixinConfig.MixinToggle(lateMixin = "mixins.wardlights.palebloom.json", defaultValue = true)
		@MixinConfig.CompatHandling(modid = "palebloom", desired = true)
		public boolean paleBloomIntegration = true;

		@Config.Comment("Enable Wardlights to integrate Scape and Run: Parasites allowing for the Parasitic Lantern to be crafted.")
		@Config.Name("Scape and Run Parasites Integration")
		@Config.RequiresMcRestart
		@MixinConfig.MixinToggle(lateMixin = "mixins.wardlights.srparasites.json", defaultValue = true)
		@MixinConfig.CompatHandling(modid = "srparasites", desired = true, warnIngame = false)
		public boolean srParasitesIntegration = true;
	}

	@Mod.EventBusSubscriber(modid = Wardlights.MODID)
	private static class EventHandler{

		@SubscribeEvent
		public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
			if(event.getModID().equals(Wardlights.MODID)) {
				ConfigManager.sync(Wardlights.MODID, Config.Type.INSTANCE);
			}
		}
	}
}
