/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.items.IEItems.Metals;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.data.*;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.function.Consumer;

class Recipes extends RecipeProvider
{
	private final Path ADV_ROOT;
	private final HashMap<String, Integer> PATH_COUNT = new HashMap<>();

	private final int standardSmeltingTime = 200;
	private final int blastDivider = 2;

	public Recipes(DataGenerator gen)
	{
		super(gen);
		ADV_ROOT = gen.getOutputFolder().resolve("data/minecraft/advancements/recipes/root.json");
	}

	@Override
	protected void saveRecipeAdvancement(DirectoryCache cache, JsonObject json, Path path)
	{
		if(path.equals(ADV_ROOT)) return; //We NEVER care about this.
		super.saveRecipeAdvancement(cache, json, path);
	}

	@Override
	protected void registerRecipes(@Nonnull Consumer<IFinishedRecipe> out)
	{
		for(EnumMetals metal : EnumMetals.values())
		{
			Item nugget = Metals.nuggets.get(metal);
			Item ingot = Metals.ingots.get(metal);
			Item plate = Metals.plates.get(metal);
			Item dust = Metals.dusts.get(metal);
			Block block = IEBlocks.Metals.storage.get(metal);
			Block sheetMetal = IEBlocks.Metals.sheetmetal.get(metal);
			if(!metal.isVanillaMetal())
			{
				add3x3Conversion(ingot, nugget, out);
				add3x3Conversion(block, ingot, out);
				if(IEBlocks.Metals.ores.containsKey(metal))
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					addStandardSmeltingBlastingRecipe(ore, ingot, metal.smeltingXP, out);
				}
			}
			addStandardSmeltingBlastingRecipe(dust, ingot, 0, out, "_from_dust");
//			addStandardSmeltingBlastingRecipe(dust, ingot, metal.smeltingXP, out, "_from_dust"); //TODO: remove this, if 0 XP on dust is intentional. this bugs out because the alloys do not have metal.smeltingXP
			ShapelessRecipeBuilder.shapelessRecipe(plate).addIngredient(IETags.getTagsFor(metal).ingot).addIngredient(Tools.hammer).addCriterion("has_"+metal.tagName()+"_ingot", this.hasItem(IETags.getTagsFor(metal).ingot)).build(out, toRL("plate_"+metal.tagName()+"_hammering"));
			ShapedRecipeBuilder.shapedRecipe(sheetMetal, 4)
					.key('p', plate)
					.patternLine(" p ")
					.patternLine("p p")
					.patternLine(" p ")
					.addCriterion("has_"+toPath(plate), hasItem(plate))
					.build(out);
		}
		addStandardSmeltingBlastingRecipe(IEItems.Ingredients.dustHopGraphite, Ingredients.ingotHopGraphite, 0.5F, out);

