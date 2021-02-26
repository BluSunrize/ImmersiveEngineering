/*
 * Adds a new mix to the Mineral Mixes
 */

//<recipetype:immersiveengineering:mineral_mix>.addRecipe(String recipePath, MCWeightedItemStack[] outputs, int weight, float failChance, ResourceLocation[] dimensions, Block background)
<recipetype:immersiveengineering:mineral_mix>.addRecipe("sheep_mix", [<item:minecraft:white_wool> % 50, <item:minecraft:orange_wool> % 25, <item:minecraft:magenta_wool>], 50, 0.5, [<resource:minecraft:overworld>], <block:minecraft:white_wool>);

/*
 * Removes a mix from the Mineral Mixes based on it's name
 */

//<recipetype:immersiveengineering:mineral_mix>.removeByName(name as String)
<recipetype:immersiveengineering:mineral_mix>.removeByName("immersiveengineering:mineral/ancient_debris");