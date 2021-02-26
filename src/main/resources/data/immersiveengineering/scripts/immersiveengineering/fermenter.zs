/*
 * Adds a recipe to the fermenter.
 * You can provide itemOutput, fluidOutput, or both.
 */
//<recipetype:immersiveengineering:fermenter>.addRecipe(name as string, input as IIngredientWithAmount, energy as int, fluidOutput as IFluidStack)
<recipetype:immersiveengineering:fermenter>.addRecipe("fermenter_extract_water", <item:minecraft:wooden_hoe>, 1000, <fluid:minecraft:water> * 100);
//<recipetype:immersiveengineering:fermenter>.addRecipe(name as string, input as IIngredientWithAmount, energy as int, itemOutput as IItemStack)
<recipetype:immersiveengineering:fermenter>.addRecipe("fermenter_upgrade_hoe", <item:minecraft:wooden_shovel>, 1000, <item:minecraft:stone_shovel>);
//<recipetype:immersiveengineering:fermenter>.addRecipe(name as string, input as IIngredientWithAmount, energy as int, itemOutput as IItemStack, fluidOutput as IFluidStack)
<recipetype:immersiveengineering:fermenter>.addRecipe("fermenter_upgrade_sword", <item:minecraft:wooden_sword>, 1000, <item:minecraft:stone_sword>, <fluid:minecraft:water> * 100);

/*
 * Removes recipes from the fermenter based on output fluid.
 * FluidStack matches the stacksize, whereas fluid only matches the fluid in general.
 */
//<recipetype:immersiveengineering:fermenter>.removeRecipe(output as IFluidStack)
<recipetype:immersiveengineering:fermenter>.removeRecipe(<fluid:immersiveengineering:ethanol> * 20);

//Works, but is commented, because it basically removes all default recipes from the fermenter.
//<recipetype:immersiveengineering:fermenter>.removeRecipe(output as MCFluid)
//<recipetype:immersiveengineering:fermenter>.removeRecipe(<fluid:immersiveengineering:ethanol>.fluid);


/*
 * Removes recipes from the fermenter based on output item.
 */
//<recipetype:immersiveengineering:fermenter>.removeRecipe(output as IItemStack)
//Works, but removes the recipe added above, so this call is commented
//<recipetype:immersiveengineering:fermenter>.removeRecipe(<item:minecraft:stone_shovel>);