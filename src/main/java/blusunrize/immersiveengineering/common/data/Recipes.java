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
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.WoodenDevices;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.*;
import blusunrize.immersiveengineering.common.items.ToolUpgradeItem.ToolUpgrade;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
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

public class Recipes extends RecipeProvider
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
			IETags.MetalTags tags=IETags.getTagsFor(metal);

			Item nugget = Metals.nuggets.get(metal);
			Item ingot = Metals.ingots.get(metal);
			Item plate = Metals.plates.get(metal);
			Item dust = Metals.dusts.get(metal);
			Block block = IEBlocks.Metals.storage.get(metal);
			Block sheetMetal = IEBlocks.Metals.sheetmetal.get(metal);
			if(!metal.isVanillaMetal())
			{
				add3x3Conversion(ingot, tags.ingot, nugget, tags.nugget, out);
				add3x3Conversion(block, IETags.getItemTag(tags.storage), ingot, tags.ingot, out);
				if(IEBlocks.Metals.ores.containsKey(metal))
				{
					Block ore = IEBlocks.Metals.ores.get(metal);
					addStandardSmeltingBlastingRecipe(ore, ingot, metal.smeltingXP, out);
				}
			}
			addStandardSmeltingBlastingRecipe(dust, ingot, 0, out, "_from_dust");
