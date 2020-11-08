package yopoyka.witcherytweaker.common;

import com.emoniph.witchery.blocks.BlockWitchesOven;
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

    public int getCookTime(BlockWitchesOven.TileEntityWitchesOven oven);

    public boolean canSmelt(BlockWitchesOven.TileEntityWitchesOven oven);

    public void smelt(BlockWitchesOven.TileEntityWitchesOven oven);
}
