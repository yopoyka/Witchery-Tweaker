package muwa.witcherytweaker;

import codechicken.nei.api.API;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import minetweaker.MineTweakerAPI;
import muwa.witcherytweaker.common.CauldronRecipesSupport;
import muwa.witcherytweaker.common.IProxy;
import muwa.witcherytweaker.common.KettleRecipesSupport;
import muwa.witcherytweaker.common.WitchOvenRecipes;
import muwa.witcherytweaker.common.nei.NeiWitchOvenHandler;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = Witweaker.MOD_ID,
        name = Witweaker.MOD_NAME,
        dependencies = "required-after:witchery;required-after:MineTweaker3;"

)
public class Witweaker {
    public static final String MOD_ID = "witweaker";
    public static final String MOD_NAME = "Witchery Tweaker";
    private static IProxy serverProxy = IProxy.EMPTY;
    private static IProxy clientProxy = IProxy.EMPTY;
    public static final Logger log = LogManager.getLogger("Witchery Tweaker");
    public static SimpleNetworkWrapper net;

//    public static void main(String[] args) {
////        FileSystem fs = FileSystems.newFileSystem(new URI("jar:file:///C:/Users/muwa/work/witchery/lib/witchery-1.7.10-0.24.1-dev-copy.jar"), new HashMap<>());
////        byte[] transform = ServerCoreMod.transformer.transform("com.emoniph.witchery.blocks.BlockWitchesOven$ContainerWitchesOven", null, Files.readAllBytes(fs.getPath("com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven.class")));
////        transform = ClientCoreMod.transformer.transform("com.emoniph.witchery.blocks.BlockWitchesOven$ContainerWitchesOven", null, transform);
////        Files.write(Paths.get("lib/test.class"), transform);
//        try {
//            MethodHandle constructor = MethodHandles
//                    .lookup()
//                    .findConstructor(
//                            KettleRecipes.KettleRecipe.class,
//                            MethodType.methodType(null, ItemStack.class, int.class, int.class, float.class, int.class, int.class, boolean.class, ItemStack[].class));
//            KettleRecipes.KettleRecipe o = (KettleRecipes.KettleRecipe) constructor.invokeExact((ItemStack) null, 1, 1, 2F, 1, 1, false, (ItemStack[]) null);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        try {
            serverProxy = (IProxy) Class.forName("muwa.witcherytweaker.server.ServerProxy").getDeclaredField("instance").get(null);
            log.info("Server Proxy loaded");
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            log.warn("Server Proxy not found");
            log.catching(Level.WARN, e);
        }
        try {
            clientProxy = (IProxy) Class.forName("muwa.witcherytweaker.client.ClientProxy").getDeclaredField("instance").get(null);
            log.info("Client Proxy loaded");
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            log.warn("Server Proxy not found");
            log.catching(Level.WARN, e);
        }

        MineTweakerAPI.registerClass(WitchOvenRecipes.class);
        MineTweakerAPI.registerClass(KettleRecipesSupport.class);
        MineTweakerAPI.registerClass(CauldronRecipesSupport.class);
        WitchOvenRecipes.init();
        KettleRecipesSupport.init();
        CauldronRecipesSupport.init();

        clientProxy.preInit();
        serverProxy.preInit();
    }
}
