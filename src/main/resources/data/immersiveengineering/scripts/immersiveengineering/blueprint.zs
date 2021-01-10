/*
 * Adds a new blueprint recipe.
 * The recipe category must already exist. It is currently not possible to add new blueprint categories.
 *
 * TODO: Remove once that issue is fixed: https://github.com/BluSunrize/ImmersiveEngineering/issues/4556 
 * Some recipe outputs may cause the game to get stuck, when putting the blueprint in the workbench.
 * For that reason, try to stick with items (not blocks) for now.
 */
//<recipetype:immersiveengineering:blueprint>.addRecipe(name as string, blueprintCategory as string, inputs as IIngredient[], output as IItemStack)
<recipetype:immersiveengineering:blueprint>.addRecipe("test_gaps", "bullet", [<item:minecraft:redstone>, <tag:items:forge:gems>], <item:minecraft:iron_sword>);

//TODO: Will currently lock the game, since it cannot render the block, remove once fixed
//<recipetype:immersiveengineering:blueprint>.addRecipe("some_test", "bullet", [<item:minecraft:bedrock>], <item:minecraft:iron_block>);

//Will not work, because the category "unknown_category" does not exist
//<recipetype:immersiveengineering:blueprint>.addRecipe("unknown_category_test", "unknown_category", [<item:minecraft:iron_nugget> * 10], <item:minecraft:iron_sword>);

/*
 * Removes a recipe based on its output
 */
<recipetype:immersiveengineering:blueprint>.removeRecipe(<item:immersiveengineering:casull>);