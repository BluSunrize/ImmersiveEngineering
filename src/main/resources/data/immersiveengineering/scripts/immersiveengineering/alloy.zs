/*
 * Adds a new recipe to the Alloy Kiln
 */

//<recipetype:immersiveengineering:alloy>.addRecipe(recipePath as string, inputA as IIngredientWithAmount, inputB as IIngredientWithAmount, time as int, output as IItemStack)
<recipetype:immersiveengineering:alloy>.addRecipe("spin_iron_to_gold", <item:minecraft:iron_ingot> * 10, <tag:items:minecraft:wool>, 200, <item:minecraft:gold_ingot> * 2);

/*
 * Removes a recipe from the Alloy Kiln based on its output.
 */

//<recipetype:immersiveengineering:alloy>.removeRecipe(output as IItemStack)
<recipetype:immersiveengineering:alloy>.removeRecipe(<item:immersiveengineering:ingot_constantan>);