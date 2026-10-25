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
import wardlights.init.ModRegistry;
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
done: [Lanterns] (all recipes and tuning live in the config, see WardlightsConfig)

- 5 lanterns, each a bauble (belt), hand-held, Tool Belt or placed block; one ward per player at a time,
  shown by its own WardLight effect. Works only in darkness / underground, except the daylight traits noted below.
  Lantern 1; Regular - blocks darkness spawns (vanilla + Lycanites) around you.
  Lantern 2; Pixie - Ice and Fire pixies can't steal (day and night), 5% mimic chance deep in caves, Luck I.
  Lantern 3; Vapula - faster mining + Fortune, more Vapula / Geonach mining spawns, which become temporary allies.
  Lantern 4; Pale - Pale Bloom mobs ignore you, chance to give hostile mobs Pale Lung.
  Lantern 5; Parasite - SRP mobs may ignore you, clears COTH in the Lantern Radius and shortens Fear (day and night).
- Reforgeable through Quality Tools; placing and breaking a lantern keeps its quality.
- Dropped: per-lantern potion white/blacklists and per-lantern particles.


done: [SOH 1 & 2 + ingredients] (all recipes live in the config, see WardlightsConfig)

- SOH 1 = Bulwark of Valor, SOH 2 = Aegis of Sacrifice (Trinkets and Baubles' Shield of Honor upgrades)
- Ingredients, each registered only when its source mods are loaded, with a "where it comes from" tooltip:
  Plutonium Alloy, Plutonium Blood Alloy (NuclearCraft Alloy Furnace), Corrupted Heart, Corrupted Fabric,
  Blood / Blight Essence, Tainted Flesh (dropped via EagleMixins' parasite replacement drop)
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


todo: [Baubles] (need models)

- done: Viral Ring (Ancient Ring + Beckon Pearl + Tainted Flesh/Tears), Blooming Crown, id dispatcher_crown (Ancient
  Crown + Dispatcher Bloom + Tainted Flesh/Tears; Rage bursts + SRP Sentient Armor-style adaptation, max 5% per source,
  not yet tested in-game), Cyclops Eye (head, 5% from I&F Cyclopes; +1 Strength level, stacking + Looting I), Siren Scale
  (amulet, 5% from I&F Sirens; Lycanites Swiftswimming I), Ring of Piercing (was Ring of Cleansing; hitting a parasite
  inflicts Immalleable). Eye, Scale and Ring of Piercing still have placeholder textures.

- Pendant of Plutonium Warding: an irradiated aura weakening parasites in a radius around the player (necklace slot)

 */