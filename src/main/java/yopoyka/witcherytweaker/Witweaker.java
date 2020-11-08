package yopoyka.witcherytweaker;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import minetweaker.MineTweakerAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import yopoyka.witcherytweaker.common.*;

@Mod(
        modid = Witweaker.MOD_ID,
        name = Witweaker.MOD_NAME,
        version = Witweaker.VERSION,
        dependencies = "required-after:witchery;required-after:MineTweaker3;"
)
public class Witweaker {
    public static final String MOD_ID = "witweaker";
    public static final String MOD_NAME = "Witchery Tweaker";
    public static final String VERSION = "@VERSION@";
    @SidedProxy(clientSide = "yopoyka.witcherytweaker.client.ClientProxy", serverSide = "yopoyka.witcherytweaker.server.ServerProxy")
    private static IProxy proxy;
    public static final Logger log = LogManager.getLogger("Witchery Tweaker");

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        MineTweakerAPI.registerClass(WitchOvenRecipesSupport.class);
        MineTweakerAPI.registerClass(OvenRecipe.class);
        MineTweakerAPI.registerClass(KettleRecipesSupport.class);
        MineTweakerAPI.registerClass(CauldronRecipesSupport.class);
        MineTweakerAPI.registerClass(DistilleryRecipesSupport.class);
        WitchOvenRecipesSupport.init();
        KettleRecipesSupport.init();
        CauldronRecipesSupport.init();
        DistilleryRecipesSupport.init();

        proxy.preInit();
    }
}
