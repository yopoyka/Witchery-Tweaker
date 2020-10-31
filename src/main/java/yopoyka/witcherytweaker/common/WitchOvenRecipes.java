package yopoyka.witcherytweaker.common;

import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.item.IItemStack;
import yopoyka.witcherytweaker.Witweaker;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.witchery.witchOven")
public class WitchOvenRecipes {
    public static final int defaultCookTime = 180;
    public static final double defaultChance = 0.3;
    public static final List<Impl> recipes = new ArrayList<>();
    public static IRecipeFactory factory = Impl::new;
    public static boolean defaultEnabled = true;

    public static void init() {
        MineTweakerImplementationAPI.onReloadEvent(reloadEvent -> {
            Witweaker.log.info("Reloading Witch's Oven recipes.");
            recipes.clear();
            defaultEnabled = true;
        });
    }

    public static interface IRecipeFactory {
        public Impl create(ItemStack input, ItemStack output, ItemStack byProduct, int cookTime, double byProductChance, int jarsRequired, boolean strictByProduct, boolean ignoreFunnelChance, boolean ignoreFunnelSpeed);
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
    public static Impl add(IItemStack input, IItemStack output) {
        Impl r = factory.create(
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
    public static Impl add(IItemStack input, IItemStack output, IItemStack byProduct) {
        Impl r = factory.create(
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

    @ZenClass("mods.witchery.OvenRecipe")
    public static class Impl extends WitchOvenRecipes {
        public boolean ignoreFunnelChance;
        public boolean ignoreFunnelSpeed;
        public int cookTime;
        public int cookTimeFraction;
        public ItemStack input;
        public ItemStack output;
        public ItemStack byProduct;
        public double byProductChance;
        public boolean strictByProduct;
        public int jarsRequired;

        public Impl(
                ItemStack input,
                ItemStack output,
                ItemStack byProduct,
                int cookTime,
                double byProductChance,
                int jarsRequired,
                boolean strictByProduct,
                boolean ignoreFunnelChance,
                boolean ignoreFunnelSpeed
        ) {
            this.ignoreFunnelChance = ignoreFunnelChance;
            this.ignoreFunnelSpeed = ignoreFunnelSpeed;
            this.cookTime = cookTime;
            this.cookTimeFraction = cookTime / 9;
            this.input = input;
            this.output = output;
            this.byProduct = byProduct;
            this.byProductChance = byProductChance;
            this.strictByProduct = strictByProduct;
            this.jarsRequired = jarsRequired;
        }

        @ZenMethod
        public Impl time(int cookTime) {
            this.cookTime = cookTime;
            return this;
        }

        @ZenMethod
        public Impl cookTime(int cookTime) {
            this.cookTime = cookTime;
            return this;
        }

        @ZenMethod
        public Impl chance(double byProductChance) {
            this.byProductChance = byProductChance;
            return this;
        }

        @ZenMethod
        public Impl defaultChance() {
            this.byProductChance = defaultChance;
            return this;
        }

        @ZenMethod
        public Impl defaultTime() {
            this.cookTime = defaultCookTime;
            return this;
        }

        @ZenMethod
        public Impl defaultCookTime() {
            this.cookTime = defaultCookTime;
            return this;
        }

        @ZenMethod
        public Impl jars(int jars) {
            jarsRequired = jars;
            return this;
        }

        @ZenMethod
        public Impl strict() {
            strictByProduct = true;
            return this;
        }

        @ZenMethod
        public Impl strict(boolean strict) {
            strictByProduct = strict;
            return this;
        }

        @ZenMethod
        public Impl ignoreFunnelChance() {
            ignoreFunnelChance = true;
            return this;
        }

        @ZenMethod
        public Impl ignoreFunnelChance(boolean ignore) {
            ignoreFunnelChance = ignore;
            return this;
        }

        @ZenMethod
        public Impl ignoreFunnelSpeed() {
            ignoreFunnelSpeed = true;
            return this;
        }

        @ZenMethod
        public Impl ignoreFunnelSpeed(boolean ignore) {
            ignoreFunnelSpeed = ignore;
            return this;
        }
    }
}