		for(Entry<Block, Block> blockSlab : IEBlocks.toSlab.entrySet())
			addSlab(blockSlab.getKey(), blockSlab.getValue(), out);
		addStairs(StoneDecoration.hempcrete, StoneDecoration.hempcreteStairs, out);
		addStairs(StoneDecoration.concrete, StoneDecoration.concreteStairs[0], out);
		addStairs(StoneDecoration.concreteTile, StoneDecoration.concreteStairs[1], out);
		addStairs(StoneDecoration.concreteLeaded, StoneDecoration.concreteStairs[2], out);
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			addStairs(WoodenDecoration.treatedWood.get(style), WoodenDecoration.treatedStairs.get(style), out);
		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			addStairs(MetalDecoration.steelScaffolding.get(type), MetalDecoration.steelScaffoldingStair.get(type), out);
			addStairs(MetalDecoration.aluScaffolding.get(type), MetalDecoration.aluScaffoldingStair.get(type), out);
		}
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.hempcrete), IEBlocks.toSlab.get(StoneDecoration.hempcrete)).func_218643_a("has_hempcrete", this.hasItem(StoneDecoration.hempcrete)).func_218647_a(out, toRL("hempcrete_slab_from_hempcrete_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.hempcrete), StoneDecoration.hempcreteStairs).func_218643_a("has_hempcrete", this.hasItem(StoneDecoration.hempcrete)).func_218647_a(out, toRL("hempcrete_stairs_from_hempcrete_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concrete), IEBlocks.toSlab.get(StoneDecoration.concrete)).func_218643_a("has_concrete", this.hasItem(StoneDecoration.concrete)).func_218647_a(out, toRL("concrete_slab_from_concrete_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concrete), StoneDecoration.concreteStairs[0]).func_218643_a("has_concrete", this.hasItem(StoneDecoration.concrete)).func_218647_a(out, toRL("concrete_stairs_from_concrete_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concreteTile), IEBlocks.toSlab.get(StoneDecoration.concreteTile)).func_218643_a("has_concrete", this.hasItem(StoneDecoration.concrete)).func_218647_a(out, toRL("concrete_tile_slab_from_concrete_tile_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concreteTile), StoneDecoration.concreteStairs[1]).func_218643_a("has_concrete", this.hasItem(StoneDecoration.concrete)).func_218647_a(out, toRL("concrete_tile_stairs_from_concrete_tile_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concreteLeaded), IEBlocks.toSlab.get(StoneDecoration.concreteLeaded)).func_218643_a("has_concrete", this.hasItem(StoneDecoration.concrete)).func_218647_a(out, toRL("concrete_leaded_slab_from_concrete_leaded_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concreteLeaded), StoneDecoration.concreteStairs[2]).func_218643_a("has_concrete", this.hasItem(StoneDecoration.concrete)).func_218647_a(out, toRL("concrete_leaded_stairs_from_concrete_leaded_stonecutting"));


		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.alloybrick, 2).patternLine("sb").patternLine("bs").key('s', Tags.Items.SANDSTONE).key('b', Tags.Items.INGOTS_BRICK).addCriterion("has_brick", hasItem(Tags.Items.INGOTS_BRICK)).build(out);
		addCornerStraightMiddle(StoneDecoration.cokebrick, 3, IETags.clay, Tags.Items.INGOTS_BRICK, Tags.Items.SANDSTONE, out);
		addCornerStraightMiddle(StoneDecoration.blastbrick, 3, Tags.Items.INGOTS_NETHER_BRICK, Tags.Items.INGOTS_BRICK, Items.BLAZE_POWDER, out);
		ShapelessRecipeBuilder.shapelessRecipe(StoneDecoration.blastbrickReinforced).addIngredient(StoneDecoration.blastbrick).addIngredient(IETags.getTagsFor(EnumMetals.STEEL).plate).addCriterion("has_blastbrick", hasItem(StoneDecoration.blastbrick)).build(out);
		addSandwich(StoneDecoration.hempcrete, 6, IETags.clay, IETags.fiberHemp, IETags.clay, out);
		add3x3Conversion(StoneDecoration.coke, IEItems.Ingredients.coalCoke, out);

		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.concrete, 8).setGroup("ie_concrete").patternLine("scs").patternLine("gbg").patternLine("scs").key('s', Tags.Items.SAND).key('c', IETags.clay).key('g', Tags.Items.GRAVEL).key('b', Items.WATER_BUCKET).addCriterion("has_clay", hasItem(IETags.clay)).build(out, toRL("concrete"));
		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.concrete, 12).setGroup("ie_concrete").patternLine("scs").patternLine("gbg").patternLine("scs").key('s', IEItems.Ingredients.slag).key('c', IETags.clay).key('g', Tags.Items.GRAVEL).key('b', Items.WATER_BUCKET).addCriterion("has_slag", hasItem(IEItems.Ingredients.slag)).build(out, toRL("concrete"));
		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.concreteTile, 4).setGroup("ie_concrete").patternLine("cc").patternLine("cc").key('c', StoneDecoration.concrete).addCriterion("has_concrete", hasItem(StoneDecoration.concrete)).build(out);
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concrete), StoneDecoration.concreteTile).func_218643_a("has_concrete", this.hasItem(StoneDecoration.concrete)).func_218647_a(out, toRL("concrete_tile_from_concrete_stonecutting"));
		ShapelessRecipeBuilder.shapelessRecipe(StoneDecoration.concreteLeaded).addIngredient(StoneDecoration.concrete).addIngredient(IETags.getTagsFor(EnumMetals.LEAD).plate).addCriterion("has_concrete", hasItem(StoneDecoration.concrete)).build(out);
		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.insulatingGlass, 2).patternLine(" g ").patternLine("idi").patternLine(" g ").key('g', Tags.Items.GLASS).key('i', IETags.getTagsFor(EnumMetals.IRON).dust).key('d', Tags.Items.DYES_GREEN).addCriterion("has_glass", hasItem(Tags.Items.GLASS)).build(out);

		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickTreated, 4).patternLine("w").patternLine("w").key('w', IETags.getItemTag(IETags.treatedWood)).setGroup("sticks").addCriterion("has_treated_planks", this.hasItem(IETags.getItemTag(IETags.treatedWood))).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickIron, 4).patternLine("i").patternLine("i").key('i', IETags.getTagsFor(EnumMetals.IRON).ingot).setGroup("sticks").addCriterion("has_iron_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickSteel, 4).patternLine("i").patternLine("i").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).setGroup("sticks").addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickAluminum, 4).patternLine("i").patternLine("i").key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot).setGroup("sticks").addCriterion("has_alu_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.hempFabric).patternLine("fff").patternLine("fsf").patternLine("fff").key('f', IETags.fiberHemp).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_hemp_fiber", this.hasItem(IETags.fiberHemp)).build(out);

		ShapedRecipeBuilder.shapedRecipe(Ingredients.componentIron).patternLine("i i").patternLine(" c ").patternLine("i i").key('i', IETags.getTagsFor(EnumMetals.IRON).plate).key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot).addCriterion("has_iron_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.componentSteel).patternLine("i i").patternLine(" c ").patternLine("i i").key('i', IETags.getTagsFor(EnumMetals.STEEL).plate).key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.waterwheelSegment).patternLine(" s ").patternLine("sbs").patternLine("bsb").key('s', IETags.treatedStick).key('b', IETags.getItemTag(IETags.treatedWood)).addCriterion("has_treated_planks", this.hasItem(IETags.getItemTag(IETags.treatedWood))).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.windmillBlade).patternLine("bb ").patternLine("ssb").patternLine("ss ").key('s', IETags.treatedStick).key('b', IETags.getItemTag(IETags.treatedWood)).addCriterion("has_treated_planks", this.hasItem(IETags.getItemTag(IETags.treatedWood))).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.windmillSail).patternLine(" cc").patternLine("ccc").patternLine(" c ").key('c', IETags.fabricHemp).addCriterion("has_hemp_fabric", this.hasItem(IETags.fabricHemp)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.woodenGrip).patternLine("ss").patternLine("cs").patternLine("ss").key('s', IETags.treatedStick).key('c', IETags.getTagsFor(EnumMetals.COPPER).nugget).addCriterion("has_treated_sticks", this.hasItem(IETags.treatedStick)).build(out);
//		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartBarrel).build(out);
//		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartDrum).build(out);
//		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartHammer).build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireCopper).addIngredient(IETags.getTagsFor(EnumMetals.COPPER).plate).addIngredient(Ingredient.fromItems(Items.SHEARS, Tools.wirecutter)).addCriterion("has_copper_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot)).build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireElectrum).addIngredient(IETags.getTagsFor(EnumMetals.ELECTRUM).plate).addIngredient(Ingredient.fromItems(Items.SHEARS, Tools.wirecutter)).addCriterion("has_electrum_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)).build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireAluminum).addIngredient(IETags.getTagsFor(EnumMetals.ALUMINUM).plate).addIngredient(Ingredient.fromItems(Items.SHEARS, Tools.wirecutter)).addCriterion("has_aluminum_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)).build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireSteel).addIngredient(IETags.getTagsFor(EnumMetals.STEEL).plate).addIngredient(Ingredient.fromItems(Items.SHEARS, Tools.wirecutter)).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Items.GUNPOWDER).addIngredient(Ingredient.fromTag(IETags.saltpeterDust), 4).addIngredient(IETags.sulfurDust).addIngredient(Items.CHARCOAL).addCriterion("has_sulfur", this.hasItem(IETags.sulfurDust)).build(out, toRL("gunpowder_from_dusts"));

		ShapedRecipeBuilder.shapedRecipe(Tools.hammer).patternLine(" if").patternLine(" si").patternLine("s  ").key('s', Tags.Items.RODS_WOODEN).key('i', IETags.getTagsFor(EnumMetals.IRON).ingot).key('f', Tags.Items.STRING).addCriterion("has_iron_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.wirecutter).patternLine("si").patternLine(" s").key('s', Tags.Items.RODS_WOODEN).key('i', IETags.getTagsFor(EnumMetals.IRON).ingot).addCriterion("has_iron_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot)).build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Tools.manual).addIngredient(Items.BOOK).addIngredient(Items.LEVER).addCriterion("has_book", hasItem(Items.BOOK)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.steelAxe).patternLine("ii").patternLine("is").patternLine(" s").key('s', IETags.treatedStick).key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.steelPick).patternLine("iii").patternLine(" s ").patternLine(" s ").key('s', IETags.treatedStick).key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.steelShovel).patternLine("i").patternLine("s").patternLine("s").key('s', IETags.treatedStick).key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.steelSword).patternLine("i").patternLine("i").patternLine("s").key('s', IETags.treatedStick).key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);

		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.COPPER),4).patternLine(" w ").patternLine("wsw").patternLine(" w ").key('w', IETags.copperWire).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_copper_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.ELECTRUM),4).patternLine(" w ").patternLine("wsw").patternLine(" w ").key('w', IETags.electrumWire).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_electrum_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.STEEL),4).patternLine(" w ").patternLine("asa").patternLine(" w ").key('w', IETags.steelWire).key('a', IETags.aluminumWire).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.STEEL),4).patternLine(" a ").patternLine("wsw").patternLine(" a ").key('w', IETags.steelWire).key('a', IETags.aluminumWire).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out, toRL("wirecoil_steel2"));
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.STRUCTURE_ROPE),4).patternLine(" w ").patternLine("wsw").patternLine(" w ").key('w', Ingredients.hempFiber).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_hemp_fiber", this.hasItem(Ingredients.hempFiber)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.STRUCTURE_STEEL),4).patternLine(" w ").patternLine("wsw").patternLine(" w ").key('w', Ingredients.wireSteel).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
		addCornerStraightMiddle(Misc.wireCoils.get(WireType.COPPER_INSULATED), 4, IETags.fabricHemp, Misc.wireCoils.get(WireType.COPPER), IETags.fabricHemp, out);
		addCornerStraightMiddle(Misc.wireCoils.get(WireType.ELECTRUM_INSULATED), 4, IETags.fabricHemp, Misc.wireCoils.get(WireType.ELECTRUM), IETags.fabricHemp, out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.REDSTONE),4).patternLine(" w ").patternLine("asa").patternLine(" w ").key('w', IETags.aluminumWire).key('a', Tags.Items.DUSTS_REDSTONE).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_aluminum_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)).build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.REDSTONE),4).patternLine(" a ").patternLine("wsw").patternLine(" a ").key('w', IETags.aluminumWire).key('a', Tags.Items.DUSTS_REDSTONE).key('s', Tags.Items.RODS_WOODEN).addCriterion("has_aluminum_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)).build(out, toRL("wirecoil_redstone2"));

		//NYI
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.steelArmor[0]).patternLine("i i").patternLine("i i").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.steelArmor[1]).patternLine("iii").patternLine("i i").patternLine("i i").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.steelArmor[2]).patternLine("i i").patternLine("iii").patternLine("iii").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.steelArmor[3]).patternLine("iii").patternLine("i i").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", this.hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
	}

	//TODO use tags
	private void add3x3Conversion(IItemProvider big, IItemProvider small, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(big)
				.key('s', small)
				.patternLine("sss")
				.patternLine("sss")
				.patternLine("sss")
				.addCriterion("has_"+toPath(small), hasItem(small))
				.build(out, toRL(toPath(small)+"_to_")+toPath(big));
		ShapelessRecipeBuilder.shapelessRecipe(small, 9)
				.addIngredient(big)
				.addCriterion("has_"+toPath(big), hasItem(small))
				.build(out, toRL(toPath(big)+"_to_"+toPath(small)));
	}

	private void addSlab(IItemProvider block, IItemProvider slab, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(slab, 6)
				.key('s', block)
				.patternLine("sss")
				.addCriterion("has_"+toPath(block), hasItem(block))
				.build(out, toRL(toPath(block)+"_to_slab"));
		ShapedRecipeBuilder.shapedRecipe(block)
				.key('s', slab)
				.patternLine("s")
				.patternLine("s")
				.addCriterion("has_"+toPath(block), hasItem(block))
				.build(out, toRL(toPath(block)+"_from_slab"));
	}

	private void addStairs(IItemProvider block, IItemProvider stairs, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(stairs, 4)
				.key('s', block)
				.patternLine("s  ")
				.patternLine("ss ")
				.patternLine("sss")
				.addCriterion("has_"+toPath(block), hasItem(block))
				.build(out, toRL(toPath(stairs)));
	}

	/**
	 * For Recipes like Coke or Blast Bricks, which use the same item in all corners, one for the sides and one for the middle
	 * Also work for shapes like TNT
	 *
	 * @param output the recipe's output
	 * @param corner the item in the corners
	 * @param side   the item on the sides
	 * @param middle the item in the middle
	 */
	@ParametersAreNonnullByDefault
	private void addCornerStraightMiddle(IItemProvider output, int count, Object corner, Object side, Object middle, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(output, count)
				.key('c', makeIngredient(corner))
				.key('s', makeIngredient(side))
				.key('m', makeIngredient(middle))
				.patternLine("csc")
				.patternLine("sms")
				.patternLine("csc")
				.addCriterion("has_"+toPath(output), hasItem(output))
				.build(out, toRL(toPath(output)));
	}

	/**
	 * For Recipes consisting of layers
	 *
	 * @param output the recipe's output
	 * @param top    the item on the top
	 * @param middle the item in the middle
	 * @param bottom the item on the bottom
	 */
	@ParametersAreNonnullByDefault
	private void addSandwich(IItemProvider output, int count, Object top, Object middle, Object bottom, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(output, count)
				.key('t', makeIngredient(top))
				.key('m', makeIngredient(middle))
				.key('b', makeIngredient(bottom))
				.patternLine("ttt")
				.patternLine("mmm")
				.patternLine("bbb")
				.addCriterion("has_"+toPath(output), hasItem(output))
				.build(out, toRL(toPath(output)));
	}

	private String toPath(IItemProvider src)
	{
		return src.asItem().getRegistryName().getPath();
	}

	private ResourceLocation toRL(String s)
	{
		if(PATH_COUNT.containsKey(s))
		{
			int count = PATH_COUNT.get(s)+1;
			PATH_COUNT.put(s, count);
			return new ResourceLocation(ImmersiveEngineering.MODID, s+count);
		}
		PATH_COUNT.put(s, 1);
		return new ResourceLocation(ImmersiveEngineering.MODID, s);
	}

	@Nonnull
	private Ingredient makeIngredient(Object in)
	{
		assert in instanceof IItemProvider||in instanceof Tag||in instanceof Ingredient;
		if(in instanceof IItemProvider)
			return Ingredient.fromItems((IItemProvider)in);
		else if(in instanceof Tag)
			return Ingredient.fromTag((Tag)in);
		else
			return (Ingredient)in;
	}

	/**
	 * For smelting recipes that also have a blasting recipe, like ores
	 * keep the smelting postfix in mind when using this for non-ores or weird cases where the primary recipe for the ingot is not occupied by the smelting recipe
	 * has an overloaded method for regular use
	 *
	 * @param input  		the recipe's input
	 * @param output 		the recipe's output
	 * @param xp			experience awarded per smelted item
	 * @param smeltingTime	smelting time in ticks
	 * @param extraPostfix	adds an additional postfix before the smelting/blasting postfix when needed (for example used by dusts)
	 * @param smeltPostfix	allows adding the smelting postfix to the smelting (non-blasting) recipe
	 */
	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, int smeltingTime, Consumer<IFinishedRecipe> out, String extraPostfix, boolean smeltPostfix) {
		CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(input), output, xp, smeltingTime).addCriterion("has_"+toPath(input), hasItem(input)).build(out, toRL(toPath(output) + extraPostfix + (smeltPostfix ? "_from_smelting" : "")));
		CookingRecipeBuilder.blastingRecipe(Ingredient.fromItems(input), output, xp, smeltingTime/blastDivider).addCriterion("has_"+toPath(input), hasItem(input)).build(out, toRL(toPath(output) + extraPostfix + "_from_blasting"));
	}

	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, Consumer<IFinishedRecipe> out) {
		addStandardSmeltingBlastingRecipe(input, output, xp, out, "");
	}

	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, Consumer<IFinishedRecipe> out, String extraPostfix) {
		addStandardSmeltingBlastingRecipe(input, output, xp, standardSmeltingTime, out, extraPostfix, false);
	}
}
