/*
 * Adds a recipe to the Refinery
 */
//<recipetype:immersiveengineering:refinery>.addRecipe(recipePath as string, fluidInput1 as MCTagWithAmount<MCFluid>, fluidInput2 as MCTagWithAmount<MCFluid>, energy as int, output as IFluidStack)
<recipetype:immersiveengineering:refinery>.addRecipe("refine_herbicide", <tag:fluids:minecraft:water> * 10, <tag:fluids:forge:ethanol> * 1, 1000, <fluid:immersiveengineering:herbicide> * 10);


/*
 * Removes recipes based on the output fluid.
 * The IFluidStack version checks stacksizes, the MCFluid one does not.
 */
//<recipetype:immersiveengineering:refinery>.removeRecipe(output as IFluidStack);
//<recipetype:immersiveengineering:refinery>.removeRecipe(<fluid:immersiveengineering:biodiesel> * 16);

//<recipetype:immersiveengineering:refinery>.removeRecipe(output as MCFluid);
//<recipetype:immersiveengineering:refinery>.removeRecipe(<fluid:immersiveengineering:biodiesel>.fluid);

