/*
 * Adds a recipe to the sawmill.
 * Sawmill recipes consist of two steps, stripping and sawing.
 * You can skip the stripping process and only add a recipe that is for the sawing.
 * If you add saw-only recipes then the recipe will do nothing, if you have no sawblade inserted.
 */

//Sawing-only recipe
//Does nothing if the sawblade is not present
//<recipetype:immersiveengineering:sawmill>.addRecipe(recipePath as string, input as IIngredient, energy as int, output as IItemStack, outputSecondaries as IItemStack[])
<recipetype:immersiveengineering:sawmill>.addRecipe("splitting_bones", <item:minecraft:bone_block>, 1000, <item:minecraft:bone> * 5, [<item:minecraft:bone_meal> * 2]);

//Stripping and Sawing recipe
//If sawblade present, returns grass, stick and sawdust
//If sawblade not present, returns grass and dead bush
//<recipetype:immersiveengineering:sawmill>.addRecipe(recipePath as string, input as IIngredient, energy as int, strippedOutput as IItemStack, strippedOutputSecondaries as IItemStack[], output as IItemStack, outputSecondaries as IItemStack[])
<recipetype:immersiveengineering:sawmill>.addRecipe("shredding_seeds", <tag:items:minecraft:saplings>, 1200, <item:minecraft:dead_bush>, [<item:minecraft:grass>], <item:minecraft:stick> * 2, [<item:immersiveengineering:dust_wood>]);


/*
 * Removes recipes from the sawmill based on the recipe's results.
 * Does not distinguish between primary or secondary outputs, nor between stripping and sawing results.
 * Removes all recipes where the provided output matches any possible output.
 */
//<recipetype:immersiveengineering:sawmill>.removeRecipe(output as IItemStack);
<recipetype:immersiveengineering:sawmill>.removeRecipe(<item:minecraft:oak_planks>);
//<recipetype:immersiveengineering:sawmill>.removeRecipe(<item:immersiveengineering:dust_wood>);