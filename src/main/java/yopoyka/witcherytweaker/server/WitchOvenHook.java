package yopoyka.witcherytweaker.server;

import com.emoniph.witchery.blocks.BlockWitchesOven;
import net.minecraft.tileentity.TileEntityFurnace;
import yopoyka.witcherytweaker.common.IOvenRecipe;
import yopoyka.witcherytweaker.common.IWitchOvenTile;
import yopoyka.witcherytweaker.common.OvenRecipe;
import yopoyka.witcherytweaker.common.WitchOvenRecipesSupport;

public class WitchOvenHook {
    public static void updateOven(BlockWitchesOven.TileEntityWitchesOven oven) {
        boolean flag = (oven.furnaceBurnTime > 0);
        boolean update = false;

        if (oven.furnaceBurnTime > 0) {
            oven.furnaceBurnTime--;
        }

        if (!oven.getWorldObj().isRemote) {
            IOvenRecipe recipe = getMatch(oven);
            if (oven.furnaceBurnTime == 0 && recipe != null) {
                oven.currentItemBurnTime = oven.furnaceBurnTime = TileEntityFurnace.getItemBurnTime(oven.getStackInSlot(1));

                if (oven.furnaceBurnTime > 0) {
                    update = true;
                    consumeFuel(oven);
                }
            }

            if (oven.isBurning() && recipe != null) {
                oven.furnaceCookTime++;

                int totalCookTime = recipe.getCookTime(oven);
                ((IWitchOvenTile) oven).setTotalCookTime(totalCookTime);
                if (oven.furnaceCookTime >= totalCookTime) {
                    oven.furnaceCookTime = 0;
                    recipe.smelt(oven);
                    update = true;
                }
            } else {
                oven.furnaceCookTime = 0;
                ((IWitchOvenTile) oven).setTotalCookTime(0);
            }

            if (flag != ((oven.furnaceBurnTime > 0))) {
                update = true;
                BlockWitchesOven.updateWitchesOvenBlockState((oven.furnaceBurnTime > 0), oven.getWorldObj(), oven.xCoord, oven.yCoord, oven.zCoord);
            }
        }


        if (update) {
            oven.getWorldObj().markBlockForUpdate(oven.xCoord, oven.yCoord, oven.zCoord);
        }
    }

    private static void consumeFuel(BlockWitchesOven.TileEntityWitchesOven oven) {
        if (oven.getStackInSlot(1) != null) {
            (oven.getStackInSlot(1)).stackSize--;

            if ((oven.getStackInSlot(1)).stackSize == 0) {
                oven.setInventorySlotContents(1, oven.getStackInSlot(1).getItem().getContainerItem(oven.getStackInSlot(1)));
            }
        }
    }

    public static IOvenRecipe getMatch(BlockWitchesOven.TileEntityWitchesOven oven) {
        for (OvenRecipe value : WitchOvenRecipesSupport.recipes) {
            if (value.canSmelt(oven))
                return value;
        }
        if (IOvenRecipe.DEFAULT.canSmelt(oven))
            return IOvenRecipe.DEFAULT;
        return null;
    }
}
