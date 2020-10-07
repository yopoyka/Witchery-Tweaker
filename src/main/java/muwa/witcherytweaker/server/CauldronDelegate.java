package muwa.witcherytweaker.server;

import com.emoniph.witchery.brewing.*;
import com.emoniph.witchery.brewing.action.BrewAction;
import com.emoniph.witchery.brewing.action.BrewActionRitualRecipe;
import minetweaker.MineTweakerAPI;
import minetweaker.MineTweakerImplementationAPI;
import minetweaker.api.item.IItemStack;
import minetweaker.api.minecraft.MineTweakerMC;
import muwa.witcherytweaker.Witweaker;
import muwa.witcherytweaker.common.CauldronRecipesSupport;
import muwa.witcherytweaker.common.util.ReflectUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CauldronDelegate extends CauldronRecipesSupport.Delegate {
    protected static Field recipesField;
    protected static Set<BrewItemKey> elements = new HashSet<>();

    static {
        try {
            recipesField = BrewActionRitualRecipe.class.getDeclaredField("recipes");
            ReflectUtil.makeAccessible(recipesField);
        } catch (NoSuchFieldException e) {
            Witweaker.log.catching(e);
        }
    }

    @Override
    public void add(IItemStack result, IItemStack last, IItemStack[][] recipes, int powerCost) throws Exception {
        ItemStack lastStack = (ItemStack) last.getInternal();

        if (getIngreds().containsKey(BrewItemKey.fromStack(lastStack))) {
            throw new Exception("Brew Item Key already registered!");
        }

        if (!CauldronRecipesSupport.ignoreWarning) {
            for (IItemStack[] recipe : recipes) {
                List<ItemStack> items = Stream.concat(Arrays.stream(recipe).map(i -> (ItemStack) i.getInternal()), Stream.of(lastStack))
                        .collect(Collectors.toList());

                if (items.stream().map(BrewItemKey::fromStack).distinct().count() != recipe.length + 1)
                    throw new Exception("Same item occurs more than once. This recipe is not going to work!");

                NBTTagCompound nbtRoot = new NBTTagCompound();
                NBTTagList nbtItems = new NBTTagList();
                nbtRoot.setTag("Items", nbtItems);

                for (ItemStack item : items) {
                    BrewAction action = WitcheryBrewRegistry.INSTANCE.getActionForItemStack(item);
                    if (action == null) {
                        nbtItems.appendTag(item.writeToNBT(new NBTTagCompound()));
                    }
                    else if (!WitcheryBrewRegistry.INSTANCE.canAdd(nbtRoot, action, true)) {
                        throw new Exception("This recipe is not going to work! At: " + MineTweakerMC.getIItemStack(item));
                    }
                }
            }
        }

        Arrays.stream(recipes)
                .flatMap(Arrays::stream)
                .map(s -> (ItemStack) s.getInternal())
                .map(BrewItemKey::fromStack)
                .forEach(elements::add);

        super.add(result, last, recipes, powerCost);
    }

    @Override
    public void reload() {
        super.reload();
        Hashtable<BrewItemKey, BrewAction> ingreds = getIngreds();
        elements.forEach(ingreds::remove);
        elements.clear();
    }

    @Override
    public void postReload() {
        super.postReload();
        Hashtable<BrewItemKey, BrewAction> ingreds = getIngreds();
        elements.forEach(brewItemKey -> ingreds.put(brewItemKey, new BrewActionNone(
                brewItemKey,
                new BrewNamePart("none"),
                new AltarPower(0),
                Probability.CERTAIN,
                true
        )));
    }
}
