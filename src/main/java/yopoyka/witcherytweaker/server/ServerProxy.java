package yopoyka.witcherytweaker.server;

import yopoyka.witcherytweaker.Witweaker;
import yopoyka.witcherytweaker.common.CauldronRecipesSupport;
import yopoyka.witcherytweaker.common.IProxy;
import yopoyka.witcherytweaker.common.WitchOvenRecipes;

public class ServerProxy implements IProxy {
    public static final IProxy instance = new ServerProxy();

    @Override
    public void preInit() {
        Witweaker.log.info("Pre Init Server Proxy");
        WitchOvenRecipes.factory = IOvenRecipe.ServerImpl::new;
        CauldronRecipesSupport.delegate = new CauldronDelegate();
    }
}
