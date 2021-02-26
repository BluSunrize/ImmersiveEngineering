/*
 * Adds a new recipe to the Arc furnace
 */

//<recipetype:immersiveengineering:arc_furnace>.addRecipe(recipePath as string, mainIngredient as IIngredientWithAmount, additives as IIngredientWithAmount[], time as int, energy as int, outputs as IItemStack[], slag as IItemStack = <item:minecraft:air>) as void
<recipetype:immersiveengineering:arc_furnace>.addRecipe("coal_to_bedrock", <item:minecraft:coal_block> * 2, [<item:minecraft:diamond> * 1, <tag:items:minecraft:wool>], 2000, 100000, [<item:minecraft:bedrock>], <item:minecraft:gold_nugget>);

/*
 * Removes a recipe based on its output.
 * Removes any recipes where at least one of the output matches the output provided.
 * If these checks should also take the slag item into account, set the coresponding flag to true.
 * 
 * Cannot remove Recycling Recipes!
 */

//<recipetype:immersiveengineering:arc_furnace>.removeRecipe(output as IItemStack, checkSlag as bool = false);
<recipetype:immersiveengineering:arc_furnace>.removeRecipe(<item:immersiveengineering:slag>, true);
<recipetype:immersiveengineering:arc_furnace>.removeRecipe(<item:minecraft:iron_ingot> * 3);