//			addStandardSmeltingBlastingRecipe(dust, ingot, metal.smeltingXP, out, "_from_dust"); //TODO: remove this, if 0 XP on dust is intentional. this bugs out because the alloys do not have metal.smeltingXP
			ShapelessRecipeBuilder.shapelessRecipe(plate)
				.addIngredient(IETags.getTagsFor(metal).ingot)
				.addIngredient(Tools.hammer)
				.addCriterion("has_"+metal.tagName()+"_ingot", hasItem(IETags.getTagsFor(metal).ingot))
				.build(out, toRL("plate_"+metal.tagName()+"_hammering"));
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

		recipesStoneDecorations(out);
		recipesWoodenDecorations(out);
		recipesWoodenDevices(out);
		recipesMetalDecorations(out);
		recipesMetalDevices(out);
		recipesConnectors(out);
		recipesCloth(out);

		recipesTools(out);
		recipesIngredients(out);
		recipesWeapons(out);
		recipesMisc(out);

		CustomRecipeBuilder.func_218656_a(RecipeSerializers.SPEEDLOADER_LOAD.get())
			.build(out, ImmersiveEngineering.MODID+":speedloader_load");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.FLARE_BULLET_COLOR.get())
			.build(out, ImmersiveEngineering.MODID+":flare_bullet_color");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.POTION_BULLET_FILL.get())
			.build(out, ImmersiveEngineering.MODID+":potion_bullet_fill");
		CustomRecipeBuilder.func_218656_a(RecipeSerializers.JERRYCAN_REFILL.get())
			.build(out, ImmersiveEngineering.MODID+":jerrycan_refill");

		//NYI
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.steelArmor[0]).patternLine("i i").patternLine("i i").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.steelArmor[1]).patternLine("iii").patternLine("i i").patternLine("i i").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.steelArmor[2]).patternLine("i i").patternLine("iii").patternLine("iii").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.steelArmor[3]).patternLine("iii").patternLine("i i").key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot).addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot)).build(out);
	}

	private void recipesStoneDecorations(@Nonnull Consumer<IFinishedRecipe> out)
	{
		addCornerStraightMiddle(StoneDecoration.cokebrick, 3, IETags.clay, Tags.Items.INGOTS_BRICK, Tags.Items.SANDSTONE, out);
		addCornerStraightMiddle(StoneDecoration.blastbrick, 3, Tags.Items.INGOTS_NETHER_BRICK, Tags.Items.INGOTS_BRICK, Items.BLAZE_POWDER, out);
		addSandwich(StoneDecoration.hempcrete, 6, IETags.clay, IETags.fiberHemp, IETags.clay, out);
		add3x3Conversion(StoneDecoration.coke, IETags.getItemTag(IETags.coalCokeBlock), IEItems.Ingredients.coalCoke, IETags.coalCoke, out);

		addStairs(StoneDecoration.hempcrete, StoneDecoration.hempcreteStairs, out);
		addStairs(StoneDecoration.concrete, StoneDecoration.concreteStairs[0], out);
		addStairs(StoneDecoration.concreteTile, StoneDecoration.concreteStairs[1], out);
		addStairs(StoneDecoration.concreteLeaded, StoneDecoration.concreteStairs[2], out);

		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.hempcrete), IEBlocks.toSlab.get(StoneDecoration.hempcrete))
			.func_218643_a("has_hempcrete", hasItem(StoneDecoration.hempcrete))
			.func_218647_a(out, toRL("hempcrete_slab_from_hempcrete_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.hempcrete), StoneDecoration.hempcreteStairs)
			.func_218643_a("has_hempcrete", hasItem(StoneDecoration.hempcrete))
			.func_218647_a(out, toRL("hempcrete_stairs_from_hempcrete_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concrete), IEBlocks.toSlab.get(StoneDecoration.concrete))
			.func_218643_a("has_concrete", hasItem(StoneDecoration.concrete))
			.func_218647_a(out, toRL("concrete_slab_from_concrete_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concrete), StoneDecoration.concreteStairs[0])
			.func_218643_a("has_concrete", hasItem(StoneDecoration.concrete))
			.func_218647_a(out, toRL("concrete_stairs_from_concrete_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concreteTile), IEBlocks.toSlab.get(StoneDecoration.concreteTile))
			.func_218643_a("has_concrete", hasItem(StoneDecoration.concrete))
			.func_218647_a(out, toRL("concrete_tile_slab_from_concrete_tile_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concreteTile), StoneDecoration.concreteStairs[1])
			.func_218643_a("has_concrete", hasItem(StoneDecoration.concrete))
			.func_218647_a(out, toRL("concrete_tile_stairs_from_concrete_tile_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concreteLeaded), IEBlocks.toSlab.get(StoneDecoration.concreteLeaded))
			.func_218643_a("has_concrete", hasItem(StoneDecoration.concrete))
			.func_218647_a(out, toRL("concrete_leaded_slab_from_concrete_leaded_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concreteLeaded), StoneDecoration.concreteStairs[2])
			.func_218643_a("has_concrete", hasItem(StoneDecoration.concrete))
			.func_218647_a(out, toRL("concrete_leaded_stairs_from_concrete_leaded_stonecutting"));
		SingleItemRecipeBuilder.func_218648_a(Ingredient.fromItems(StoneDecoration.concrete), StoneDecoration.concreteTile)
			.func_218643_a("has_concrete", hasItem(StoneDecoration.concrete))
			.func_218647_a(out, toRL("concrete_tile_from_concrete_stonecutting"));

		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.alloybrick, 2)
			.patternLine("sb")
			.patternLine("bs")
			.key('s', Tags.Items.SANDSTONE)
			.key('b', Tags.Items.INGOTS_BRICK)
			.addCriterion("has_brick", hasItem(Tags.Items.INGOTS_BRICK))
			.build(out);
		ShapelessRecipeBuilder.shapelessRecipe(StoneDecoration.blastbrickReinforced)
			.addIngredient(StoneDecoration.blastbrick)
			.addIngredient(IETags.getTagsFor(EnumMetals.STEEL).plate)
			.addCriterion("has_blastbrick", hasItem(StoneDecoration.blastbrick))
			.build(out);
	
		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.concrete, 8)
			.setGroup("ie_concrete")
			.patternLine("scs")
			.patternLine("gbg")
			.patternLine("scs")
			.key('s', Tags.Items.SAND)
			.key('c', IETags.clay)
			.key('g', Tags.Items.GRAVEL)
			.key('b', Items.WATER_BUCKET)
			.addCriterion("has_clay", hasItem(IETags.clay))
			.build(out, toRL("concrete"));
		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.concrete, 12)
			.setGroup("ie_concrete")
			.patternLine("scs")
			.patternLine("gbg")
			.patternLine("scs")
			.key('s', IEItems.Ingredients.slag)
			.key('c', IETags.clay)
			.key('g', Tags.Items.GRAVEL)
			.key('b', Items.WATER_BUCKET)
			.addCriterion("has_slag", hasItem(IEItems.Ingredients.slag))
			.build(out, toRL("concrete"));
		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.concreteTile, 4)
			.setGroup("ie_concrete")
			.patternLine("cc")
			.patternLine("cc")
			.key('c', StoneDecoration.concrete)
			.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
			.build(out);
		ShapelessRecipeBuilder.shapelessRecipe(StoneDecoration.concreteLeaded)
			.addIngredient(StoneDecoration.concrete)
			.addIngredient(IETags.getTagsFor(EnumMetals.LEAD).plate)
			.addCriterion("has_concrete", hasItem(StoneDecoration.concrete))
			.build(out);
	
		ShapedRecipeBuilder.shapedRecipe(StoneDecoration.insulatingGlass, 2)
			.patternLine(" g ")
			.patternLine("idi")
			.patternLine(" g ")
			.key('g', Tags.Items.GLASS)
			.key('i', IETags.getTagsFor(EnumMetals.IRON).dust)
			.key('d', Tags.Items.DYES_GREEN)
			.addCriterion("has_glass", hasItem(Tags.Items.GLASS))
			.build(out);
	}

	private void recipesWoodenDecorations(@Nonnull Consumer<IFinishedRecipe> out)
	{
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			addStairs(WoodenDecoration.treatedWood.get(style), WoodenDecoration.treatedStairs.get(style), out);

		int numTreatedStyles=TreatedWoodStyles.values().length;
		for(TreatedWoodStyles from:TreatedWoodStyles.values())
		{
			TreatedWoodStyles to = TreatedWoodStyles.values()[(from.ordinal()+1)%numTreatedStyles];
			ShapelessRecipeBuilder.shapelessRecipe(WoodenDecoration.treatedWood.get(to))
				.addIngredient(WoodenDecoration.treatedWood.get(from))
				.addCriterion("has_"+toPath(WoodenDecoration.treatedWood.get(from)), hasItem(WoodenDecoration.treatedWood.get(from)))
				.build(out, toRL(toPath(WoodenDecoration.treatedWood.get(to))+"_from_"+from.toString().toLowerCase()));
		}
		ShapedRecipeBuilder.shapedRecipe(WoodenDecoration.treatedScaffolding, 6)
			.patternLine("iii")
			.patternLine(" s ")
			.patternLine("s s")
			.key('i', IETags.getItemTag(IETags.treatedWood))
			.key('s', IETags.treatedStick)
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.addCriterion("has_treated_sticks", hasItem(IETags.treatedStick))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDecoration.treatedFence, 3)
			.patternLine("isi")
			.patternLine("isi")
			.key('i', IETags.getItemTag(IETags.treatedWood))
			.key('s', IETags.treatedStick)
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.addCriterion("has_treated_sticks", hasItem(IETags.treatedStick))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDecoration.treatedPost)
			.patternLine("f")
			.patternLine("f")
			.patternLine("s")
			.key('f', WoodenDecoration.treatedFence)
			.key('s', Blocks.STONE_BRICKS)
			.addCriterion("has_"+toPath(WoodenDecoration.treatedFence), hasItem(WoodenDecoration.treatedFence))
			.build(out);
	}

	private void recipesWoodenDevices(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.crate)
			.patternLine("ppp")
			.patternLine("p p")
			.patternLine("ppp")
			.key('p', IETags.getItemTag(IETags.treatedWood))
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.reinforcedCrate) // TODO Needs NBT copying
			.patternLine("wpw")
			.patternLine("rcr")
			.patternLine("wpw")
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('p', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('r', IETags.ironRod)
			.key('c', IEBlocks.WoodenDevices.crate)
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.treatedWallmount, 4)
			.patternLine("ww")
			.patternLine("ws")
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('s', IETags.treatedStick)
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.sorter)
			.patternLine("wrw")
			.patternLine("ibi")
			.patternLine("wcw")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('b', ConveyorHandler.getBlock(BasicConveyor.NAME))
			.key('c', Ingredients.componentIron)
			.addCriterion("has_"+toPath(ConveyorHandler.getBlock(BasicConveyor.NAME)), hasItem(ConveyorHandler.getBlock(BasicConveyor.NAME)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.fluidSorter)
			.patternLine("wrw")
			.patternLine("ibi")
			.patternLine("wcw")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('b', MetalDevices.fluidPipe)
			.key('c', Ingredients.componentIron)
			.addCriterion("has_"+toPath(MetalDevices.fluidPipe), hasItem(MetalDevices.fluidPipe))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.turntable)
			.patternLine("iwi")
			.patternLine("rcr")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('c', MetalDecoration.lvCoil)
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.windmill)
			.patternLine("ppp")
			.patternLine("pip")
			.patternLine("ppp")
			.key('p', Ingredients.windmillBlade)
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.addCriterion("has_"+toPath(Ingredients.windmillBlade), hasItem(Ingredients.windmillBlade))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.watermill)
			.patternLine(" p ")
			.patternLine("pip")
			.patternLine(" p ")
			.key('p', Ingredients.waterwheelSegment)
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.addCriterion("has_"+toPath(Ingredients.waterwheelSegment), hasItem(Ingredients.waterwheelSegment))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.gunpowderBarrel)
			.patternLine(" f ")
			.patternLine("gbg")
			.patternLine("ggg")
			.key('f', Ingredients.hempFiber)
			.key('g', Tags.Items.GUNPOWDER)
			.key('b', WoodenDevices.woodenBarrel)
			.addCriterion("has_"+toPath(WoodenDevices.woodenBarrel), hasItem(WoodenDevices.woodenBarrel))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.workbench)
			.patternLine("ppp")
			.patternLine("c f")
			.key('p', IETags.getItemTag(IETags.treatedWood))
			.key('c', Blocks.CRAFTING_TABLE)
			.key('f', WoodenDecoration.treatedFence)
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(WoodenDevices.woodenBarrel)
			.patternLine("sss")
			.patternLine("w w")
			.patternLine("www")
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('s', IETags.getItemTag(IETags.treatedWoodSlab))
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
	}

	private void recipesMetalDecorations(@Nonnull Consumer<IFinishedRecipe> out)
	{
		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			addStairs(MetalDecoration.steelScaffolding.get(type), MetalDecoration.steelScaffoldingStair.get(type), out);
			addStairs(MetalDecoration.aluScaffolding.get(type), MetalDecoration.aluScaffoldingStair.get(type), out);
		}

		int numScaffoldingTypes = MetalScaffoldingType.values().length;
		for(MetalScaffoldingType from:MetalScaffoldingType.values())
		{
			MetalScaffoldingType to = MetalScaffoldingType.values()[(from.ordinal()+1)%numScaffoldingTypes];
			ShapelessRecipeBuilder.shapelessRecipe(MetalDecoration.aluScaffolding.get(to))
				.addIngredient(MetalDecoration.aluScaffolding.get(from))
				.addCriterion("has_"+toPath(MetalDecoration.aluScaffolding.get(from)), hasItem(MetalDecoration.aluScaffolding.get(from)))
				.build(out, toRL("alu_scaffolding_"+to.name().toLowerCase()+"_from_"+from.name().toLowerCase()));
			ShapelessRecipeBuilder.shapelessRecipe(MetalDecoration.steelScaffolding.get(to))
				.addIngredient(MetalDecoration.steelScaffolding.get(from))
				.addCriterion("has_"+toPath(MetalDecoration.steelScaffolding.get(from)), hasItem(MetalDecoration.steelScaffolding.get(from)))
				.build(out, toRL("steel_scaffolding_"+to.name().toLowerCase()+"_from_"+from.name().toLowerCase()));
		}

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD), 6)
			.patternLine("iii")
			.patternLine(" s ")
			.patternLine("s s")
			.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
			.key('s', IETags.aluminumRod)
			.addCriterion("has_alu_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
			.addCriterion("has_alu_sticks", hasItem(IETags.aluminumRod))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.slopeAlu)
			.patternLine("sss")
			.patternLine("ss ")
			.patternLine("s  ")
			.key('s', MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD))
			.addCriterion("has_"+toPath(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD)), hasItem(MetalDecoration.aluScaffolding.get(MetalScaffoldingType.STANDARD)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD), 6)
			.patternLine("iii")
			.patternLine(" s ")
			.patternLine("s s")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('s', IETags.steelRod)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_steel_sticks", hasItem(IETags.steelRod))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.slopeSteel)
			.patternLine("sss")
			.patternLine("ss ")
			.patternLine("s  ")
			.key('s', MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))
			.addCriterion("has_"+toPath(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)), hasItem(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.aluFence, 3)
			.patternLine("isi")
			.patternLine("isi")
			.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
			.key('s', IETags.aluminumRod)
			.addCriterion("has_alu_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
			.addCriterion("has_alu_sticks", hasItem(IETags.aluminumRod))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.steelFence, 3)
			.patternLine("isi")
			.patternLine("isi")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('s', IETags.steelRod)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_steel_sticks", hasItem(IETags.steelRod))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.lvCoil)
			.patternLine("www")
			.patternLine("wiw")
			.patternLine("www")
			.key('i', Tags.Items.INGOTS_IRON)
			.key('w', Misc.wireCoils.get(WireType.COPPER))
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.COPPER)), hasItem(Misc.wireCoils.get(WireType.COPPER)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.mvCoil)
			.patternLine("www")
			.patternLine("wiw")
			.patternLine("www")
			.key('i', Tags.Items.INGOTS_IRON)
			.key('w', Misc.wireCoils.get(WireType.ELECTRUM))
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.ELECTRUM)), hasItem(Misc.wireCoils.get(WireType.ELECTRUM)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.hvCoil)
			.patternLine("www")
			.patternLine("wiw")
			.patternLine("www")
			.key('i', Tags.Items.INGOTS_IRON)
			.key('w', Misc.wireCoils.get(WireType.STEEL))
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.STEEL)), hasItem(Misc.wireCoils.get(WireType.STEEL)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.engineeringRS, 2)
			.patternLine("iri")
			.patternLine("rcr")
			.patternLine("iri")
			.key('i', Tags.Items.INGOTS_IRON)
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.addCriterion("has_redstone", hasItem(Items.REDSTONE))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.engineeringLight, 2)
			.patternLine("igi")
			.patternLine("ccc")
			.patternLine("igi")
			.key('i', Tags.Items.INGOTS_IRON)
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('g', Ingredients.componentIron)
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.addCriterion("has_component_iron", hasItem(Ingredients.componentIron))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.engineeringHeavy, 2)
			.patternLine("igi")
			.patternLine("pep")
			.patternLine("igi")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('e', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
			.key('g', Ingredients.componentSteel)
			.key('p', Blocks.PISTON)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
			.addCriterion("has_component_steel", hasItem(Ingredients.componentSteel))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.generator, 2)
			.patternLine("iii")
			.patternLine("ede")
			.patternLine("iii")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('e', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
			.key('d', MetalDevices.dynamo)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
			.addCriterion("has_"+toPath(MetalDevices.dynamo), hasItem(MetalDevices.dynamo))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.radiator, 2)
			.patternLine("ici")
			.patternLine("cbc")
			.patternLine("ici")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('b', Items.WATER_BUCKET)
			.addCriterion("has_water_bucket", hasItem(Items.WATER_BUCKET))
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.aluPost)
			.patternLine("f")
			.patternLine("f")
			.patternLine("s")
			.key('f', MetalDecoration.aluFence)
			.key('s', Blocks.STONE_BRICKS)
			.addCriterion("has_"+toPath(MetalDecoration.aluFence), hasItem(MetalDecoration.aluFence))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.steelPost)
			.patternLine("f")
			.patternLine("f")
			.patternLine("s")
			.key('f', MetalDecoration.steelFence)
			.key('s', Blocks.STONE_BRICKS)
			.addCriterion("has_"+toPath(MetalDecoration.steelFence), hasItem(MetalDecoration.steelFence))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.aluWallmount)
			.patternLine("ii")
			.patternLine("is")
			.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
			.key('s', IETags.aluminumRod)
			.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDecoration.steelWallmount)
			.patternLine("ii")
			.patternLine("is")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('s', IETags.steelRod)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
	}

	private void recipesMetalDevices(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.razorWire, 3)
			.patternLine("psp")
			.patternLine("sss")
			.patternLine("psp")
			.key('s', Ingredients.wireSteel)
			.key('p', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.addCriterion("has_"+toPath(Ingredients.wireSteel), hasItem(Ingredients.wireSteel))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.capacitorLV)
			.patternLine("iii")
			.patternLine("clc")
			.patternLine("wrw")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('l', IETags.getTagsFor(EnumMetals.LEAD).ingot)
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.addCriterion("has_lead_ingot", hasItem(IETags.getTagsFor(EnumMetals.LEAD).ingot))
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.capacitorMV)
			.patternLine("iii")
			.patternLine("ele")
			.patternLine("wrw")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('l', IETags.getTagsFor(EnumMetals.LEAD).ingot)
			.key('e', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('r', makeIngredient(Tags.Blocks.STORAGE_BLOCKS_REDSTONE))
			.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
			.addCriterion("has_lead_ingot", hasItem(IETags.getTagsFor(EnumMetals.LEAD).ingot))
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.capacitorHV)
			.patternLine("sss")
			.patternLine("ala")
			.patternLine("wrw")
			.key('s', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('a', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
			.key('l', IEBlocks.Metals.storage.get(EnumMetals.LEAD))
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('r', makeIngredient(Tags.Blocks.STORAGE_BLOCKS_REDSTONE))
			.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.barrel)
			.patternLine("sss")
			.patternLine("w w")
			.patternLine("www")
			.key('w', IETags.getItemTag(IETags.getTagsFor(EnumMetals.IRON).sheetmetal))
			.key('s', IEBlocks.toSlab.get(IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON)))
			.addCriterion("has_iron_sheet_slab", hasItem(IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.fluidPump)
			.patternLine(" i ")
			.patternLine("ici")
			.patternLine("ppp")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('c', Ingredients.componentIron)
			.key('p', IEBlocks.MetalDevices.fluidPipe)
			.addCriterion("has_"+toPath(IEBlocks.MetalDevices.fluidPipe), hasItem(IEBlocks.MetalDevices.fluidPipe))
			.build(out);
		// TODO Uncomment below when fluidPlacer is implemented
//		ShapedRecipeBuilder.shapedRecipe(MetalDevices.fluidPlacer)
//			.patternLine("cbc")
//			.patternLine("b b")
//			.patternLine("cbc")
//			.key('c', IETags.getTagsFor(EnumMetals.IRON).plate)
//			.key('b', Blocks.IRON_BARS)
//			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
//			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.blastFurnacePreheater)
			.patternLine("sss")
			.patternLine("s s")
			.patternLine("shs")
			.key('s', IEBlocks.Metals.sheetmetal.get(EnumMetals.IRON))
			.key('h', MetalDevices.furnaceHeater)
			.addCriterion("has_"+toPath(MetalDevices.furnaceHeater), hasItem(MetalDevices.furnaceHeater))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.furnaceHeater)
			.patternLine("ici")
			.patternLine("clc")
			.patternLine("iri")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('l', MetalDecoration.lvCoil)
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.dynamo)
			.patternLine("rlr")
			.patternLine("iii")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('l', MetalDecoration.lvCoil)
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.thermoelectricGen)
			.patternLine("iii")
			.patternLine("ele")
			.patternLine("eee")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('l', MetalDecoration.lvCoil)
			.key('e', IETags.getTagsFor(EnumMetals.ELECTRUM).plate)
			.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.electricLantern)
			.patternLine(" i ")
			.patternLine("pep")
			.patternLine("iri")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('e', Ingredients.electronTube)
			.key('p', makeIngredient(Tags.Blocks.GLASS_PANES))
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.addCriterion("has_"+toPath(Ingredients.electronTube), hasItem(Ingredients.electronTube))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.chargingStation)
			.patternLine("ici")
			.patternLine("ggg")
			.patternLine("wlw")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('l', MetalDecoration.lvCoil)
			.key('g', makeIngredient(Tags.Blocks.GLASS))
			.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.fluidPipe, 8)
			.patternLine("ppp")
			.patternLine("   ")
			.patternLine("ppp")
			.key('p', IETags.getTagsFor(EnumMetals.IRON).plate)
			.addCriterion("has_iron_plate", hasItem(IETags.getTagsFor(EnumMetals.IRON).plate))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.sampleDrill)
			.patternLine("sfs")
			.patternLine("sfs")
			.patternLine("efe")
			.key('s', MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD))
			.key('f', MetalDecoration.steelFence)
			.key('e', MetalDecoration.engineeringLight)
			.addCriterion("has_"+toPath(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)), hasItem(MetalDecoration.steelScaffolding.get(MetalScaffoldingType.STANDARD)))
			.addCriterion("has_"+toPath(MetalDecoration.steelFence), hasItem(MetalDecoration.steelFence))
			.addCriterion("has_"+toPath(MetalDecoration.engineeringLight), hasItem(MetalDecoration.engineeringLight))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.teslaCoil)
			.patternLine("iii")
			.patternLine(" l ")
			.patternLine("hlh")
			.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
			.key('l', MetalDecoration.lvCoil)
			.key('h', MetalDevices.capacitorHV)
			.addCriterion("has_"+toPath(MetalDevices.capacitorHV), hasItem(MetalDevices.capacitorHV))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.floodlight)
			.patternLine("iii")
			.patternLine("pel")
			.patternLine("ici")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('l', MetalDecoration.lvCoil)
			.key('e', Ingredients.electronTube)
			.key('c', Ingredients.componentIron)
			.key('p', makeIngredient(Tags.Blocks.GLASS_PANES))
			.addCriterion("has_"+toPath(Ingredients.electronTube), hasItem(Ingredients.electronTube))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.turretChem)
			.patternLine(" s ")
			.patternLine(" gc")
			.patternLine("bte")
			.key('s', Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
			.key('g', Weapons.chemthrower)
			.key('c', Ingredients.circuitBoard)
			.key('b', MetalDevices.barrel)
			.key('t', WoodenDevices.turntable)
			.key('e', MetalDecoration.engineeringRS)
			.addCriterion("has_"+toPath(Weapons.chemthrower), hasItem(Weapons.chemthrower))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.turretGun)
			.patternLine(" s ")
			.patternLine(" gc")
			.patternLine("bte")
			.key('s', Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
			.key('g', Weapons.revolver)
			.key('c', Ingredients.circuitBoard)
			.key('b', Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))
			.key('t', WoodenDevices.turntable)
			.key('e', MetalDecoration.engineeringRS)
			.addCriterion("has_"+toPath(Weapons.revolver), hasItem(Weapons.revolver))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(MetalDevices.belljar)
			.patternLine("geg")
			.patternLine("g g")
			.patternLine("wcw")
			.key('g', makeIngredient(Tags.Blocks.GLASS))
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('e', Ingredients.electronTube)
			.key('c', Ingredients.componentIron)
			.addCriterion("has_"+toPath(Ingredients.electronTube), hasItem(Ingredients.electronTube))
			.build(out);
	}

	private void recipesConnectors(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.breakerswitch)
			.patternLine(" l ")
			.patternLine("cic")
			.key('l', Items.LEVER)
			.key('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('c', Blocks.TERRACOTTA)
			.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.COPPER)), hasItem(Misc.wireCoils.get(WireType.COPPER)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.redstoneBreaker)
			.patternLine("h h")
			.patternLine("ici")
			.patternLine("iri")
			.key('h', IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false))
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('c', Items.REPEATER)
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.addCriterion("has_hv_connector", hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.energyMeter)
			.patternLine(" m ")
			.patternLine("bcb")
			.patternLine("ici")
			.key('m', IEItems.Tools.voltmeter)
			.key('b', Blocks.TERRACOTTA)
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('c', MetalDecoration.lvCoil)
			.addCriterion("has_voltmeter", hasItem(IEItems.Tools.voltmeter))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.transformer)
			.patternLine("l m")
			.patternLine("ibi")
			.patternLine("iii")
			.key('l', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
			.key('m', IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false))
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('b', MetalDecoration.mvCoil)
			.addCriterion("has_mv_connector", hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.transformerHV)
			.patternLine("m h")
			.patternLine("ibi")
			.patternLine("iii")
			.key('m', IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false))
			.key('h', IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false))
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('b', MetalDecoration.hvCoil)
			.addCriterion("has_hv_connector", hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.connectorStructural, 8)
			.patternLine("isi")
			.patternLine("i i")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('s', IETags.steelRod)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.connectorRedstone, 4)
			.patternLine("iii")
			.patternLine("brb")
			.key('i', IETags.getTagsFor(EnumMetals.ELECTRUM).nugget)
			.key('b', Blocks.TERRACOTTA)
			.key('r', Tags.Items.DUSTS_REDSTONE)
			.addCriterion("has_electrum_nugget", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).nugget))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.connectorProbe)
			.patternLine(" c ")
			.patternLine("gpg")
			.patternLine(" q ")
			.key('c', IEBlocks.Connectors.connectorRedstone)
			.key('g', Tags.Items.GLASS_PANES)
			.key('p', IETags.getTagsFor(EnumMetals.ELECTRUM).ingot)
			.key('q', Tags.Items.GEMS_QUARTZ)
			.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
			.build(out);

		// Connectors and Relays
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), 4)
			.patternLine(" i ")
			.patternLine("cic")
			.patternLine("cic")
			.key('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('c', Blocks.TERRACOTTA)
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), 8)
			.patternLine(" i ")
			.patternLine("cic")
			.key('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('c', Blocks.TERRACOTTA)
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), 4)
			.patternLine(" i ")
			.patternLine("cic")
			.patternLine("cic")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('c', Blocks.TERRACOTTA)
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), 8)
			.patternLine(" i ")
			.patternLine("cic")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('c', Blocks.TERRACOTTA)
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), 4)
			.patternLine(" i ")
			.patternLine("cic")
			.patternLine("cic")
			.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
			.key('c', Blocks.TERRACOTTA)
			.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), 8)
			.patternLine(" i ")
			.patternLine("cic")
			.patternLine("cic")
			.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
			.key('c', StoneDecoration.insulatingGlass)
			.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
	}

	private void recipesCloth(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Cloth.balloon, 2)
			.patternLine(" f ")
			.patternLine("ftf")
			.patternLine(" s ")
			.key('f', IEItems.Ingredients.hempFabric)
			.key('t', Items.TORCH)
			.key('s', IETags.getItemTag(IETags.treatedWoodSlab))
			.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Cloth.cushion, 3)
			.patternLine("fff")
			.patternLine("f f")
			.patternLine("fff")
			.key('f', IEItems.Ingredients.hempFabric)
			.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Cloth.curtain, 3)
			.patternLine("sss")
			.patternLine("fff")
			.patternLine("fff")
			.key('s', IETags.ironRod)
			.key('f', IEItems.Ingredients.hempFabric)
			.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
			.addCriterion("has_iron_rod", hasItem(IETags.ironRod))
			.setGroup("strip_curtain")
			.build(out, toRL(toPath(IEBlocks.Cloth.curtain)+"_with_iron_rod"));
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Cloth.curtain, 3)
			.patternLine("sss")
			.patternLine("fff")
			.patternLine("fff")
			.key('s', IETags.aluminumRod)
			.key('f', IEItems.Ingredients.hempFabric)
			.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
			.addCriterion("has_alu_rod", hasItem(IETags.aluminumRod))
			.setGroup("strip_curtain")
			.build(out, toRL(toPath(IEBlocks.Cloth.curtain)+"_with_alu_rod"));
		ShapedRecipeBuilder.shapedRecipe(IEBlocks.Cloth.curtain, 3)
			.patternLine("sss")
			.patternLine("fff")
			.patternLine("fff")
			.key('s', IETags.steelRod)
			.key('f', IEItems.Ingredients.hempFabric)
			.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
			.addCriterion("has_steel_rod", hasItem(IETags.steelRod))
			.setGroup("strip_curtain")
			.build(out, toRL(toPath(IEBlocks.Cloth.curtain)+"_with_steel_rod"));
	}

	private void recipesTools(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Tools.hammer)
			.patternLine(" if")
			.patternLine(" si")
			.patternLine("s  ")
			.key('s', Tags.Items.RODS_WOODEN)
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('f', Tags.Items.STRING)
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.wirecutter)
			.patternLine("si")
			.patternLine(" s")
			.key('s', Tags.Items.RODS_WOODEN)
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Tools.manual)
			.addIngredient(Items.BOOK)
			.addIngredient(Items.LEVER)
			.addCriterion("has_book", hasItem(Items.BOOK))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.steelAxe)
			.patternLine("ii")
			.patternLine("is")
			.patternLine(" s")
			.key('s', IETags.treatedStick)
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.steelPick)
			.patternLine("iii")
			.patternLine(" s ")
			.patternLine(" s ")
			.key('s', IETags.treatedStick)
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.steelShovel)
			.patternLine("i")
			.patternLine("s")
			.patternLine("s")
			.key('s', IETags.treatedStick)
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.steelSword)
			.patternLine("i")
			.patternLine("i")
			.patternLine("s")
			.key('s', IETags.treatedStick)
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.toolbox)
			.patternLine("ppp")
			.patternLine("rcr")
			.key('p', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
			.key('r', Tags.Items.DYES_RED)
			.key('c', IEBlocks.WoodenDevices.crate)
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.addCriterion("has_red_dye", hasItem(Items.RED_DYE))
			.addCriterion("has_"+toPath(IEBlocks.WoodenDevices.crate), hasItem(IEBlocks.WoodenDevices.crate))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.voltmeter)
			.patternLine(" p ")
			.patternLine("scs")
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('p', Items.COMPASS)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.addCriterion("has_compass", hasItem(Items.COMPASS))
			.addCriterion("has_treated_sticks", hasItem(IETags.treatedStick))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(Tools.drill)
			.patternLine("  g")
			.patternLine(" hg")
			.patternLine("c  ")
			.key('g', Ingredients.woodenGrip)
			.key('h', MetalDecoration.engineeringHeavy)
			.key('c', Ingredients.componentIron)
			.addCriterion("has_"+toPath(MetalDecoration.engineeringHeavy), hasItem(MetalDecoration.engineeringHeavy))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.drillheadIron)
			.patternLine("ii ")
			.patternLine("bbi")
			.patternLine("ii ")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('b', makeIngredient(IETags.getTagsFor(EnumMetals.IRON).storage))
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Tools.drillheadSteel)
			.patternLine("ii ")
			.patternLine("bbi")
			.patternLine("ii ")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('b', makeIngredient(IETags.getTagsFor(EnumMetals.STEEL).storage))
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
	}

	private void recipesIngredients(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickTreated, 4)
			.patternLine("w")
			.patternLine("w")
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.setGroup("sticks")
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickIron, 4)
			.patternLine("i")
			.patternLine("i")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.setGroup("sticks")
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickSteel, 4)
			.patternLine("i")
			.patternLine("i")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.setGroup("sticks")
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.stickAluminum, 4)
			.patternLine("i")
			.patternLine("i")
			.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).ingot)
			.setGroup("sticks")
			.addCriterion("has_alu_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.hempFabric)
			.patternLine("fff")
			.patternLine("fsf")
			.patternLine("fff")
			.key('f', IETags.fiberHemp)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_hemp_fiber", hasItem(IETags.fiberHemp))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(Ingredients.componentIron)
			.patternLine("i i")
			.patternLine(" c ")
			.patternLine("i i")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.addCriterion("has_iron_ingot", hasItem(IETags.getTagsFor(EnumMetals.IRON).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.componentSteel)
			.patternLine("i i")
			.patternLine(" c ")
			.patternLine("i i")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).plate)
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.waterwheelSegment)
			.patternLine(" s ")
			.patternLine("sbs")
			.patternLine("bsb")
			.key('s', IETags.treatedStick)
			.key('b', IETags.getItemTag(IETags.treatedWood))
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.windmillBlade)
			.patternLine("bb ")
			.patternLine("ssb")
			.patternLine("ss ")
			.key('s', IETags.treatedStick)
			.key('b', IETags.getItemTag(IETags.treatedWood))
			.addCriterion("has_treated_planks", hasItem(IETags.getItemTag(IETags.treatedWood)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.windmillSail)
			.patternLine(" cc")
			.patternLine("ccc")
			.patternLine(" c ")
			.key('c', IETags.fabricHemp)
			.addCriterion("has_hemp_fabric", hasItem(IETags.fabricHemp))
			.build(out);
		
		ShapedRecipeBuilder.shapedRecipe(Ingredients.woodenGrip)
			.patternLine("ss")
			.patternLine("cs")
			.patternLine("ss")
			.key('s', IETags.treatedStick)
			.key('c', IETags.getTagsFor(EnumMetals.COPPER).nugget)
			.addCriterion("has_treated_sticks", hasItem(IETags.treatedStick))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartBarrel)
			.patternLine("ss")
			.key('s', IETags.steelRod)
			.addCriterion("has_"+toPath(Ingredients.stickSteel), hasItem(IETags.steelRod))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartDrum)
			.patternLine(" i ")
			.patternLine("ici")
			.patternLine(" i ")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('c', Ingredients.componentSteel)
			.addCriterion("has_ingot_steel", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Ingredients.gunpartHammer)
			.patternLine("i  ")
			.patternLine("ii ")
			.patternLine(" ii")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.addCriterion("has_ingot_steel", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.DRILL_WATERPROOF))
			.patternLine("bl ")
			.patternLine("lbl")
			.patternLine(" lc")
			.key('b', Items.BUCKET)
			.key('l', Tags.Items.GEMS_LAPIS)
			.key('c', Ingredients.componentIron)
			.addCriterion("has_bucket", hasItem(Items.BUCKET))
			.build(out);
