package muwa.witcherytweaker.server;

import muwa.witcherytweaker.common.Inject;
import net.minecraft.item.ItemStack;

public interface IWitchOvenTile {
    @Inject.Access(value = "wtw_cookTime", create = true)
    public void setTotalCookTime(int time);

    @Inject.Access(value = "wtw_cookTime", create = true)
    public int getTotalCookTime();

    @Inject.Public
    public int getFumeFunnels();

    @Inject.Public
    public void generateByProduct(ItemStack result);

    @Inject.Public
    public double getFumeFunnelsChance();
}
