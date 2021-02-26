/*
 * Adds a recipe to the metal press
 */
//<recipetype:immersiveengineering:metal_press>.addRecipe(recipePath as string, input as IIngredientWithAmount, mold as IItemStack, energy as int, output as IItemStack)
<recipetype:immersiveengineering:metal_press>.addRecipe("copy_manual", <item:minecraft:paper>, <item:immersiveengineering:manual>, 1000, <item:immersiveengineering:manual>);

/*
 * Removes recipes from the Metal Press based on output
 */
//<recipetype:immersiveengineering:metal_press>.removeRecipe(output as IItemStack)
<recipetype:immersiveengineering:metal_press>.removeRecipe(<item:immersiveengineering:empty_casing> * 2);
