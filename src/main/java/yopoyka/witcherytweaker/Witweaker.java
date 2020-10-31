package yopoyka.witcherytweaker;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import minetweaker.MineTweakerAPI;
import yopoyka.witcherytweaker.common.*;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private static IProxy serverProxy = IProxy.EMPTY;
    private static IProxy clientProxy = IProxy.EMPTY;
    public static final Logger log = LogManager.getLogger("Witchery Tweaker");
    public static SimpleNetworkWrapper net;

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        try {
            serverProxy = (IProxy) Class.forName("yopoyka.witcherytweaker.server.ServerProxy").getDeclaredField("instance").get(null);
            log.info("Server Proxy loaded");
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            log.warn("Server Proxy not found");
            log.catching(Level.WARN, e);
        }
        try {
            clientProxy = (IProxy) Class.forName("yopoyka.witcherytweaker.client.ClientProxy").getDeclaredField("instance").get(null);
            log.info("Client Proxy loaded");
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            log.warn("Server Proxy not found");
            log.catching(Level.WARN, e);
        }

        MineTweakerAPI.registerClass(WitchOvenRecipes.class);
        MineTweakerAPI.registerClass(KettleRecipesSupport.class);
        MineTweakerAPI.registerClass(CauldronRecipesSupport.class);
        MineTweakerAPI.registerClass(DistilleryRecipesSupport.class);
        WitchOvenRecipes.init();
        KettleRecipesSupport.init();
        CauldronRecipesSupport.init();
        DistilleryRecipesSupport.init();

        clientProxy.preInit();
        serverProxy.preInit();
    }
}
