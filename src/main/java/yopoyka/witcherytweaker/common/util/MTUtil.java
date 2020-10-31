package yopoyka.witcherytweaker.common.util;

import minetweaker.MineTweakerAPI;
import yopoyka.witcherytweaker.Witweaker;

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
