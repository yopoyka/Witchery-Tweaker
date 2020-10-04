package muwa.witcherytweaker;

import com.emoniph.witchery.Witchery;
import com.emoniph.witchery.blocks.BlockWitchesOven;
import com.emoniph.witchery.util.Config;
import com.emoniph.witchery.util.Log;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementation;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.item.*;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ZenClass("mods.witchery.witchOven")
public abstract class WitchOvenRecipe {
    public static final int
            INPUT = 0,
            FUEL = 1,
            OUTPUT = 2,
            BYPRODUCT = 3,
            JARS = 4,
            defaultCookTime = 180;
    public static final double defaultChance = 0.3;
    public static List<WitchOvenRecipe> recipes = new ArrayList<>();

    static {
        MineTweakerImplementationAPI.onReloadEvent(reloadEvent -> {
            recipes.clear();
            System.out.println("reloading");
        });
    }

    @ZenMethod
    public static Impl add(IItemStack input, IItemStack output) {
        Impl recipe = new Impl(
                (ItemStack) input.getInternal(),
                (ItemStack) output.getInternal(),
                null,
                defaultCookTime,
                defaultChance,
                0,
                false,
                false,
                false
        );
        recipes.add(recipe);
        return recipe;
    }

    @ZenMethod
    public static Impl add(IItemStack input, IItemStack output, IItemStack byProduct) {
        Impl recipe = new Impl(
                (ItemStack) input.getInternal(),
                (ItemStack) output.getInternal(),
                (ItemStack) byProduct.getInternal(),
                defaultCookTime,
                defaultChance,
                0,
                false,
                false,
                false
        );
        recipes.add(recipe);
        return recipe;
    }

    @ZenClass("mods.witchery.OvenRecipe")
    public static class Impl extends WitchOvenRecipe {
        boolean ignoreFunnelChance;
        boolean ignoreFunnelSpeed;
        int cookTime;
        int cookTimeFraction;
        ItemStack input;
        ItemStack output;
        ItemStack byProduct;
        double byProductChance;
        boolean strictByProduct;
        int jarsRequired;

