package muwa.witcherytweaker.common;

import java.util.Collection;
import java.util.Collections;

public interface IProxy {
    public static final IProxy EMPTY = new IProxy() {
        @Override
        public void preInit() {

        }
    };

    public void preInit();
}
