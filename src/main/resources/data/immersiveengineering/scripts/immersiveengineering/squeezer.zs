/*
 * Adds a recipe to the squeezer.
 * Squeezer recipes can return an item output, a fluid output or both
 */

//<recipetype:immersiveengineering:squeezer>.addRecipe(recipePath as string, input as IIngredientWithAmount, energy as int, itemOutput as IItemStack)
<recipetype:immersiveengineering:squeezer>.addRecipe("slag_off", <item:immersiveengineering:slag> * 9, 5000, <item:minecraft:dirt>);

//<recipetype:immersiveengineering:squeezer>.addRecipe(recipePath as string, input as IIngredientWithAmount, energy as int, fluidOutput as IFluidStack)
<recipetype:immersiveengineering:squeezer>.addRecipe("the_last_drops", <item:minecraft:coal> * 8, 6000, <fluid:immersiveengineering:creosote> * 250);

//<recipetype:immersiveengineering:squeezer>.addRecipe(recipePath as string, input as IIngredientWithAmount, energy as int, fluidOutput as IFluidStack, itemOutput as IItemStack)
<recipetype:immersiveengineering:squeezer>.addRecipe("pressure_creates_diamonds", <item:minecraft:coal_block> * 8, 6000, <fluid:immersiveengineering:creosote> * 2500, <item:minecraft:diamond>);

/*
 * Removes recipes based on output fluids.
 * The IFluidStack variant checks stack size, whereas the MCFluid variant does not.
 */

//<recipetype:immersiveengineering:squeezer>.removeRecipe(output as IFluidStack);
<recipetype:immersiveengineering:squeezer>.removeRecipe(<fluid:immersiveengineering:plantoil> * 60);

//<recipetype:immersiveengineering:squeezer>.removeRecipe(output as MCFluid);
//<recipetype:immersiveengineering:squeezer>.removeRecipe(<fluid:immersiveengineering:plantoil>.fluid);

/*
 * Removes recipes based on output items.
 */

//<recipetype:immersiveengineering:squeezer>.removeRecipe(output as IItemStack);
<recipetype:immersiveengineering:squeezer>.removeRecipe(<item:immersiveengineering:dust_hop_graphite>);