package muwa.witcherytweaker.server;

import net.minecraft.item.ItemStack;

public interface IWitchOvenTile {

    public void setTotalCookTime(int time);

    public int getTotalCookTime();

    public int getFumeFunnels();

    public void generateByProduct(ItemStack result);

    public double getFumeFunnelsChance();
}
