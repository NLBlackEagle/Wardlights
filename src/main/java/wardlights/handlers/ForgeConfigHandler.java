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

		@Config.Comment("Item registry names (e.g. \"minecraft:diamond_chestplate\") of chest armor that hides the "
				+ "Aegis of Sacrifice shoulder model while worn, for armor too bulky for it to fit over. * is a "
				+ "wildcard (e.g. \"iceandfire:armor_*_chestplate\"). The chest badge still renders.")
		@Config.Name("Armor Hiding Bauble Shoulders")
		public String[] shoulderHidingArmor = {};

		@Config.Comment("Aegis of Sacrifice chest badge placement, applied on top of the base hand-anchored "
				+ "transform: {offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ, scale}. Offsets are in "
				+ "blocks, rotations in degrees, scale is a multiplier. Live-tunable from the in-game config GUI "
				+ "(or by editing this file + F3+T).")
		@Config.Name("Aegis of Sacrifice Badge Placement")
		public String aegisBadgePlacement = "{0, 3, -0.1, -90, -90, -180, 1}";

		@Config.Comment("Aegis of Sacrifice shoulder placement, same format as the badge placement.")
		@Config.Name("Aegis of Sacrifice Shoulder Placement")
		public String aegisShoulderPlacement = "{-0.05, -0.12, 0, -90, -90, -180, 1}";

		@Config.Comment(ARMOR_PLACEMENT_COMMENT)
		@Config.Name("Aegis of Sacrifice Badge Armor Placement")
		public String[] aegisBadgeArmorPlacement = { "{default, 0, 0, -0.35, 1}" };

		@Config.Comment(ARMOR_PLACEMENT_COMMENT)
		@Config.Name("Aegis of Sacrifice Shoulder Armor Placement")
		public String[] aegisShoulderArmorPlacement = { "{default, 0, -0.1, 0, 1.15}" };

		@Config.Comment("Bulwark of Valor chest badge placement, applied on top of the base hand-anchored "
				+ "transform: {offsetX, offsetY, offsetZ, rotationX, rotationY, rotationZ, scale}. Offsets are in "
				+ "blocks, rotations in degrees, scale is a multiplier. Live-tunable from the in-game config GUI "
				+ "(or by editing this file + F3+T).")
		@Config.Name("Bulwark of Valor Badge Placement")
		public String bulwarkBadgePlacement = "{0, 3, -0.1, -90, -90, -180, 1}";

		@Config.Comment(ARMOR_PLACEMENT_COMMENT)
		@Config.Name("Bulwark of Valor Badge Armor Placement")
		public String[] bulwarkBadgeArmorPlacement = { "{default, 0, 0, -0.2, 1}" };

		private static final String ARMOR_PLACEMENT_COMMENT = "Extra adjustment while chest armor is worn, so the "
				+ "part sits on the armor instead of inside it, one entry per line: {type, offsetX, offsetY, "
				+ "offsetZ, scale}. Offsets add to the placement above, scale multiplies it. type is either "
				+ "\"default\" (any chest armor not listed) or a chest armor registry id for a custom fit, where * "
				+ "is a wildcard (e.g. {iceandfire:armor_*_chestplate, 0, 0, -0.4, 1.1}). The first matching "
				+ "entry wins, so put specific ids above broad wildcards.";
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
