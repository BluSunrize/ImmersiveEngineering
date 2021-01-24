/*
 * Adds a recipe to the crusher.
 * The additionalOutputs arguments is variadic, that means you can provide 0 or more additionalOutputs.
 */
//<recipetype:immersiveengineering:crusher>.addRecipe(recipePath as string, input as IIngredient, energy as int, mainOutput as IItemStack, additionalOutputs as MCWeightedItemStack...)
<recipetype:immersiveengineering:crusher>.addRecipe("tnt_discharge", <item:minecraft:tnt>, 500, <item:minecraft:gunpowder> * 4, <item:minecraft:coal> % 50, <item:minecraft:diamond> % 1);

/*
 * Removes crusher recipes based on their output.
 * Removes all recipes where any output (regardless of primary or secondary) matches the provided IIngredient
 */
//<recipetype:immersiveengineering:crusher>.removeRecipe(output as IIngredient)
<recipetype:immersiveengineering:crusher>.removeRecipe(<item:immersiveengineering:dust_iron>);
//Also works here, but is put in a comment, since it removes many recipes
//<recipetype:immersiveengineering:crusher>.removeRecipe(<tag:items:forge:dusts>);