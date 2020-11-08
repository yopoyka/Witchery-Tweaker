package yopoyka.witcherytweaker.common;

import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import yopoyka.witcherytweaker.Witweaker;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.witchery.witchOven")
public class WitchOvenRecipesSupport {
    public static final int defaultCookTime = 180;
    public static final double defaultChance = 0.3;
    public static final List<OvenRecipe> recipes = new ArrayList<>();
    public static boolean defaultEnabled = true;

    public static void init() {
        MineTweakerImplementationAPI.onReloadEvent(reloadEvent -> {
            Witweaker.log.info("Reloading Witch's Oven recipes.");
            recipes.clear();
            defaultEnabled = true;
        });
    }

    public static interface IRecipeFactory {
        public OvenRecipe create(ItemStack input, ItemStack output, ItemStack byProduct, int cookTime, double byProductChance, int jarsRequired, boolean strictByProduct, boolean ignoreFunnelChance, boolean ignoreFunnelSpeed);
    }

    @ZenMethod
    public static void removeDefault() {
        defaultEnabled = false;
    }

    @ZenMethod
    public static void addDefault() {
        defaultEnabled = true;
    }

    @ZenMethod
    public static OvenRecipe add(IItemStack input, IItemStack output) {
        OvenRecipe r = new OvenRecipe(
                (ItemStack) input.getInternal(),
                (ItemStack) output.getInternal(),
                null,
                defaultCookTime,
                defaultChance,
                0,
                false,
                false,
                false
        );
        recipes.add(r);
        return r;
    }

    @ZenMethod
    public static OvenRecipe add(IItemStack input, IItemStack output, IItemStack byProduct) {
        OvenRecipe r = new OvenRecipe(
                (ItemStack) input.getInternal(),
                (ItemStack) output.getInternal(),
                (ItemStack) byProduct.getInternal(),
                defaultCookTime,
                defaultChance,
                1,
                false,
                false,
                false
        );
        recipes.add(r);
        return r;
    }
}
