package muwa.witcherytweaker.common.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class Util {
    public static void decrStackInSlot(IInventory inv, int slot, int amount) {
        final ItemStack stack = inv.getStackInSlot(slot);
        if (stack != null) {
            stack.stackSize -= amount;
            if (stack.stackSize < 1) {
                inv.setInventorySlotContents(slot, null);
            }
        }
    }
}
