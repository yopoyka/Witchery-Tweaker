package yopoyka.witcherytweaker.common;

import com.emoniph.witchery.brewing.*;
import com.emoniph.witchery.brewing.action.BrewAction;
import com.emoniph.witchery.brewing.action.BrewActionRitualRecipe;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import yopoyka.witcherytweaker.Witweaker;
import yopoyka.witcherytweaker.common.util.MTUtil;
import yopoyka.witcherytweaker.common.util.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ZenClass("mods.witchery.cauldron")
public class CauldronRecipesSupport {
    protected static Field recipesField;
    protected static Method register;
    protected static Method cauldron;
    protected static Field ingredients;
    protected static Set<BrewItemKey> elements = new HashSet<>();
    public static boolean ignoreWarning = false;

    static {
        try {
            recipesField = BrewActionRitualRecipe.class.getDeclaredField("recipes");
            ReflectUtil.makeAccessible(recipesField);
            register = WitcheryBrewRegistry.class.getDeclaredMethod("register", BrewAction.class);
            register.setAccessible(true);
            cauldron = WitcheryBrewRegistry.class.getDeclaredMethod("wtw_cauldron");
            ingredients = WitcheryBrewRegistry.class.getDeclaredField("ingredients");
            ingredients.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            Witweaker.log.catching(e);
        }
    }

    public static void init() {
        MineTweakerImplementationAPI.onReloadEvent(reloadEvent -> reload());
        MineTweakerImplementationAPI.onPostReload(reloadEvent -> postReload());
    }

    @ZenMethod
    public static void add(IItemStack result, IItemStack[] items, int powerCost) throws Exception {
        add(result, items[items.length - 1], new IItemStack[][] { Arrays.copyOf(items, items.length - 1) }, powerCost);
    }

    @ZenMethod
    public static void add(IItemStack result, IItemStack last, IItemStack[][] recipes, int powerCost) throws Exception {
        ItemStack lastStack = (ItemStack) last.getInternal();

        if (getIngreds().containsKey(BrewItemKey.fromStack(lastStack))) {
            throw new Exception("Brew Item Key already registered!");
        }

        if (!CauldronRecipesSupport.ignoreWarning) {
            for (IItemStack[] recipe : recipes) {
                List<ItemStack> items = Stream.concat(Arrays.stream(recipe).map(i -> (ItemStack) i.getInternal()), Stream.of(lastStack))
                        .collect(Collectors.toList());

                if (items.stream().map(BrewItemKey::fromStack).distinct().count() != recipe.length + 1)
                    throw new Exception("Same item occurs more than once. This recipe is not going to work!");

                NBTTagCompound nbtRoot = new NBTTagCompound();
                NBTTagList nbtItems = new NBTTagList();
                nbtRoot.setTag("Items", nbtItems);

                for (ItemStack item : items) {
                    BrewAction action = WitcheryBrewRegistry.INSTANCE.getActionForItemStack(item);
                    if (action == null) {
                        nbtItems.appendTag(item.writeToNBT(new NBTTagCompound()));
                    }
                    else if (!WitcheryBrewRegistry.INSTANCE.canAdd(nbtRoot, action, true)) {
                        throw new Exception("This recipe is not going to work! At: " + MineTweakerMC.getIItemStack(item));
                    }
                }
            }
        }

        Arrays.stream(recipes)
                .flatMap(Arrays::stream)
                .map(s -> (ItemStack) s.getInternal())
                .map(BrewItemKey::fromStack)
                .forEach(elements::add);

        register(createAction(result, last, recipes, powerCost));
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
    public static void add(IItemStack result, IItemStack last, IItemStack[][] recipes) throws Exception {
        add(result, last, recipes, 0);
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

    public static void reload() {
        Witweaker.log.info("Reloading Cauldron recipes.");
        ignoreWarning = false;
        WitcheryBrewRegistry.INSTANCE.getRecipes();
        MTUtil.wrapError(() -> {
            Hashtable<BrewItemKey, BrewAction> ingreds = getIngreds();
            WitcheryBrewRegistry.INSTANCE.getRecipes().forEach(r -> ingreds.remove(r.ITEM_KEY));
            WitcheryBrewRegistry.INSTANCE.getRecipes().clear();
        });
        MTUtil.wrapError(() -> cauldron.invoke(WitcheryBrewRegistry.INSTANCE));
        Hashtable<BrewItemKey, BrewAction> ingreds = getIngreds();
        elements.forEach(ingreds::remove);
        elements.clear();
    }

    public static void postReload() {
        Hashtable<BrewItemKey, BrewAction> ingreds = getIngreds();
        elements.forEach(brewItemKey -> ingreds.put(brewItemKey, new BrewActionNone(
                brewItemKey,
                new BrewNamePart("none"),
                new AltarPower(0),
                Probability.CERTAIN,
                true
        )));
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
