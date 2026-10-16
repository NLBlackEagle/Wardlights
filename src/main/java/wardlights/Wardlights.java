package wardlights;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;
import wardlights.compat.ArtifactsCompat;
import wardlights.compat.FirstAidCompat;
import wardlights.compat.NuclearCraftCompat;
import wardlights.handlers.ModRegistry;
import wardlights.proxy.CommonProxy;

@Mod(modid = Wardlights.MODID, version = Wardlights.VERSION, name = Wardlights.NAME, dependencies = "required-after:fermiumbooter;required-after:geckolib3;required-after:baubles")
public class Wardlights {
    public static final String MODID = "wardlights";
    public static final String VERSION = "1.0.0";
    public static final String NAME = "Wardlights";
    public static final Logger LOGGER = LogManager.getLogger();

    @SidedProxy(clientSide = "wardlights.proxy.ClientProxy", serverSide = "wardlights.proxy.CommonProxy")
    public static CommonProxy PROXY;

    @Instance(MODID)
    public static Wardlights instance;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GeckoLib.initialize();
        ModRegistry.init();
        Wardlights.PROXY.preInit();
        if (Loader.isModLoaded(FirstAidCompat.MODID)) {
            MinecraftForge.EVENT_BUS.register(new FirstAidCompat());
        }
        if (Loader.isModLoaded(ArtifactsCompat.MODID)) {
            MinecraftForge.EVENT_BUS.register(new ArtifactsCompat());
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        if (Loader.isModLoaded(ModRegistry.NUCLEARCRAFT_MODID)) {
            NuclearCraftCompat.addRecipes();
        }
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
    }
}

