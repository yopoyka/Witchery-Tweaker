package muwa.witcherytweaker.common.util;

import muwa.witcherytweaker.Witweaker;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ReflectUtil {
    public static void makeAccessible(Field field) {
        try {
            field.setAccessible(true);
            Field modifiers = Field.class.getDeclaredField("modifiers");
            modifiers.setAccessible(true);
            modifiers.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            Witweaker.log.catching(e);
        }
    }
}
