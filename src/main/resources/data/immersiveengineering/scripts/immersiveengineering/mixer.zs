/*
 * Adds a recipe to the Mixer.
 *
 * Mixer recipes will always convert 1mB of the input fluid to 1mB of the output fluid.
 * The `amount` parameter specifies for how many mB the given ingredients last
 */
//<recipetype:immersiveengineering:mixer>.addRecipe(recipePath as string, fluidInput as MCTag<MCFluid>, inputItems as IIngredientWithAmount[], energy as int, output as MCFluid, amount as int)
<recipetype:immersiveengineering:mixer>.addRecipe("grow_creosote_oil", <tag:fluids:minecraft:water>, [<item:minecraft:oak_sapling>, <item:minecraft:bone_meal> * 4, <item:immersiveengineering:creosote_bucket>], 5000, <fluid:immersiveengineering:creosote>, 8000);


/*
 * Removes recipes based on the output fluid.
 * Cannot remove potion recipes!
 * The IFluidStack version checks stacksizes, the MCFluid one does not.
 */
//<recipetype:immersiveengineering:mixer>.removeRecipe(output as IFluidStack);
<recipetype:immersiveengineering:mixer>.removeRecipe(<fluid:immersiveengineering:concrete> * 500);
//<recipetype:immersiveengineering:mixer>.removeRecipe(output as MCFluid);
<recipetype:immersiveengineering:mixer>.removeRecipe(<fluid:immersiveengineering:concrete>.fluid);

// You could remove all potion recipes by removing the mixer_potion_list recipe by its name.
//<recipetype:immersiveengineering:mixer>.removeByName("immersiveengineering:mixer_potion_list");
