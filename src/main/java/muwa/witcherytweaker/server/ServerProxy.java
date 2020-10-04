package muwa.witcherytweaker.server;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import minetweaker.MineTweakerAPI;
import muwa.witcherytweaker.WitchOvenRecipe;
import muwa.witcherytweaker.common.IProxy;

public class ServerProxy implements IProxy {
    public static final IProxy instance = new ServerProxy();

    @Override
    public void preInit() {
        MineTweakerAPI.registerClass(WitchOvenRecipe.class);
    }

    @SubscribeEvent
    public void a() {}

}