/*
todo: [Lanterns]

- Lanterns Main Concept: bauble, right-hand and toolbelt equipable: Pale Lantern = keeps darkness entities at bay
  (cancels targeting) and make it a belt-equippable too - max 1 lantern active. (make each lantern have its own white and blacklist potion effects)
  only activates in darkness and/or underground (below y64), otherwise does not work. And add a effect called WardLight so people know
  when it is active.
- Lantern types:
  Lantern 1; Regular Lantern - Creates a light source preventing darkness mobs using you as a excuse to materialize! (only underground & darkness)
  Lantern 2; Pixie Infused - prevents ice and fire pixie stealing, during both night and day and adds 5% chance below y25 for a mimic to spawn + gives luck I underground & in darkness.
  Lantern 3; Vapula Infused - Increase mining speed make vapula and geoachs mined into your ally temporary & increase their spawns (only underground & darkness)
  Lantern 4; Pale Infused - Have a entity list that will cancel their targeting if conditions met & make a whitelist of entities which will get pale bloom effect when they are nearby
  Lantern 5; Parasite Infused - make SRP targeting ignore you (also during light) <config.perc.times> Also clears entities of COTH within the "Lantern Radius" config
- Lantern Looks:
  Lantern 1; (Default) simple cast iron lantern with a candle burning.
  Lantern 2; Lavender like glow and little fire-fly pink/purple/white like particles floating about
  Lantern 3; Crystal floating in the center emitting blue-white light
  Lantern 4; Overgrown lantern, pale leaves, white light and white particles floating about.
  Lantern 5; Tendrils poking out making a twitching animation with red light emitted from within. SRP overgrowth
- Crafting:
  Lantern 1; 4x iron ingot 4x framed glass pane 1x glowing powder (Or 10 gold coins at the blacksmith)
  Lantern 2; 1x lantern + 4x pixie dust + 4x silver
  Lantern 3; 1x lantern + 4x vapulacrystal/geonach spear + 4x heart dust
  Lantern 4; 1x lantern + 4x weird root + 4x amber valve
  Lantern 5; 1x lantern + 4x essential segment + 4x quench

- Make the lantern baubles reforgeable! (give them a bauble quality!)

- Next steps:
  1. Foundation: LanternType enum (Regular, Pixie, Vapula, Pale, Parasite), lanterns become a real bauble (IBauble),
     LanternUtil also checks the Baubles slots, not just the hands. Everything below depends on this.
  2. Effects per lantern (see Lantern types above). Regular's spawn blocking already works, but only for vanilla mobs
     and for every lantern alike.
  3. Recipes: need the exact item ids for pixie dust, silver, vapula crystal / geonach spear, heart dust, weird root,
     amber valve, essential segment, quench, framed glass pane and glowing powder.
  4. Fill in the empty Lycanites / Pale Bloom / SRParasites mixin stubs, and check the game starts with those mods.


done: [SOH 1 & 2 + ingredients] (all recipes live in the config, see ForgeConfigHandler)

- SOH 1 = Bulwark of Valor, SOH 2 = Aegis of Sacrifice (Trinkets and Baubles' Shield of Honor upgrades)
- Ingredients, each registered only when its source mods are loaded, with a "where it comes from" tooltip:
  Plutonium Alloy, Plutonium Blood Alloy (NuclearCraft Alloy Furnace), Corrupted Heart, Corrupted Fabric,
  Blood / Blight Essence, Tainted Flesh (dropped via EagleMixins' parasite replacement drop)
- todo: Blight Essence green glint (like Blood Essence's red one, same strength, e.g. 008000), once its texture is final
- Badge of Bravery chain:
  steel, blood essence, steel / silver, tainted flesh, silver / steel, blight essence, steel -> Dormant Badge
  Dormant Badge + Ancient Coins -> Badge of Bravery (Brutal Merchant trade in DregoraRL; fallback recipe in config)


later: [Waking Night] (separate mod, kept here for reference)

- Owns the night events, the Waking Bloods / Blights, their essence drops + XP bonus, and Waking Nightmare.
  Connects to Wardlights only through item / block ids in its config (e.g. lanterns as sleep protection).
- Essences may move from Wardlights to Waking Night (then the Dormant Badge recipe default uses wakingnight: ids).
- Looks:
  Blood: red mist around a bright core.
  Blight: yellow-green haze with drifting spore pixels.

- Blooded / Blighted mobs (existing mobs, no new entities):
  whitelisted mob spawning during a bloodmoon / lycanites event -> chance roll -> marked (capability, saved, synced)
  name prefix "Blooded <name>" / "Blighted <name>" (mixin Entity#getName, translated)
  configurable potion list, applied on marking
  2-6 random blobs picked from ~10 animated GeckoLib blob models, placed on the hitbox via a saved seed
  per-mob offset/scale overrides in the whitelist; drops essence (min/max/chance config)
  much more XP than normal (LivingExperienceDropEvent, configurable multiplier / flat bonus)

- Waking Nightmare (additive on top of the normal Bloodmoon / Lycanites schedules, which stay untouched):
  per-player capability counts sleeps without protection (checked on PlayerWakeUpEvent, only when the night was skipped)
  thresholds rolled once per player and saved: 8-12 -> Waking Nightmare I, 15-18 -> II, keep sleeping -> III
    before I: uneasy wake-up messages
    I:   ominous sounds around the player (client side, only for that player)
    II:  random insomnia (some sleep attempts fail with a message)
    III: next night summons a bloodmoon (BloodmoonHandler.INSTANCE.force()) or a Lycanites world event
         (ExtendedWorld.startWorldEvent, event whitelist in config, destructive ones like eruption excluded by default),
         then again every 3-5 days while level III lasts
  trigger check at nightfall, same timing as Bloodmoon (just before worldTime % 24000 == 12000, so force() lands on
    Bloodmoon's own 12000 check), which is before beds work, so levels set on wake-up are already in place
  rule: ALL online players must have Waking Nightmare III (one player without it blocks it; if they log out, the
    next nightfall triggers)
  check: loop over all online players, if every one has Waking Nightmare III -> trigger on that tick
  event pick, last event saved in WorldSavedData (null / 1 = bloodmoon / 2 = lycanites):
    null -> 50/50, last 1 -> 75% lycanites / 25% bloodmoon, last 2 -> 75% bloodmoon / 25% lycanites
  timing: decide + force() at tick 11999, show the title at tick 12000 (worldTime % 24000, overworld, END phase), the
    same tick Bloodmoon sends its red "The Blood Moon is rising..." chat message (text.bloodmoon.notify)
  on trigger: title "The Waking Night" for everyone, subtitle "Creatures Blooded have amassed!" (bloodmoon) or
    "Creatures Blighted have amassed!" (lycanites), lang keys; plus the level I ominous sounds;
    that night can't be slept through
  a protected sleep lowers the level by one (III -> II -> I -> none)
  protection (any one):
    ChunkComfort comfort effect (chunkcomfort:comfort) at or above a configured level (default 10, the level shown
      in the HUD; amplifier = level - 1, so default = 10th tier = 250 comfort with ChunkComfort's default tiers)
    comfort effect (any tier) + a placed Wardlights lantern within a radius of the bed
      (config: PLACED / INVENTORY / BOTH, default PLACED)
    mod_lavacow:dreamcatcher equipped in a Baubles slot (it's an IBauble)
  Waking Nightmare is a potion effect (levels I-III) so it shows in the HUD


todo: [Baubles] (after the lanterns, need models)

- Ring of Cleansing (LC): applies immalleable and removes link (ring slot)
- Charm of Guarding (Teddy Bear + Blood Gem): counters fear, corrosion and needler (necklace slot)
- Viral Ring: limits viral to max 2 (ring slot)
- Sentient Crown: gives SRP rage effect and adaptation of max 5% (head slot)
15 seconds of rage I below 85% and 15 seconds of rage II below 35% (just something that happens and might save you in a pickle?)
- Cyclops Eye: gives strength 1 and looting 1, same as the dragon eye
- Siren Scale: Swimming Speed 1 (universal slot)
- Pendant of Plutonium Warding: an irradiated aura weakening parasites in a radius around the player (necklace slot)

 */