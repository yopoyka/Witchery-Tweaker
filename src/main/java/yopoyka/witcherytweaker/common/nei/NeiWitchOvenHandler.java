package yopoyka.witcherytweaker.common.nei;

import codechicken.nei.PositionedStack;
import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.integration.NEIWitchesOvenRecipeHandler;
import yopoyka.witcherytweaker.Witweaker;
import yopoyka.witcherytweaker.common.WitchOvenRecipes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static codechicken.nei.NEIServerUtils.areStacksSameType;

public class NeiWitchOvenHandler extends NEIWitchesOvenRecipeHandler {
    private static Field ingredField;

    static {
        try {
            ingredField = NEIWitchesOvenRecipeHandler.SmeltingPair.class.getDeclaredField("ingred");
            ingredField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            Witweaker.log.catching(e);
        }
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        WitchOvenRecipes.recipes
                .stream()
                .filter(impl -> areStacksSameType(impl.output, result) || areStacksSameType(impl.byProduct, result))
                .map(impl -> new Recipe(
                        impl.input.copy(),
                        impl.output.copy(),
                        impl.byProduct.copy(),
                        impl.jarsRequired
                ))
                .forEach(recipe -> arecipes.add(recipe));
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals("witchery_cooking") && getClass() == NeiWitchOvenHandler.class) {
            WitchOvenRecipes.recipes.forEach(impl -> {
                arecipes.add(new Recipe(
                        impl.input.copy(),
                        impl.output.copy(),
                        impl.byProduct.copy(),
                        impl.jarsRequired
                ));
            });
        }
        else {
            super.loadCraftingRecipes(outputId, results);
        }
    }

    @Override
    public void loadUsageRecipes(String inputId, Object... ingredients) {
        if (inputId.equals("fuel") && getClass() == NeiWitchOvenHandler.class) {
            loadCraftingRecipes("witchery_cooking");
        } else {
            super.loadUsageRecipes(inputId, ingredients);
        }
    }

    @Override
    public void loadUsageRecipes(ItemStack ingred) {
        boolean jar = Witchery.Items.GENERIC.itemEmptyClayJar.isMatch(ingred);
        WitchOvenRecipes.recipes
                .stream()
                .filter(impl -> areStacksSameType(impl.input, ingred) || (jar && impl.jarsRequired > 0))
                .map(impl -> new Recipe(
                        impl.input.copy(),
                        impl.output.copy(),
                        impl.byProduct.copy(),
                        impl.jarsRequired
                ))
                .forEach(recipe -> arecipes.add(recipe));
    }

    private static final ItemStack empty = new ItemStack(Blocks.air);
    public class Recipe extends NEIWitchesOvenRecipeHandler.SmeltingPair {
        PositionedStack byProduct;
        PositionedStack jars;

        public Recipe(ItemStack ingred, ItemStack result, @Nullable ItemStack byproduct, int jars) {
            super(empty, result, empty);
            try {
                ingredField.set(this, new PositionedStack(ingred, 51, 6));
            } catch (IllegalAccessException e) {
                Witweaker.log.catching(e);
            }
            if (byproduct != null)
                byProduct = new PositionedStack(byproduct, 113, 42);
            if (jars > 0)
                this.jars = new PositionedStack(Witchery.Items.GENERIC.itemEmptyClayJar.createStack(jars), 78, 42);
        }


        public List<PositionedStack> getOtherStacks() {
            ArrayList<PositionedStack> stacks = new ArrayList<PositionedStack>();
            PositionedStack stack = getOtherStack();
            if (stack != null)
                stacks.add(stack);
            if (byProduct != null)
                stacks.add(byProduct);
            if (jars != null)
                stacks.add(jars);
            return stacks;
        }
    }
}
