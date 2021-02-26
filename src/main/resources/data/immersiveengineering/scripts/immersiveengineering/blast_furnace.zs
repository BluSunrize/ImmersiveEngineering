/*
 * Adds a new fuel to the blast furnace.
 */
//<recipetype:immersiveengineering:blast_furnace_fuel>.addFuel(name as string, fuel as IIngredient, burnTime as int)
<recipetype:immersiveengineering:blast_furnace_fuel>.addFuel("the_sungods_sword_can_burn", <item:minecraft:golden_sword>.withTag({display: {Name: "{\"text\":\"Sword of the Sungod\"}" as string}}), 100000);

/*
 * Removes a fuel from the blast furnace
 */
 //<recipetype:immersiveengineering:blast_furnace_fuel>.removeRecipe(fuel as IItemStack)
<recipetype:immersiveengineering:blast_furnace_fuel>.removeFuel(<item:minecraft:charcoal>);

//<recipetype:immersiveengineering:blast_furnace>.removeRecipe(output as IItemStack)
<recipetype:immersiveengineering:blast_furnace>.removeRecipe(<item:minecraft:charcoal>);

/*
 * Adds a new recipe to the blast furnace.
 * The slag item is optional and will be set to air if not provided
 */
//<recipetype:immersiveengineering:blast_furnace>.addRecipe(recipePath as string, ingredient as IIngredientWithAmount, time as int, output as IItemStack, slag as IItemStack = <item:minecraft:air>)
<recipetype:immersiveengineering:blast_furnace>.addRecipe("wool_to_charcoal", <tag:items:minecraft:wool>, 1000, <item:minecraft:charcoal>, <item:minecraft:string>);