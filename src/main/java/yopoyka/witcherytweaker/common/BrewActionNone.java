package yopoyka.witcherytweaker.common;

import com.emoniph.witchery.brewing.*;
import com.emoniph.witchery.brewing.action.BrewAction;
import com.emoniph.witchery.brewing.action.BrewActionList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class BrewActionNone extends BrewAction {
    protected BrewActionNone(BrewItemKey itemKey, BrewNamePart namePart, AltarPower powerCost, Probability baseProbability, boolean createsSplash) {
        super(itemKey, namePart, powerCost, baseProbability, createsSplash);
    }

    protected BrewActionNone(BrewItemKey itemKey, BrewNamePart namePart, AltarPower powerCost, Probability baseProbability, boolean createsSplash, int forcedColor) {
        super(itemKey, namePart, powerCost, baseProbability, createsSplash, forcedColor);
    }

    @Override
    public boolean augmentEffectLevels(EffectLevelCounter paramEffectLevelCounter) {
        return true;
    }

    @Override
    public void augmentEffectModifiers(ModifiersEffect paramModifiersEffect) {

    }

    @Override
    public void prepareSplashPotion(World paramWorld, BrewActionList paramBrewActionList, ModifiersImpact paramModifiersImpact) {

    }

    @Override
    public void prepareRitual(World paramWorld, int paramInt1, int paramInt2, int paramInt3, ModifiersRitual paramModifiersRitual, ItemStack paramItemStack) {

    }

    @Override
    public RitualStatus updateRitual(MinecraftServer paramMinecraftServer, BrewActionList paramBrewActionList, World paramWorld, int paramInt1, int paramInt2, int paramInt3, ModifiersRitual paramModifiersRitual, ModifiersImpact paramModifiersImpact) {
        return RitualStatus.COMPLETE;
    }

    @Override
    public void applyToEntity(World paramWorld, EntityLivingBase paramEntityLivingBase, ModifiersEffect paramModifiersEffect, ItemStack paramItemStack) {

    }

    @Override
    public void applyToBlock(World paramWorld, int paramInt1, int paramInt2, int paramInt3, ForgeDirection paramForgeDirection, int paramInt4, ModifiersEffect paramModifiersEffect, ItemStack paramItemStack) {

    }
}
