package muwa.witcherytweaker.common;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.WitcheryRecipes;
import com.emoniph.witchery.crafting.DistilleryRecipes;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.item.IItemStack;
import muwa.witcherytweaker.Witweaker;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.lang.reflect.Method;

import static muwa.witcherytweaker.common.util.MTUtil.wrapError;

@ZenClass("mods.witchery.distillery")
public class DistilleryRecipesSupport {
    private static Method loadDefault;

    static {
        try {
            loadDefault = WitcheryRecipes.class.getDeclaredMethod("wtw_distillery");
        } catch (NoSuchMethodException e) {
            Witweaker.log.catching(e);
        }
    }

    public static void init() {
        MineTweakerImplementationAPI.onReloadEvent(reloadEvent -> {
            Witweaker.log.info("Reloading distillery recipes.");
            DistilleryRecipes.instance().recipes.clear();
            wrapError(() -> loadDefault.invoke(Witchery.Recipes));
        });
    }

    @ZenMethod
    public static void add(IItemStack[] inputs, IItemStack[] outputs, int jars, int cookTime) {
        if (inputs.length < 1 || inputs.length > 2)
            throw new IllegalArgumentException("Distillery inputs must be in range from 1 (one) to 2 (two) inclusively.");

        if (outputs.length < 1 || outputs.length > 4)
            throw new IllegalArgumentException("Distillery outputs must be in range from 1 (one) to 4 (four) inclusively.");

        final DistilleryRecipes.DistilleryRecipe recipe = DistilleryRecipes.instance().addRecipe(
                (ItemStack) inputs[0].getInternal(),
                (ItemStack) inputs[1].getInternal(),
                jars,
                (ItemStack) outputs[0].getInternal(),
                outputs.length > 1 ? (ItemStack) outputs[1].getInternal() : null,
                outputs.length > 2 ? (ItemStack) outputs[2].getInternal() : null,
                outputs.length > 3 ? (ItemStack) outputs[3].getInternal() : null
        );

        ((IDistilleryRecipe) recipe).setCookTime(cookTime);
    }

    @ZenMethod
    public static void add(IItemStack[] inputs, IItemStack[] outputs, int jars) {
        add(inputs, outputs, jars, 800);
    }
}
