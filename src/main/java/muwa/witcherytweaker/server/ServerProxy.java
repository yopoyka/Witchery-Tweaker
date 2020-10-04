package muwa.witcherytweaker.server;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import minetweaker.MineTweakerAPI;
import muwa.witcherytweaker.Witweaker;
import muwa.witcherytweaker.common.IProxy;
import muwa.witcherytweaker.common.WitchOvenRecipes;

public class ServerProxy implements IProxy {
    public static final IProxy instance = new ServerProxy();

    @Override
    public void preInit() {
        Witweaker.log.info("Pre Init Server Proxy");
        WitchOvenRecipes.factory = IOvenRecipe.ServerImpl::new;
    }
}
