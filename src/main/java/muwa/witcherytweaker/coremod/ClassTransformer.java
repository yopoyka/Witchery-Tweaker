package muwa.witcherytweaker.coremod;

import net.minecraft.launchwrapper.IClassTransformer;
import org.apache.logging.log4j.Level;

import static muwa.witcherytweaker.coremod.CorePlugin.log;

public class ClassTransformer implements IClassTransformer {
    private static IClassTransformer client = (name, transformedName, basicClass) -> basicClass;
    private static IClassTransformer server = (name, transformedName, basicClass) -> basicClass;

    static {
        try {
            Class<?> clientClass = Class.forName("muwa.witcherytweaker.client.ClientCoreMod");
            client = (IClassTransformer) clientClass.getDeclaredField("transformer").get(null);
            log.info("Client coremod part loaded");
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            log.warn("Client coremod part not found");
            log.catching(Level.WARN, e);
        }
        try {
            Class<?> serverClass = Class.forName("muwa.witcherytweaker.server.ServerCoreMod");
            server = (IClassTransformer) serverClass.getDeclaredField("transformer").get(null);
            log.info("Server coremod part loaded");
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            log.warn("Server coremod part not found");
            log.catching(Level.WARN, e);
        }
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        return client.transform(name, transformedName, server.transform(name, transformedName, basicClass));
    }
}
