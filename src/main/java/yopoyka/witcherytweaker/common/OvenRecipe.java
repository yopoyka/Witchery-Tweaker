package yopoyka.witcherytweaker.common;

import com.emoniph.witchery.blocks.BlockWitchesOven;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import static yopoyka.witcherytweaker.common.WitchOvenRecipesSupport.defaultChance;
import static yopoyka.witcherytweaker.common.WitchOvenRecipesSupport.defaultCookTime;

@ZenClass("mods.witchery.OvenRecipe")
public class OvenRecipe implements IOvenRecipe {
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

    public OvenRecipe(
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
    public OvenRecipe time(int cookTime) {
        this.cookTime = cookTime;
        return this;
    }

    @ZenMethod
    public OvenRecipe cookTime(int cookTime) {
        this.cookTime = cookTime;
        return this;
    }

    @ZenMethod
    public OvenRecipe chance(double byProductChance) {
        this.byProductChance = byProductChance;
        return this;
    }

    @ZenMethod
    public OvenRecipe defaultChance() {
        this.byProductChance = defaultChance;
        return this;
    }

    @ZenMethod
    public OvenRecipe defaultTime() {
        this.cookTime = defaultCookTime;
        return this;
    }

    @ZenMethod
    public OvenRecipe defaultCookTime() {
        this.cookTime = defaultCookTime;
        return this;
    }

    @ZenMethod
    public OvenRecipe jars(int jars) {
        jarsRequired = jars;
        return this;
    }

    @ZenMethod
    public OvenRecipe strict() {
        strictByProduct = true;
        return this;
    }

    @ZenMethod
    public OvenRecipe strict(boolean strict) {
        strictByProduct = strict;
        return this;
    }

    @ZenMethod
    public OvenRecipe ignoreFunnelChance() {
        ignoreFunnelChance = true;
        return this;
    }

    @ZenMethod
    public OvenRecipe ignoreFunnelChance(boolean ignore) {
        ignoreFunnelChance = ignore;
        return this;
    }

    @ZenMethod
    public OvenRecipe ignoreFunnelSpeed() {
        ignoreFunnelSpeed = true;
        return this;
    }

    @ZenMethod
    public OvenRecipe ignoreFunnelSpeed(boolean ignore) {
        ignoreFunnelSpeed = ignore;
        return this;
    }

    public static final int
            INPUT = 0,
            FUEL = 1,
            OUTPUT = 2,
            BYPRODUCT = 3,
            JARS = 4;

    public int getCookTime(BlockWitchesOven.TileEntityWitchesOven oven) {
        return ignoreFunnelSpeed ? cookTime : cookTime - cookTimeFraction * ((IWitchOvenTile) oven).getFumeFunnels();
    }

    @Override
    public boolean canSmelt(BlockWitchesOven.TileEntityWitchesOven oven) {
        ItemStack input = oven.getStackInSlot(INPUT);

        if (input == null || !this.input.isItemEqual(input) || input.stackSize < this.input.stackSize)
            return false;

        ItemStack output = oven.getStackInSlot(OUTPUT);
        if (output != null && (!output.isItemEqual(this.output) || output.stackSize + this.output.stackSize > this.output.getMaxStackSize()))
            return false;

        if (strictByProduct) {
            if (jarsRequired > 0 && (oven.getStackInSlot(JARS) == null || oven.getStackInSlot(JARS).stackSize < jarsRequired))
                return false;

            ItemStack byProduct = oven.getStackInSlot(BYPRODUCT);
            return byProduct == null || (byProduct.isItemEqual(this.byProduct) && byProduct.stackSize + this.byProduct.stackSize <= byProduct.getMaxStackSize());
        }

        return true;
    }

    @Override
    public void smelt(BlockWitchesOven.TileEntityWitchesOven oven) {
        ItemStack input = oven.getStackInSlot(INPUT);
        input.stackSize -= this.input.stackSize;
        if (input.stackSize <= 0)
            oven.setInventorySlotContents(INPUT, null);

        ItemStack output = oven.getStackInSlot(OUTPUT);
        if (output == null)
            oven.setInventorySlotContents(OUTPUT, this.output.copy());
        else
            output.stackSize += this.output.stackSize;

        if (this.byProduct == null)
            return;

        if (strictByProduct) {
            if (jarsRequired > 0) {
                ItemStack jars = oven.getStackInSlot(JARS);
                jars.stackSize -= jarsRequired;
                if (jars.stackSize == 0)
                    oven.setInventorySlotContents(JARS, null);
            }

            ItemStack byProduct = oven.getStackInSlot(BYPRODUCT);
            if (byProduct == null)
                oven.setInventorySlotContents(BYPRODUCT, this.byProduct.copy());
            else
                byProduct.stackSize += this.byProduct.stackSize;
        }
        else if (oven.getWorldObj().rand.nextDouble() <= Math.min(ignoreFunnelChance ? byProductChance : byProductChance + ((IWitchOvenTile) oven).getFumeFunnelsChance(), 1)) {
            if (jarsRequired > 0) {
                ItemStack jars = oven.getStackInSlot(JARS);
                if (jars == null || jars.stackSize < jarsRequired)
                    return;

                ItemStack byProduct = oven.getStackInSlot(BYPRODUCT);
                if (byProduct != null && (!byProduct.isItemEqual(this.byProduct) || byProduct.stackSize + this.byProduct.stackSize > this.byProduct.getMaxStackSize()))
                    return;

                jars.stackSize -= jarsRequired;
                if (jars.stackSize == 0) {
                    oven.setInventorySlotContents(JARS, null);
                }

                if (byProduct == null)
                    oven.setInventorySlotContents(BYPRODUCT, this.byProduct.copy());
                else
                    byProduct.stackSize += this.byProduct.stackSize;
            }
            else {
                ItemStack byProduct = oven.getStackInSlot(BYPRODUCT);
                if (byProduct == null)
                    oven.setInventorySlotContents(BYPRODUCT, this.byProduct.copy());
                else if (byProduct.isItemEqual(this.byProduct) && byProduct.stackSize + this.byProduct.stackSize <= this.byProduct.getMaxStackSize())
                    byProduct.stackSize += this.byProduct.stackSize;
            }
        }
    }

}
