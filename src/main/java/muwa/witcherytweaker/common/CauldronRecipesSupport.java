package muwa.witcherytweaker.common;

import com.emoniph.witchery.brewing.*;
import com.emoniph.witchery.brewing.action.BrewAction;
import com.emoniph.witchery.brewing.action.BrewActionRitualRecipe;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.item.IItemStack;
import muwa.witcherytweaker.Witweaker;
import muwa.witcherytweaker.common.util.MTUtil;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Hashtable;

@ZenClass("mods.witchery.cauldron")
public class CauldronRecipesSupport {
    public static Delegate delegate = new Delegate();
    public static boolean ignoreWarning = false;

    public static class Delegate {
        protected static Method register;
        protected static Method cauldron;
        protected static Field ingredients;

        static {
            try {
                register = WitcheryBrewRegistry.class.getDeclaredMethod("register", BrewAction.class);
                register.setAccessible(true);
                cauldron = WitcheryBrewRegistry.class.getDeclaredMethod("wtw_cauldron");
                ingredients = WitcheryBrewRegistry.class.getDeclaredField("ingredients");
                ingredients.setAccessible(true);
            } catch (NoSuchMethodException | NoSuchFieldException e) {
                Witweaker.log.catching(e);
            }
        }

        public void add(IItemStack result, IItemStack[] items, int powerCost) throws Exception {
            add(result, items[items.length - 1], new IItemStack[][] { Arrays.copyOf(items, items.length - 1) }, powerCost);
        }

        public void add(IItemStack result, IItemStack last, IItemStack[][] recipes, int powerCost) throws Exception {
            register(createAction(result, last, recipes, powerCost));
        }

        protected static BrewActionRitualRecipe createAction(IItemStack result, IItemStack last, IItemStack[][] recipes, int powerCost) {
            ItemStack resultStack = (ItemStack) result.getInternal();
            ItemStack lastStack = (ItemStack) last.getInternal();
            return new BrewActionRitualRecipe(
                    BrewItemKey.fromStack(lastStack),
                    new AltarPower(powerCost),
                    Arrays.stream(recipes)
                            .map(stacks -> (ItemStack[]) Arrays.stream(stacks).map(s -> (ItemStack) s.getInternal()).toArray(ItemStack[]::new))
                            .map(stacks -> new BrewActionRitualRecipe.Recipe(resultStack, stacks))
                            .toArray(BrewActionRitualRecipe.Recipe[]::new)
            );
        }

        public void reload() {
            Witweaker.log.info("Reloading Cauldron recipes.");
            ignoreWarning = false;
            WitcheryBrewRegistry.INSTANCE.getRecipes();
            MTUtil.wrapError(() -> {
                Hashtable<BrewItemKey, BrewAction> ingreds = getIngreds();
                WitcheryBrewRegistry.INSTANCE.getRecipes().forEach(r -> ingreds.remove(r.ITEM_KEY));
                WitcheryBrewRegistry.INSTANCE.getRecipes().clear();
            });
            MTUtil.wrapError(() -> cauldron.invoke(WitcheryBrewRegistry.INSTANCE));
        }

        public void postReload() {
        }

        protected static void register(BrewAction action) {
            MTUtil.wrapError(() -> register.invoke(WitcheryBrewRegistry.INSTANCE, action));
        }

        protected static Hashtable<BrewItemKey, BrewAction> getIngreds() {
            try {
                return (Hashtable<BrewItemKey, BrewAction>) ingredients.get(WitcheryBrewRegistry.INSTANCE);
            } catch (IllegalAccessException e) {
                Witweaker.log.catching(e);
                return null;
            }
        }
    }

    public static void init() {
        MineTweakerImplementationAPI.onReloadEvent(reloadEvent -> delegate.reload());
        MineTweakerImplementationAPI.onPostReload(reloadEvent -> delegate.postReload());
    }

    @ZenMethod
    public static void ignoreWarning() {
        ignoreWarning = true;
    }

    @ZenMethod
    public static void ignoreWarning(boolean value) {
        ignoreWarning = value;
    }

    @ZenMethod
    public static void add(IItemStack result, IItemStack[] items) throws Exception {
        add(result, items, 0);
    }

    @ZenMethod
    public static void add(IItemStack result, IItemStack[] items, int power) throws Exception {
        delegate.add(result, items, power);
    }

    @ZenMethod
    public static void add(IItemStack result, IItemStack last, IItemStack[][] recipes) throws Exception {
        add(result, last, recipes, 0);
    }

    @ZenMethod
    public static void add(IItemStack result, IItemStack last, IItemStack[][] recipes, int power) throws Exception {
        delegate.add(result, last, recipes, power);
    }
}
