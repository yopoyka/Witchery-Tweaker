package muwa.witcherytweaker.common;

public interface IProxy {
    public static final IProxy EMPTY = new IProxy() {
        @Override
        public void preInit() {

        }
    };

    public void preInit();
}
