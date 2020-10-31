package yopoyka.witcherytweaker.server;

import com.emoniph.witchery.blocks.BlockWitchesOven;
import yopoyka.witcherytweaker.common.WitchOvenRecipes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

public interface IOvenRecipe {
    public static IOvenRecipe DEFAULT = new IOvenRecipe() {
        @Override
        public int getCookTime(BlockWitchesOven.TileEntityWitchesOven oven) {
            return 180 - 20 * ((IWitchOvenTile) oven).getFumeFunnels();
        }

        @Override
        public boolean canSmelt(BlockWitchesOven.TileEntityWitchesOven oven) {
            ItemStack stack = oven.getStackInSlot(0);
            if (stack == null)
                return false;

            stack = FurnaceRecipes.smelting().getSmeltingResult(stack);
            if (stack == null)
                return false;

            ItemStack resultStack = oven.getStackInSlot(2);
            if (resultStack == null)
                return true;
            if (!resultStack.isItemEqual(stack))
                return false;

            int resultCount = resultStack.stackSize + stack.stackSize;
            return resultCount <= oven.getInventoryStackLimit() && resultCount <= resultStack.getMaxStackSize();
        }

        @Override
        public void smelt(BlockWitchesOven.TileEntityWitchesOven oven) {
            ItemStack stack = oven.getStackInSlot(0);
            ItemStack result = FurnaceRecipes.smelting().getSmeltingResult(stack);
            ItemStack resultStack = oven.getStackInSlot(2);

            if (resultStack == null)
                oven.setInventorySlotContents(2, result.copy());
            else if (resultStack.isItemEqual(result))
                resultStack.stackSize += result.stackSize;

            ((IWitchOvenTile) oven).generateByProduct(result);

            stack.stackSize--;
            if (stack.stackSize <= 0)
                oven.setInventorySlotContents(0, null);
        }
    };

    public static class ServerImpl extends WitchOvenRecipes.Impl implements IOvenRecipe {
        public static final int
                INPUT = 0,
                FUEL = 1,
                OUTPUT = 2,
                BYPRODUCT = 3,
                JARS = 4;

        public ServerImpl(ItemStack input, ItemStack output, ItemStack byProduct, int cookTime, double byProductChance, int jarsRequired, boolean strictByProduct, boolean ignoreFunnelChance, boolean ignoreFunnelSpeed) {
            super(input, output, byProduct, cookTime, byProductChance, jarsRequired, strictByProduct, ignoreFunnelChance, ignoreFunnelSpeed);
        }

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

    public int getCookTime(BlockWitchesOven.TileEntityWitchesOven oven);

    public boolean canSmelt(BlockWitchesOven.TileEntityWitchesOven oven);

    public void smelt(BlockWitchesOven.TileEntityWitchesOven oven);
}
