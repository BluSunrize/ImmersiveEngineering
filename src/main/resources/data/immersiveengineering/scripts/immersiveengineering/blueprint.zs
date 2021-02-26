/*
 * Adds a new blueprint recipe.
 * The recipe category must already exist. It is currently not possible to add new blueprint categories.
 *
 */
//<recipetype:immersiveengineering:blueprint>.addRecipe(name as string, blueprintCategory as string, inputs as IIngredientWithAmount[], output as IItemStack)
<recipetype:immersiveengineering:blueprint>.addRecipe("test_gaps", "bullet", [<item:minecraft:redstone>, <tag:items:forge:gems>], <item:minecraft:iron_sword>);

<recipetype:immersiveengineering:blueprint>.addRecipe("some_test", "bullet", [<item:minecraft:bedrock>], <item:minecraft:iron_block>);

//Will not work, because the category "unknown_category" does not exist
//<recipetype:immersiveengineering:blueprint>.addRecipe("unknown_category_test", "unknown_category", [<item:minecraft:iron_nugget> * 10], <item:minecraft:iron_sword>);

/*
 * Removes a recipe based on its output
 */
<recipetype:immersiveengineering:blueprint>.removeRecipe(<item:immersiveengineering:casull>);