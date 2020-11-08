package yopoyka.witcherytweaker.client;

import codechicken.nei.api.API;
import yopoyka.witcherytweaker.common.IProxy;
import yopoyka.witcherytweaker.common.nei.NeiWitchOvenHandler;

public class ClientProxy implements IProxy {
    @Override
    public void preInit() {
        API.registerRecipeHandler(new NeiWitchOvenHandler());
        API.registerUsageHandler(new NeiWitchOvenHandler());
    }
}
