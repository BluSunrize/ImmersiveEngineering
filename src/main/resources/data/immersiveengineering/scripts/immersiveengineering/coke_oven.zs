/*
 * Adds a new recipe to the coke oven.
 * The creosoteProduced argument can be left out and will default to 0mB
 */
//<recipetype:immersiveengineering:coke_oven>.addRecipe(recipePath as string, ingredient as IIngredientWithAmount, time as int, output as IItemStack, creosoteProduced as int = 0)
<recipetype:immersiveengineering:coke_oven>.addRecipe("burn_a_stick", <item:minecraft:stick>, 100, <item:immersiveengineering:stick_treated>, 1);

/*
 * Removes a recipe from the coke oven
 */
 //<recipetype:immersiveengineering:coke_oven>.removeRecipe(fuel as IItemStack)
<recipetype:immersiveengineering:coke_oven>.removeRecipe(<item:immersiveengineering:coal_coke>);