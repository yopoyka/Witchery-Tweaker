package muwa.witcherytweaker.client;

import muwa.witcherytweaker.common.IProxy;

public class ClientProxy implements IProxy {
    public static final IProxy instance = new ClientProxy();

    @Override
    public void preInit() {
    }
}