//		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.DRILL_LUBE))
//			.patternLine("bi ")
//			.patternLine("ibi")
//			.patternLine(" iv")
//			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
//			.key('c', Ingredients.componentIron)
//			.key('b', Ingredients.plantOil) // TODO Plant Oil Bucket missing for ToolUpgrade.DRILL_LUBE recipe
//			.addCriterion("has_"+toPath(Ingredients.plantOil), hasItem(Ingredients.plantOil))
//			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.DRILL_DAMAGE))
			.patternLine("iii")
			.patternLine(" c ")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('c', Ingredients.componentIron)
			.addCriterion("has_"+toPath(Ingredients.componentIron), hasItem(Ingredients.componentIron))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY))
			.patternLine("ci ")
			.patternLine("ibr")
			.patternLine(" rb")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('c', Ingredients.componentIron)
			.key('r', Tags.Items.DYES_RED)
			.key('b', Items.BUCKET)
			.addCriterion("has_bucket", hasItem(Items.BUCKET))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_BAYONET))
			.patternLine("si")
			.patternLine("iw")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('w', IETags.getItemTag(IETags.treatedWood))
			.key('s', Items.IRON_SWORD)
			.addCriterion("has_iron_sword", hasItem(Items.IRON_SWORD))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_MAGAZINE))
			.patternLine(" ai")
			.patternLine("a a")
			.patternLine("ca ")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('a', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('c', Ingredients.componentIron)
			.addCriterion("has_"+toPath(Ingredients.componentIron), hasItem(Ingredients.componentIron))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.REVOLVER_ELECTRO))
			.patternLine("eee")
			.patternLine("rwr")
			.key('e', Ingredients.electronTube)
			.key('r', IETags.steelRod)
			.key('w', Ingredients.wireCopper)
			.addCriterion("has_"+toPath(Ingredients.electronTube), hasItem(Ingredients.electronTube))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_FOCUS))
			.patternLine(" ii")
			.patternLine("pph")
			.patternLine(" ii")
			.key('i', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.key('p', MetalDevices.fluidPipe)
			.key('h', Items.HOPPER)
			.addCriterion("has_"+toPath(MetalDevices.fluidPipe), hasItem(MetalDevices.fluidPipe))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.CHEMTHROWER_MULTITANK))
			.patternLine(" p ")
			.patternLine("tct")
			.key('p', MetalDevices.fluidPipe)
			.key('c', Ingredients.componentIron)
			.key('t', Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY))
			.addCriterion("has_"+toPath(Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY)), hasItem(Misc.toolUpgrades.get(ToolUpgrade.DRILL_CAPACITY)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_SCOPE))
			.patternLine("pi ")
			.patternLine("i i")
			.patternLine(" ip")
			.key('i', IETags.getTagsFor(EnumMetals.COPPER).ingot)
			.key('p', makeIngredient(Tags.Blocks.GLASS_PANES))
			.addCriterion("has_ingot_copper", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.RAILGUN_CAPACITORS))
			.patternLine("www")
			.patternLine("ccc")
			.key('w', Misc.wireCoils.get(WireType.STEEL))
			.key('c', MetalDevices.capacitorHV)
			.addCriterion("has_"+toPath(Misc.wireCoils.get(WireType.STEEL)), hasItem(Misc.wireCoils.get(WireType.STEEL)))
			.build(out);

		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_FLASH))
			.patternLine("ipi")
			.patternLine("pep")
			.key('i', IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
			.key('p', makeIngredient(Tags.Blocks.GLASS_PANES))
			.key('e', Ingredients.electronTube)
			.addCriterion("has_"+toPath(Ingredients.electronTube), hasItem(Ingredients.electronTube))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_SHOCK))
			.patternLine("crc")
			.patternLine("crc")
			.patternLine("crc")
			.key('r', IETags.ironRod)
			.key('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
			.addCriterion("has_"+toPath(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)), hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.toolUpgrades.get(ToolUpgrade.SHIELD_MAGNET))
			.patternLine("  l")
			.patternLine("lc ")
			.patternLine("lil")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('l', Tags.Items.LEATHER)
			.key('c', MetalDecoration.lvCoil)
			.addCriterion("has_"+toPath(MetalDecoration.lvCoil), hasItem(MetalDecoration.lvCoil))
			.build(out);

		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireCopper)
			.addIngredient(IETags.getTagsFor(EnumMetals.COPPER).plate)
			.addIngredient(Ingredient.fromItems(Items.SHEARS, Tools.wirecutter))
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireElectrum)
			.addIngredient(IETags.getTagsFor(EnumMetals.ELECTRUM).plate)
			.addIngredient(Ingredient.fromItems(Items.SHEARS, Tools.wirecutter))
			.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
			.build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireAluminum)
			.addIngredient(IETags.getTagsFor(EnumMetals.ALUMINUM).plate)
			.addIngredient(Ingredient.fromItems(Items.SHEARS, Tools.wirecutter))
			.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
			.build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Ingredients.wireSteel)
			.addIngredient(IETags.getTagsFor(EnumMetals.STEEL).plate)
			.addIngredient(Ingredient.fromItems(Items.SHEARS, Tools.wirecutter))
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapelessRecipeBuilder.shapelessRecipe(Items.GUNPOWDER)
			.addIngredient(Ingredient.fromTag(IETags.saltpeterDust), 4)
			.addIngredient(IETags.sulfurDust)
			.addIngredient(Items.CHARCOAL)
			.addCriterion("has_sulfur", hasItem(IETags.sulfurDust))
			.build(out, toRL("gunpowder_from_dusts"));
	}

	private void recipesWeapons(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Weapons.chemthrower)
			.patternLine(" tg")
			.patternLine(" hg")
			.patternLine("pb ")
			.key('g', Ingredients.woodenGrip)
			.key('p', MetalDevices.fluidPipe)
			.key('h', MetalDecoration.engineeringHeavy)
			.key('t', Misc.toolUpgrades.get(ToolUpgrade.DRILL_WATERPROOF))
			.key('b', Items.BUCKET)
			.addCriterion("has_"+toPath(Ingredients.woodenGrip), hasItem(Ingredients.woodenGrip))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Weapons.railgun)
			.patternLine(" vg")
			.patternLine("cbv")
			.patternLine("bc ")
			.key('g', Ingredients.woodenGrip)
			.key('b', Ingredients.gunpartBarrel)
			.key('v', MetalDevices.capacitorHV)
			.key('c', MetalDecoration.mvCoil)
			.addCriterion("has_"+toPath(MetalDevices.capacitorHV), hasItem(MetalDevices.capacitorHV))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Weapons.revolver)
			.patternLine(" i ")
			.patternLine("bdh")
			.patternLine("gig")
			.key('b', Ingredients.gunpartBarrel)
			.key('d', Ingredients.gunpartDrum)
			.key('h', Ingredients.gunpartHammer)
			.key('g', Ingredients.woodenGrip)
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.addCriterion("has_"+toPath(Ingredients.woodenGrip), hasItem(Ingredients.woodenGrip))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Weapons.speedloader)
			.patternLine("  i")
			.patternLine("iis")
			.patternLine("  i")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).ingot)
			.key('s', IETags.getTagsFor(EnumMetals.STEEL).ingot)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
	}

	private void recipesMisc(@Nonnull Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.COPPER), 4)
			.patternLine(" w ")
			.patternLine("wsw")
			.patternLine(" w ")
			.key('w', IETags.copperWire)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.ELECTRUM), 4)
			.patternLine(" w ")
			.patternLine("wsw")
			.patternLine(" w ")
			.key('w', IETags.electrumWire)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_electrum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ELECTRUM).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.STEEL), 4)
			.patternLine(" w ")
			.patternLine("asa")
			.patternLine(" w ")
			.key('w', IETags.steelWire)
			.key('a', IETags.aluminumWire)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.STEEL), 4)
			.patternLine(" a ")
			.patternLine("wsw")
			.patternLine(" a ")
			.key('w', IETags.steelWire)
			.key('a', IETags.aluminumWire)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out, toRL("wirecoil_steel2"));
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.STRUCTURE_ROPE), 4)
			.patternLine(" w ")
			.patternLine("wsw")
			.patternLine(" w ")
			.key('w', Ingredients.hempFiber)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_hemp_fiber", hasItem(Ingredients.hempFiber))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.STRUCTURE_STEEL), 4)
			.patternLine(" w ")
			.patternLine("wsw")
			.patternLine(" w ")
			.key('w', Ingredients.wireSteel)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_steel_ingot", hasItem(IETags.getTagsFor(EnumMetals.STEEL).ingot))
			.build(out);
		addCornerStraightMiddle(Misc.wireCoils.get(WireType.COPPER_INSULATED), 4, IETags.fabricHemp, Misc.wireCoils.get(WireType.COPPER), IETags.fabricHemp, out);
		addCornerStraightMiddle(Misc.wireCoils.get(WireType.ELECTRUM_INSULATED), 4, IETags.fabricHemp, Misc.wireCoils.get(WireType.ELECTRUM), IETags.fabricHemp, out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.REDSTONE), 4)
			.patternLine(" w ")
			.patternLine("asa")
			.patternLine(" w ")
			.key('w', IETags.aluminumWire)
			.key('a', Tags.Items.DUSTS_REDSTONE)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
			.build(out);
		ShapedRecipeBuilder.shapedRecipe(Misc.wireCoils.get(WireType.REDSTONE), 4)
			.patternLine(" a ")
			.patternLine("wsw")
			.patternLine(" a ")
			.key('w', IETags.aluminumWire)
			.key('a', Tags.Items.DUSTS_REDSTONE)
			.key('s', Tags.Items.RODS_WOODEN)
			.addCriterion("has_aluminum_ingot", hasItem(IETags.getTagsFor(EnumMetals.ALUMINUM).ingot))
			.build(out, toRL("wirecoil_redstone2"));

		ShapedRecipeBuilder.shapedRecipe(Misc.jerrycan)
			.patternLine(" ii")
			.patternLine("ibb")
			.patternLine("ibb")
			.key('i', IETags.getTagsFor(EnumMetals.IRON).plate)
			.key('b', Items.BUCKET)
			.addCriterion("has_bucket", hasItem(Items.BUCKET))
			.build(out, toRL("jerrycan"));
		ShapedRecipeBuilder.shapedRecipe(Misc.powerpack)
			.patternLine("lbl")
			.patternLine("wcw")
			.key('l', Tags.Items.LEATHER)
			.key('w', Misc.wireCoils.get(WireType.COPPER))
			.key('b', MetalDevices.capacitorLV)
			.key('c', IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false))
			.addCriterion("has_leather", hasItem(Items.LEATHER))
			.addCriterion("has_copper_ingot", hasItem(IETags.getTagsFor(EnumMetals.COPPER).ingot))
			.addCriterion("has_"+toPath(MetalDevices.capacitorLV), hasItem(MetalDevices.capacitorLV))
			.addCriterion("has_"+toPath(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)), hasItem(IEBlocks.Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)))
			.build(out);
		// TODO Uncomment below when maintenanceKit is implemented
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.maintenanceKit)
//			.patternLine("rc ")
//			.patternLine("fff")
//			.key('c', Ingredient.fromItems(Items.SHEARS, Tools.wirecutter))
//			.key('r', IETags.ironRod)
//			.key('f', IETags.fabricHemp)
//			.addCriterion("has_"+toPath(Tools.wirecutter)), hasItem(Tools.wirecutter))
//			.build(out);
		// TODO Uncomment below when shield is implemented
