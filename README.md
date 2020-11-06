## About
This is extension Minecraft mod to use with [Witchery](https://www.curseforge.com/minecraft/mc-mods/witchery) and [Minetweaker3](https://www.curseforge.com/minecraft/mc-mods/minetweaker3).
It extends various Witchery features.
NEI support included.

Current list
 - Custom Witch's Oven recipes.
 - Custom Kettle recipes.
 - Custom Distillery recipes.
 - Custom Cauldron recipes (there is a catch. see Cauldron section).

This mod initially was designed to have strong server-client separation that's why it may look weird.

## How to
All configuration done inside Minetweaker scripts.
All methods currently support ingredients as `ItemsStack` only.
#### Witch's Oven
`mods.witchery.witchOven` oven object.
```
// disables default oven smelting recipes
mods.witchery.witchOven.removeDefault();

// enables default oven smelting recipes
mods.witchery.witchOven.addDefault();

// mods.witchery.witchOven.add(input, output);
// mods.witchery.witchOven.add(input, output, byproduct);

// The newly added recipe is configurable from mods.witchery.OvenRecipe class
mods.witchery.witchOven.add(<minecraft:dirt>, <minecraft:diamond>)
    .cookTime(100) // sets cook time in ticks. default is 180
    .time(100) // same as cookTime
    .defaultCookTime() // sets cook time to default value
    .defaultTime() // same as defaultCookTime
    .chance(0.5) // byproduct chance. default is 0.3 - 30%
    .defaultChance() // resets byproduct chance to default value
    .jars(3) // clay jars required for this recipe
    .strict() // there must be space for byproduct for recipe to proceed. default is false
    .strict(false) // if false items will be smelting even if byproduct slot is full
    .ignoreFunnelChance() // fume funnels byproduct boost will be ignored
    .ignoreFunnelChance(true) // parameterized version
    .ignoreFunnelSpeed() // fume funnels speed boost will be ignored
    .ignoreFunnelSpeed(true); // parameterized version
```
#### Kettle
`mods.witchery.kettle` kettle object.

Inputs array must be of 6 (six) elements.
```
mods.witchery.kettle.add(output, [inputs]);
mods.witchery.kettle.add(output, [inputs], (float) power); // required altar power
mods.witchery.kettle.remove(output); // removes first matched recipe
mods.witchery.kettle.removeAll(output); // removes all matched recipes
mods.witchery.kettle.get(output); // returns first matched recipe
mods.witchery.kettle.getAll(output); // returns list af all matched recipes

// kettle recipe also has a wrapper class named mods.witchery.KettleRecipe
mods.witchery.kettle.add(<minecraft:diamond>, [<minecraft:dirt>, <minecraft:dirt>, <minecraft:dirt>,
                                                <minecraft:dirt>, <minecraft:dirt>, <minecraft:dirt>], 150)
    .setTranslationKey("key") // translation key for book entry
    .power(1000) // float value. required altar power
    .color(0xFFFFFF) // integer color value
    .hatBonus(10) // integer value
    .familiar(2) // integer value. familiar type
    .dimension(0) // allowed dimension. default is 0 - overworld
    .inBook(true); // whether or not should this recipe be displayed in book
```
#### Witch's Cauldron
`mods.witchery.cauldron` cauldron object.

Warnings will be printed for invalid recipes including location inside the recipe.
This documentation section will be extended later.
```
mods.witchery.cauldron.ingoreWarning(); // ignore invalid recipes warning
mods.witchery.cauldron.ingoreWarning(false);

mods.witchery.cauldron.add(result, [items]);
mods.witchery.cauldron.add(result, [items], (int) power);
mods.witchery.cauldron.add(result, last, [[recipes]]);
mods.witchery.cauldron.add(result, last, [[recipes]], power);
```
#### Distillery
`mods.witchery.distillery` distillery object.
```
mods.witchery.distillery.add([input], [outup], (int) jars);
mods.witchery.distillery.add([input], [outup], (int) jars, (int) cookTime); // default cook time is 800 ticks
```