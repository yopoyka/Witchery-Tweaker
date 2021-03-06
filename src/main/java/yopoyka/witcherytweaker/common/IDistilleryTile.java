package yopoyka.witcherytweaker.common;

import com.emoniph.witchery.common.IPowerSource;
import com.emoniph.witchery.util.Coord;

public interface IDistilleryTile {
    @Inject.Access(value = "wtw_cookTime", create = true)
    public int getTotalCookTime();

    @Inject.Access(value = "wtw_cookTime", create = true)
    public void setTotalCookTime(int value);

    @Inject.Access
    public Coord getPowerSourceCoord();

    @Inject.Access
    public void setPowerSourceCoord(Coord coord);

    @Inject.Owner("com.emoniph.witchery.blocks.TileEntityBase")
    @Inject.Access
    public long getTicks();

    @Inject.Access
    public long getLastUpdate();

    @Inject.Access
    public void setLastUpdate(long value);

    @Inject.Access
    public boolean getNeedUpdate();

    @Inject.Access
    public void setNeedUpdate(boolean value);

    @Inject.Public
    public IPowerSource getPowerSource();
}
