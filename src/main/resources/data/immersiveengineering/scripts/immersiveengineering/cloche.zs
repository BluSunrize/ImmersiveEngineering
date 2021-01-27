/*
 * Adds a fertilizer to the garden Cloche
 */
//<recipetype:immersiveengineering:fertilizer>.addFertilizer(name as string, fertilizer as IIngredient, growthModifier as float);
<recipetype:immersiveengineering:fertilizer>.addFertilizer("sulfur_grow", <tag:items:forge:dusts/sulfur>, 6.0F);


/*
 * Removes a fertilizer from the garden Cloche
 */
//<recipetype:immersiveengineering:fertilizer>.removeFertilizer(fertilizer as IItemStack);
<recipetype:immersiveengineering:fertilizer>.removeFertilizer(<item:minecraft:bone_meal>);



/*
 * Adds a recipe to the Garden cloche.
 * The render type can be left out, which will default to "generic"
 *
 * By default these 4 renderers are present:
 * "crop", can be used for any 1-block crops with an age property
 * "stacking", can be used for stacking plants like sugarcane or cactus
 * "stem", can be used for stem-grown plants like melon or pumpkin
 * "generic", can be used for any block, making it grow in size, like mushrooms
 */
//<recipetype:immersiveengineering:cloche>.addRecipe(name as string, seed as IIngredient, soil as IIngredient, time as int, outputs as IItemStack[], renderBlock as MCBlock, renderType as string = "generic")
<recipetype:immersiveengineering:cloche>.addRecipe("bonsai_oak", <item:minecraft:oak_sapling>, <item:minecraft:dirt>, 100, [<item:minecraft:apple>, <item:minecraft:oak_sapling>, <item:minecraft:oak_wood> * 5], <blockstate:minecraft:oak_sapling>.block, "generic");

/*
 * Removes a cloche recipe based on recipe output.
 * Removes all recipes where at least one of the outputs matches the provided ingredient
 */
//<recipetype:immersiveengineering:cloche>.removeRecipe(output as IItemStack); 
<recipetype:immersiveengineering:cloche>.removeRecipe(<item:minecraft:melon>);