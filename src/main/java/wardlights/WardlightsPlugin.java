package wardlights;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;

@IFMLLoadingPlugin.MCVersion("1.12.2")
public class WardlightsPlugin implements IFMLLoadingPlugin {

	public WardlightsPlugin() {
		MixinBootstrap.init();

		// The mixin configs (mixins.wardlights.*.json) are enqueued for us by FermiumBooter,
		// driven by the @MixinConfig.MixinToggle fields in WardlightsConfig.MixinToggleConfig
		// and their @MixinConfig.CompatHandling mod-presence checks.
		//
		// Only add explicit calls here for conditions the annotations can't express, e.g.:
		//   FermiumRegistryAPI.enqueueMixin(true, "mixins.wardlights.foo.json",
		//           () -> Loader.isModLoaded("a") && Loader.isModLoaded("b"));
		// First arg: false = early (vanilla/coremod targets), true = late (other mods).
	}

	@Override
	public String[] getASMTransformerClass()
	{
		return new String[0];
	}
	
	@Override
	public String getModContainerClass()
	{
		return null;
	}
	
	@Override
	public String getSetupClass()
	{
		return null;
	}
	
	@Override
	public void injectData(Map<String, Object> data) { }
	
	@Override
	public String getAccessTransformerClass()
	{
		return null;
	}
}