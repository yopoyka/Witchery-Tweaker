package muwa.witcherytweaker.common.util;

import minetweaker.MineTweakerAPI;
import muwa.witcherytweaker.Witweaker;

public class MTUtil {
    public static interface Task {
        public void run() throws Throwable;
    }

    public static void wrapError(Task task) {
        try {
            task.run();
        }
        catch (Throwable t) {
            MineTweakerAPI.logError(t.toString());
            Witweaker.log.catching(t);
        }
    }
}
