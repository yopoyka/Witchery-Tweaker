package muwa.witcherytweaker.coremod;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

@IFMLLoadingPlugin.Name("Witchery Tweaker Coremod")
@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.SortingIndex(1001)
public class CorePlugin implements IFMLLoadingPlugin {
    public static Logger log = LogManager.getLogger("Witweaker Core Plugin");

    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "muwa.witcherytweaker.coremod.ClassTransformer" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
