package muwa.witcherytweaker.common;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryRecipes;
import com.emoniph.witchery.crafting.KettleRecipes;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.item.IItemStack;
import muwa.witcherytweaker.Witweaker;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Objects;

import static muwa.witcherytweaker.common.util.MTUtil.wrapError;
import static muwa.witcherytweaker.common.util.ReflectUtil.makeAccessible;

@ZenClass("mods.witchery.kettle")
public class KettleRecipesSupport {
    private static Constructor<KettleRecipes.KettleRecipe> constructor;
    private static Method loadKettle;

    static {
        try {
            constructor = KettleRecipes.KettleRecipe.class.getDeclaredConstructor(ItemStack.class, int.class, int.class, float.class, int.class, int.class, boolean.class, ItemStack[].class);
            constructor.setAccessible(true);
            loadKettle = WitcheryRecipes.class.getDeclaredMethod("wtw_kettle");
        } catch (NoSuchMethodException e) {
            Witweaker.log.catching(e);
        }
    }

    public static void init() {
        MineTweakerImplementationAPI.onReloadEvent(reloadEvent -> {
            Witweaker.log.info("Reloading Kettle recipes.");
            KettleRecipes.instance().recipes.clear();
            wrapError(() -> loadKettle.invoke(Witchery.Recipes));
        });
    }

    @ZenMethod
    public static KettleRecipeWrapper add(
            IItemStack output,
            IItemStack[] input
    ) throws Exception {
        return add(output, input, 0F);
    }

    @ZenMethod
    public static KettleRecipeWrapper add(
            IItemStack output,
            IItemStack[] input,
            float power
    ) throws Exception {
        return add(
                output,
                input,
                power,
                Objects.hash(((ItemStack) output.getInternal()).getItem(), ((ItemStack) output.getInternal()).getItemDamage()),
                0,
                0,
                0,
                true
        );
    }

    @ZenMethod
    public static KettleRecipeWrapper add(
            IItemStack output,
            IItemStack[] input,
            float power,
            int color,
            int hatBonus,
            int familiarType,
            int dimension,
            boolean inBook
    ) throws Exception {
        if (input.length != 6 || Arrays.stream(input).anyMatch(i -> i == null || i.getInternal() == null)) {
            throw new Exception("Kettle doesn't support partial recipes. You must provide 6 (six) ingredients.");
        }
        KettleRecipes.KettleRecipe recipe = KettleRecipes.instance().addRecipe(
                (ItemStack) output.getInternal(),
                hatBonus,
                familiarType,
                power,
                color,
                dimension,
                inBook,
                Arrays.stream(input).map(i -> (ItemStack) i.getInternal()).toArray(ItemStack[]::new)
        );
        return new KettleRecipeWrapper(recipe);
    }

    @ZenMethod
    public static void remove(IItemStack output) {
        KettleRecipes.KettleRecipe recipe = KettleRecipes.instance().findRecipeFor((ItemStack) output.getInternal());
        if (recipe != null)
            KettleRecipes.instance().recipes.remove(recipe);
    }

    @ZenMethod
    public static void removeAll(IItemStack output) {
        ItemStack out = (ItemStack) output.getInternal();
        KettleRecipes.instance().recipes.removeIf(recipe -> recipe.output.isItemEqual(out));
    }

    @ZenMethod
    public static KettleRecipeWrapper get(IItemStack output) {
        KettleRecipes.KettleRecipe recipe = KettleRecipes.instance().findRecipeFor((ItemStack) output.getInternal());
        if (recipe != null)
            return new KettleRecipeWrapper(recipe);
        else
            return null;
    }

    @ZenMethod
    public static KettleRecipeWrapper[] getAll(IItemStack output) {
        ItemStack out = (ItemStack) output.getInternal();
        return KettleRecipes.instance().recipes
                .stream()
                .filter(recipe -> recipe.output.isItemEqual(out))
                .map(KettleRecipeWrapper::new)
                .toArray(KettleRecipeWrapper[]::new);
    }

    @ZenClass("mods.witchery.KettleRecipe")
    public static class KettleRecipeWrapper {
        private static Field power;
        private static Field color;
        private static Field hatBonus;
        private static Field familiarType;
        private static Field dimension;
        private static Field inBook;

        static {
            try {
                power = KettleRecipes.KettleRecipe.class.getDeclaredField("power");
                color = KettleRecipes.KettleRecipe.class.getDeclaredField("color");
                hatBonus = KettleRecipes.KettleRecipe.class.getDeclaredField("hatBonus");
                familiarType = KettleRecipes.KettleRecipe.class.getDeclaredField("familiarType");
                dimension = KettleRecipes.KettleRecipe.class.getDeclaredField("dimension");
                inBook = KettleRecipes.KettleRecipe.class.getDeclaredField("inBook");
            } catch (NoSuchFieldException e) {
                Witweaker.log.catching(e);
            }
            makeAccessible(power);
            makeAccessible(color);
            makeAccessible(hatBonus);
            makeAccessible(familiarType);
            makeAccessible(dimension);
            makeAccessible(inBook);
        }

        public final KettleRecipes.KettleRecipe recipe;

        public KettleRecipeWrapper(KettleRecipes.KettleRecipe recipe) {
            this.recipe = recipe;
        }

        @ZenMethod
        public KettleRecipeWrapper setTranslationKey(String key) {
            recipe.setUnlocalizedName(key);
            return this;
        }

        @ZenMethod
        public KettleRecipeWrapper power(float powerValue) {
            wrapError(() -> power.setFloat(this.recipe, powerValue));
            return this;
        }

        @ZenMethod
        public KettleRecipeWrapper color(int colorValue) {
            wrapError(() -> color.setInt(this.recipe, colorValue));
            return this;
        }

        @ZenMethod
        public KettleRecipeWrapper hatBonus(int hat) {
            wrapError(() -> hatBonus.setInt(this.recipe, hat));
            return this;
        }

        @ZenMethod
        public KettleRecipeWrapper familiar(int type) {
            wrapError(() -> familiarType.setInt(this.recipe, type));
            return this;
        }

        @ZenMethod
        public KettleRecipeWrapper dimension(int dim) {
            wrapError(() -> dimension.setInt(this.recipe, dim));
            return this;
        }

        @ZenMethod
        public KettleRecipeWrapper inBook(boolean value) {
            wrapError(() -> inBook.setBoolean(this.recipe, value));
            return this;
        }
    }
}
