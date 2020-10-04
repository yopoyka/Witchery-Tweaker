package muwa.witcherytweaker;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import muwa.witcherytweaker.client.ClientCoreMod;
import muwa.witcherytweaker.common.IProxy;
import muwa.witcherytweaker.common.MessageCookTime;
import muwa.witcherytweaker.server.ServerCoreMod;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

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

//    public static void main(String[] args) throws URISyntaxException, IOException {
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

        serverProxy.preInit();
        clientProxy.preInit();

        net = NetworkRegistry.INSTANCE.newSimpleChannel("witweak");
        net.registerMessage(MessageCookTime.Handler.class, MessageCookTime.class, 0, Side.CLIENT);
    }
}
