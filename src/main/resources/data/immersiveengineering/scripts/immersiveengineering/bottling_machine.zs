/*
 * Adds a Bottling Machine Recipe
 */
//<recipetype:immersiveengineering:bottling_machine>.addRecipe(recipePath as string, inputs as IIngredientWithAmount[], fluidTag as MCTagWithAmount<Fluid>, output as IItemStack)
<recipetype:immersiveengineering:bottling_machine>.addRecipe("grow_a_pick", [<item:minecraft:stick> * 3], <tag:fluids:minecraft:water> * 250, <item:minecraft:wooden_pickaxe>);

/*
 * Removes based on recipe output.
 * Cannot remove potion recipes!
 */
//<recipetype:immersiveengineering:bottling_machine>.remove(output as IItemStack);
<recipetype:immersiveengineering:bottling_machine>.remove(<item:minecraft:wet_sponge>);