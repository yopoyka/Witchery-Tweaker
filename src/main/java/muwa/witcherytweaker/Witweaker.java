package muwa.witcherytweaker;

import codechicken.nei.api.API;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import minetweaker.MineTweakerAPI;
import muwa.witcherytweaker.common.IProxy;
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
//        FileSystem fs = FileSystems.newFileSystem(new URI("jar:file:///C:/Users/muwa/work/witchery/lib/witchery-1.7.10-0.24.1-dev-copy.jar"), new HashMap<>());
//        byte[] transform = ServerCoreMod.transformer.transform("com.emoniph.witchery.blocks.BlockWitchesOven$ContainerWitchesOven", null, Files.readAllBytes(fs.getPath("com/emoniph/witchery/blocks/BlockWitchesOven$ContainerWitchesOven.class")));
//        transform = ClientCoreMod.transformer.transform("com.emoniph.witchery.blocks.BlockWitchesOven$ContainerWitchesOven", null, transform);
//        Files.write(Paths.get("lib/test.class"), transform);
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
        API.registerRecipeHandler(new NeiWitchOvenHandler());
        API.registerUsageHandler(new NeiWitchOvenHandler());

        clientProxy.preInit();
        serverProxy.preInit();
    }
}