//		ShapedRecipeBuilder.shapedRecipe(IEItems.Misc.shield)
//			.patternLine("sws")
//			.patternLine("scs")
//			.patternLine("sws")
//			.key('s', IETags.getTagsFor(EnumMetals.STEEL).plate)
//			.key('w', IETags.getItemTag(IETags.treatedWood))
//			.key('c', Items.SHIELD)
//			.addCriterion("has_shield", hasItem(Items.SHIELD))
//			.build(out);
	}

	private void add3x3Conversion(IItemProvider bigItem, Tag<Item> bigTag, IItemProvider smallItem, Tag<Item> smallTag, Consumer<IFinishedRecipe> out)
	{
		ShapedRecipeBuilder.shapedRecipe(bigItem)
			.key('s', smallTag)
			.patternLine("sss")
			.patternLine("sss")
			.patternLine("sss")
			.addCriterion("has_"+toPath(smallItem), hasItem(smallItem))
			.build(out, toRL(toPath(smallItem)+"_to_")+toPath(bigItem));
		ShapelessRecipeBuilder.shapelessRecipe(smallItem, 9)
			.addIngredient(bigTag)
			.addCriterion("has_"+toPath(bigItem), hasItem(smallItem))
			.build(out, toRL(toPath(bigItem)+"_to_"+toPath(smallItem)));
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
	 * @param input        the recipe's input
	 * @param output       the recipe's output
	 * @param xp           experience awarded per smelted item
	 * @param smeltingTime smelting time in ticks
	 * @param extraPostfix adds an additional postfix before the smelting/blasting postfix when needed (for example used by dusts)
	 * @param smeltPostfix allows adding the smelting postfix to the smelting (non-blasting) recipe
	 */
	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, int smeltingTime, Consumer<IFinishedRecipe> out, String extraPostfix, boolean smeltPostfix)
	{
		CookingRecipeBuilder.smeltingRecipe(Ingredient.fromItems(input), output, xp, smeltingTime).addCriterion("has_"+toPath(input), hasItem(input)).build(out, toRL(toPath(output)+extraPostfix+(smeltPostfix?"_from_smelting": "")));
		CookingRecipeBuilder.blastingRecipe(Ingredient.fromItems(input), output, xp, smeltingTime/blastDivider).addCriterion("has_"+toPath(input), hasItem(input)).build(out, toRL(toPath(output)+extraPostfix+"_from_blasting"));
	}

	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, Consumer<IFinishedRecipe> out)
	{
		addStandardSmeltingBlastingRecipe(input, output, xp, out, "");
	}

	private void addStandardSmeltingBlastingRecipe(IItemProvider input, IItemProvider output, float xp, Consumer<IFinishedRecipe> out, String extraPostfix)
	{
		addStandardSmeltingBlastingRecipe(input, output, xp, standardSmeltingTime, out, extraPostfix, false);
	}
}
