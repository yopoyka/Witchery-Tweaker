package muwa.witcherytweaker.server;

import com.emoniph.witchery.blocks.BlockDistillery;
import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.crafting.DistilleryRecipes;
import muwa.witcherytweaker.common.IDistilleryRecipe;
import net.minecraft.item.ItemStack;

import static muwa.witcherytweaker.common.util.Util.decrStackInSlot;

public class DistilleryHook {
    public static void updateDistillery(BlockDistillery.TileEntityDistillery tile) {
        if (tile.getWorldObj().isRemote) return;
        IDistilleryTile tileHook = (IDistilleryTile) tile;
        boolean cooking = tile.furnaceCookTime > 0;
        boolean powered = tile.powerLevel > 0;
        final DistilleryRecipes.DistilleryRecipe activeRecipe = tile.getActiveRecipe();
        final IDistilleryRecipe recipeHook = (IDistilleryRecipe) activeRecipe;

        boolean markForUpdate = false;
        boolean canSmelt;

        if (activeRecipe == null) {
            canSmelt = false;
        }
        else {
            ItemStack[] itemstacks = activeRecipe.getOutputs();

            canSmelt = true;

            for (int i = 0; i < itemstacks.length; ++i) {
                ItemStack current = tile.getStackInSlot(i + 3);
                if (itemstacks[i] != null && current != null && current.isItemEqual(itemstacks[i])) {
                    int newSize = current.stackSize + itemstacks[i].stackSize;
                    if (newSize > tile.getInventoryStackLimit() || newSize > current.getMaxStackSize()) {
                        canSmelt = false;
                        break;
                    }
                }
            }
        }

        if (!canSmelt) {
            if (tileHook.getTicks() % 40L == 0L) {
                updatePowerLevel(tile, false);
            }

            tile.furnaceCookTime = 0;
        } else {
            final IPowerSource powerSource = updatePowerLevel(tile, true);
            if (powerSource != null && powerSource.consumePower(0.6F)) {
                ++tile.furnaceCookTime;
                final int totalCookTime = recipeHook.getCookTime();
                tileHook.setTotalCookTime(totalCookTime);
                if (tile.furnaceCookTime >= totalCookTime) {
                    tile.furnaceCookTime = 0;

                    final ItemStack[] itemstacks = activeRecipe.getOutputs();

                    for(int i = 0; i < itemstacks.length; ++i) {
                        int furnaceIndex = i + 3;
                        if (itemstacks[i] != null) {
                            if (tile.getStackInSlot(furnaceIndex) == null) {
                                tile.setInventorySlotContents(furnaceIndex, itemstacks[i].copy());
                            } else if (tile.getStackInSlot(furnaceIndex).isItemEqual(itemstacks[i])) {
                                tile.getStackInSlot(furnaceIndex).stackSize += itemstacks[i].stackSize;
                            }
                        }
                    }

                    decrStackInSlot(tile, 0, 1);
                    decrStackInSlot(tile, 1, 1);
                    decrStackInSlot(tile, 2, activeRecipe.getJars());

                    markForUpdate = true;
                }
            } else {
                tile.powerLevel = 0;
                tileHook.setTotalCookTime(0);
            }
        }

        if (cooking != tile.furnaceCookTime > 0) {
            BlockDistillery.updateDistilleryBlockState(tile.furnaceCookTime > 0 && tile.powerLevel > 0, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
            tileHook.setLastUpdate(tileHook.getTicks());
            tileHook.setNeedUpdate(false);
        } else if (powered != tile.powerLevel > 0) {
            if (tileHook.getTicks() - tileHook.getLastUpdate() <= 20L) {
                tileHook.setNeedUpdate(true);
            } else {
                BlockDistillery.updateDistilleryBlockState(tile.furnaceCookTime > 0 && tile.powerLevel > 0, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
                tileHook.setLastUpdate(tileHook.getTicks());
                tileHook.setNeedUpdate(false);
            }
        } else if (tileHook.getNeedUpdate() && tileHook.getTicks() - tileHook.getLastUpdate() > 20L) {
            BlockDistillery.updateDistilleryBlockState(tile.furnaceCookTime > 0 && tile.powerLevel > 0, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
            tileHook.setLastUpdate(tileHook.getTicks());
            tileHook.setNeedUpdate(false);
        }

        if (markForUpdate) {
            tile.getWorldObj().markBlockForUpdate(tile.xCoord, tile.yCoord, tile.zCoord);
        }
    }

    private static IPowerSource updatePowerLevel(BlockDistillery.TileEntityDistillery tile, boolean setNull) {
        final IDistilleryTile tileHook = (IDistilleryTile) tile;
        final IPowerSource powerSource = tileHook.getPowerSource();
        if (powerSource != null && !powerSource.isLocationEqual(tileHook.getPowerSourceCoord())) {
            tileHook.setPowerSourceCoord(powerSource.getLocation());
        }
        else if (setNull) {
            tileHook.setPowerSourceCoord(null);
        }

        tile.powerLevel = powerSource == null ? 0 : 1;
        return powerSource;
    }
}