        public Impl(
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
        public Impl cookTime(int cookTime) {
            this.cookTime = cookTime;
            return this;
        }

        @ZenMethod
        public Impl chance(double byProductChance) {
            this.byProductChance = byProductChance;
            return this;
        }

        @ZenMethod
        public Impl defaultChance() {
            this.byProductChance = defaultChance;
            return this;
        }

        @ZenMethod
        public Impl defaultCookTime() {
            this.cookTime = defaultCookTime;
            return this;
        }

        @ZenMethod
        public Impl jars(int jars) {
            jarsRequired = jars;
            return this;
        }

        @ZenMethod
        public Impl strict() {
            strictByProduct = true;
            return this;
        }

        @ZenMethod
        public Impl strict(boolean strict) {
            strictByProduct = strict;
            return this;
        }

        @ZenMethod
        public Impl ignoreFunnelChance() {
            ignoreFunnelChance = true;
            return this;
        }

        @ZenMethod
        public Impl ignoreFunnelChance(boolean ignore) {
            ignoreFunnelChance = ignore;
            return this;
        }

        @ZenMethod
        public Impl ignoreFunnelSpeed() {
            ignoreFunnelSpeed = true;
            return this;
        }

        @ZenMethod
        public Impl ignoreFunnelSpeed(boolean ignore) {
            ignoreFunnelSpeed = ignore;
            return this;
        }

        @Override
        public int getCookTime(BlockWitchesOven.TileEntityWitchesOven oven) {
            return ignoreFunnelSpeed ? cookTime : cookTime - cookTimeFraction * WitchOvenRecipe.getFumeFunnels(oven);
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
            else if (oven.getWorld().rand.nextDouble() <= Math.min(ignoreFunnelChance ? byProductChance : byProductChance + getFumeFunnelsChance(oven), 1)) {
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

    public static WitchOvenRecipe DEFAULT = new WitchOvenRecipe() {
        @Override
        public int getCookTime(BlockWitchesOven.TileEntityWitchesOven oven) {
            return 180 - 20 * getFumeFunnels(oven);
        }

        @Override
        public boolean canSmelt(BlockWitchesOven.TileEntityWitchesOven oven) {
            ItemStack stack = oven.getStackInSlot(0);
            if (stack == null)
                return false;

            stack = FurnaceRecipes.instance().getSmeltingResult(stack);
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
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
            ItemStack resultStack = oven.getStackInSlot(2);

            if (resultStack == null)
                oven.setInventorySlotContents(2, result.copy());
            else if (resultStack.isItemEqual(result))
                resultStack.stackSize += result.stackSize;

            generateByProduct(oven, result);

            stack.stackSize--;
            if (stack.stackSize <= 0)
                oven.setInventorySlotContents(0, null);
        }
    };

    @Nullable
    public static WitchOvenRecipe getMatch(BlockWitchesOven.TileEntityWitchesOven oven) {
        for (WitchOvenRecipe value : recipes) {
            if (value.canSmelt(oven))
                return value;
        }
        if (DEFAULT.canSmelt(oven))
            return DEFAULT;
        return null;
    }

    public abstract int getCookTime(BlockWitchesOven.TileEntityWitchesOven oven);

    public abstract boolean canSmelt(BlockWitchesOven.TileEntityWitchesOven oven);

    public abstract void smelt(BlockWitchesOven.TileEntityWitchesOven oven);

    private static int getFumeFunnels(BlockWitchesOven.TileEntityWitchesOven oven) {
        int funnels = 0;
        int meta = oven.getWorld().getBlockMetadata(oven.xCoord, oven.yCoord, oven.zCoord);
        switch (meta) {
            case 2:
            case 3:
                funnels += isFumeFunnel(oven, oven.xCoord - 1, oven.yCoord, oven.zCoord, meta) ? 1 : 0;
                funnels += isFumeFunnel(oven, oven.xCoord + 1, oven.yCoord, oven.zCoord, meta) ? 1 : 0;
                break;
            case 4:
            case 5:
                funnels += isFumeFunnel(oven, oven.xCoord, oven.yCoord, oven.zCoord - 1, meta) ? 1 : 0;
                funnels += isFumeFunnel(oven, oven.xCoord, oven.yCoord, oven.zCoord + 1, meta) ? 1 : 0;
                break;
        }
        funnels += isFumeFunnel(oven, oven.xCoord, oven.yCoord + 1, oven.zCoord, meta) ? 1 : 0;
        return funnels;
    }

    private static boolean isFumeFunnel(BlockWitchesOven.TileEntityWitchesOven oven, int xCoord, int yCoord, int zCoord, int meta) {
        Block block = oven.getWorld().getBlock(xCoord, yCoord, zCoord);
        return ((block == Witchery.Blocks.OVEN_FUMEFUNNEL || block == Witchery.Blocks.OVEN_FUMEFUNNEL_FILTERED) && oven.getWorld().getBlockMetadata(xCoord, yCoord, zCoord) == meta);
    }

    private static void generateByProduct(BlockWitchesOven.TileEntityWitchesOven oven, ItemStack itemstack) {
        try {
            double BASE_CHANCE = 0.3D;
            double funnels = getFumeFunnelsChance(oven);

            Log.instance().debug("" + oven.getStackInSlot(0) + ": " + oven.getStackInSlot(0).getItem().getUnlocalizedName());



            if (oven.getWorld().rand.nextDouble() <= Math.min(0.3D + funnels, 1.0D) && oven.getStackInSlot(4) != null)
            {
                if (oven.getStackInSlot(0).getItem() == Item.getItemFromBlock(Blocks.sapling) && oven.getStackInSlot(0).getMetadata() != 3) {

                    switch (oven.getStackInSlot(0).getMetadata()) {
                        case 0:
                            createByProduct(oven, Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(1));
                            break;
                        case 1:
                            createByProduct(oven, Witchery.Items.GENERIC.itemHintOfRebirth.createStack(1));
                            break;
                        case 2:
                            createByProduct(oven, Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(1));
                            break;
                    }

                } else if (oven.getStackInSlot(0).getItem() == Item.getItemFromBlock(Witchery.Blocks.SAPLING)) {

                    switch (oven.getStackInSlot(0).getMetadata()) {
                        case 0:
                            createByProduct(oven, Witchery.Items.GENERIC.itemWhiffOfMagic.createStack(1));
                            break;
                        case 1:
                            createByProduct(oven, Witchery.Items.GENERIC.itemReekOfMisfortune.createStack(1));
                            break;
                        case 2:
                            createByProduct(oven, Witchery.Items.GENERIC.itemOdourOfPurity.createStack(1));
                            break;
                    }
                } else if (oven.getStackInSlot(0).getUnlocalizedName().equals("tile.bop.saplings") && oven.getStackInSlot(0).getMetadata() == 6) {


                    createByProduct(oven, Witchery.Items.GENERIC.itemHintOfRebirth.createStack(1));
                } else if (oven.getStackInSlot(0).hasTagCompound() && oven.getStackInSlot(0).getTagCompound().hasKey("Genome")) {

                    NBTBase tag = oven.getStackInSlot(0).getTagCompound().getTag("Genome");
                    if (tag != null && tag instanceof NBTTagCompound) {
                        NBTTagCompound compound = (NBTTagCompound)tag;
                        if (compound.hasKey("Chromosomes") && compound.getTag("Chromosomes") instanceof NBTTagList) {

                            NBTTagList list = compound.getTagList("Chromosomes", 10);

                            if (list != null && list.tagCount() > 0) {
                                NBTTagCompound nBTTagCompound = list.getCompoundTagAt(0);
                                if (nBTTagCompound != null && nBTTagCompound instanceof NBTTagCompound) {
                                    NBTTagCompound chromosome = nBTTagCompound;
                                    if (chromosome.hasKey("UID0")) {
                                        String treeType = chromosome.getString("UID0");
                                        if (treeType != null) {
                                            Log.instance().debug("Forestry tree: " + treeType);
                                            if (treeType.equals("forestry.treeOak")) {
                                                createByProduct(oven, Witchery.Items.GENERIC.itemExhaleOfTheHornedOne.createStack(1));
                                            }
                                            else if (treeType.equals("forestry.treeSpruce")) {
                                                createByProduct(oven, Witchery.Items.GENERIC.itemHintOfRebirth.createStack(1));
                                            }
                                            else if (treeType.equals("forestry.treeBirch")) {
                                                createByProduct(oven, Witchery.Items.GENERIC.itemBreathOfTheGoddess.createStack(1));
                                            }

                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {

                    createByProduct(oven, Witchery.Items.GENERIC.itemFoulFume.createStack(1));
                }
            }
        } catch (Throwable e) {
            Log.instance().warning(e, "Exception occured while generating a by product from a witches oven");
        }
    }

    private static void createByProduct(BlockWitchesOven.TileEntityWitchesOven oven, ItemStack byProduct) {
        int BY_PRODUCT_INDEX = 3;
        if (oven.getStackInSlot(3) == null) {
            oven.setInventorySlotContents(3, byProduct);

            if (--(oven.getStackInSlot(4)).stackSize <= 0) {
                oven.setInventorySlotContents(4, null);
            }
        } else if (oven.getStackInSlot(3).isItemEqual(byProduct) && (oven.getStackInSlot(3)).stackSize + byProduct.stackSize < oven.getStackInSlot(3).getMaxStackSize()) {


            (oven.getStackInSlot(3)).stackSize += byProduct.stackSize;

            if (--(oven.getStackInSlot(4)).stackSize <= 0) {
                oven.setInventorySlotContents(4, null);
            }
        }
    }

    private static double getFumeFunnelsChance(BlockWitchesOven.TileEntityWitchesOven oven) {
        double funnels = 0.0D;
        switch (oven.getWorld().getBlockMetadata(oven.xCoord, oven.yCoord, oven.zCoord)) {
            case 2:
                funnels += getFumeFunnelChance(oven, oven.xCoord + 1, oven.yCoord, oven.zCoord, 2);
                funnels += getFumeFunnelChance(oven, oven.xCoord - 1, oven.yCoord, oven.zCoord, 2);
                break;
            case 3:
                funnels += getFumeFunnelChance(oven, oven.xCoord + 1, oven.yCoord, oven.zCoord, 3);
                funnels += getFumeFunnelChance(oven, oven.xCoord - 1, oven.yCoord, oven.zCoord, 3);
                break;
            case 4:
                funnels += getFumeFunnelChance(oven, oven.xCoord, oven.yCoord, oven.zCoord + 1, 4);
                funnels += getFumeFunnelChance(oven, oven.xCoord, oven.yCoord, oven.zCoord - 1, 4);
                break;
            case 5:
                funnels += getFumeFunnelChance(oven, oven.xCoord, oven.yCoord, oven.zCoord + 1, 5);
                funnels += getFumeFunnelChance(oven, oven.xCoord, oven.yCoord, oven.zCoord - 1, 5);
                break;
        }
        return funnels;
    }

    private static double getFumeFunnelChance(BlockWitchesOven.TileEntityWitchesOven oven, int x, int y, int z, int meta) {
        Block block = oven.getWorld().getBlock(x, y, z);
        if (block == Witchery.Blocks.OVEN_FUMEFUNNEL) {
            if (oven.getWorld().getBlockMetadata(x, y, z) == meta) {
                return 0.25D;
            }
        } else if (block == Witchery.Blocks.OVEN_FUMEFUNNEL_FILTERED &&
                oven.getWorld().getBlockMetadata(x, y, z) == meta) {
            return (Config.instance()).doubleFumeFilterChance ? 0.8D : 0.3D;
        }


        return 0.0D;
    }
}
