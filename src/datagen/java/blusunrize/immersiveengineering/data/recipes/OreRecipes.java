/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Metals;
import blusunrize.immersiveengineering.common.register.IEItems.Molds;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.data.recipes.builder.*;
import blusunrize.immersiveengineering.data.resources.RecipeMetals;
import blusunrize.immersiveengineering.data.resources.RecipeMetals.AlloyProperties;
import blusunrize.immersiveengineering.data.resources.RecipeOres;
import blusunrize.immersiveengineering.data.resources.SecondaryOutput;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.concurrent.CompletableFuture;

import static blusunrize.immersiveengineering.api.utils.TagUtils.createItemWrapper;

public class OreRecipes extends IERecipeProvider
{
	public OreRecipes(PackOutput p_248933_, CompletableFuture<Provider> provider)
	{
		super(p_248933_, provider);
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		makeMaterialRecipes(out);
		/* Common Metals */
		for(RecipeMetals metal : RecipeMetals.values())
		{
			if(metal.getOre()!=null)
			{
				SecondaryOutput[] secondaryOutputs = metal.getSecondaryOutputs();

				// Hammer crushing
				HammerCrushingRecipeBuilder hammerBuilder = HammerCrushingRecipeBuilder.builder(metal.getOre(), metal.getDust());
				if(!metal.isNative())
					hammerBuilder.addCondition(getTagCondition(metal.getDust()))
							.addCondition(getTagCondition(metal.getOre()));
				hammerBuilder.build(out, toRL("crafting/hammercrushing_"+metal.getName()));

				HammerCrushingRecipeBuilder rawHammerBuilder = HammerCrushingRecipeBuilder.builder(metal.getRawOre(), metal.getDust());
				if(!metal.isNative())
					rawHammerBuilder.addCondition(getTagCondition(metal.getDust()))
							.addCondition(getTagCondition(metal.getRawOre()));
				rawHammerBuilder.build(out, toRL("crafting/raw_hammercrushing_"+metal.getName()));

				// Crush ore
				CrusherRecipeBuilder oreCrushing = CrusherRecipeBuilder.builder()
						.output(metal.getDust(), 2)
						.input(metal.getOre())
						.setEnergy(6000);
				if(!metal.isNative())
					oreCrushing.addCondition(getTagCondition(metal.getDust()))
							.addCondition(getTagCondition(metal.getOre()));
				if(secondaryOutputs!=null)
					for(SecondaryOutput secondaryOutput : secondaryOutputs)
						oreCrushing.addSecondary(secondaryOutput.getItem(), secondaryOutput.getChance(), secondaryOutput.getConditions());
				oreCrushing.build(out, toRL("crusher/ore_"+metal.getName()));

				CrusherRecipeBuilder rawOreCrushing = CrusherRecipeBuilder.builder()
						.input(metal.getRawOre())
						.output(metal.getDust(), 1)
						.addSecondary(metal.getDust(), 1/3f)
						.setEnergy(6000);
				if(!metal.isNative())
					rawOreCrushing.addCondition(getTagCondition(metal.getDust()))
							.addCondition(getTagCondition(metal.getRawOre()));
				rawOreCrushing.build(out, toRL("crusher/raw_ore_"+metal.getName()));

				TagKey<Item> rawBlock = createItemWrapper(IETags.getRawBlock(metal.getName()));
				rawOreCrushing = CrusherRecipeBuilder.builder()
						.input(rawBlock)
						.output(metal.getDust(), 12)
						.setEnergy(9*6000);
				if(!metal.isNative())
					rawOreCrushing.addCondition(getTagCondition(metal.getDust())).addCondition(getTagCondition(rawBlock));
				rawOreCrushing.build(out, toRL("crusher/raw_block_"+metal.getName()));

				// Arcfurnace ore
				ArcFurnaceRecipeBuilder arcBuilder = ArcFurnaceRecipeBuilder.builder()
						.input(metal.getOre())
						.output(metal.getIngot(), 2)
						.slag(IETags.slag, 1)
						.setTime(200)
						.setEnergy(102400);
				if(!metal.isNative())
					arcBuilder.addCondition(getTagCondition(metal.getIngot())).addCondition(getTagCondition(metal.getOre()));
				arcBuilder.build(out, toRL("arcfurnace/ore_"+metal.getName()));

				// Arcfurnace raw ore
				arcBuilder = ArcFurnaceRecipeBuilder.builder()
						.input(metal.getRawOre())
						.output(metal.getIngot(), 1)
						.secondary(metal.getIngot(), 0.5F)
						.setTime(100)
						.setEnergy(25600);
				if(!metal.isNative())
					arcBuilder.addCondition(getTagCondition(metal.getIngot()))
							.addCondition(getTagCondition(metal.getRawOre()));
				arcBuilder.build(out, toRL("arcfurnace/raw_ore_"+metal.getName()));

				// Arcfurnace raw ore block
				arcBuilder = ArcFurnaceRecipeBuilder.builder()
						.input(metal.getRawBlock())
						.output(metal.getIngot(), 13)
						.secondary(metal.getIngot(), 0.5F)
						.setTime(9*100)
						.setEnergy(9*25600);
				if(!metal.isNative())
					arcBuilder.addCondition(getTagCondition(metal.getIngot()))
							.addCondition(getTagCondition(metal.getRawBlock()));
				arcBuilder.build(out, toRL("arcfurnace/raw_block_"+metal.getName()));
			}

			// Crush ingot
			CrusherRecipeBuilder ingotCrushing = CrusherRecipeBuilder.builder()
					.input(metal.getIngot())
					.output(metal.getDust(), 1)
					.setEnergy(3000);
			if(!metal.isNative())
				ingotCrushing.addCondition(getTagCondition(metal.getDust()))
						.addCondition(getTagCondition(metal.getIngot()));
			ingotCrushing.build(out, toRL("crusher/ingot_"+metal.getName()));

			// Arcfurnace dust
			ArcFurnaceRecipeBuilder arcBuilder = ArcFurnaceRecipeBuilder.builder()
					.input(metal.getDust())
					.output(metal.getIngot(), 1)
					.setTime(100)
					.setEnergy(51200);
			if(!metal.isNative())
				arcBuilder.addCondition(getTagCondition(metal.getIngot()))
						.addCondition(getTagCondition(metal.getDust()));
			arcBuilder.build(out, toRL("arcfurnace/dust_"+metal.getName()));

			// Plate
			TagKey<Item> plate = createItemWrapper(IETags.getPlate(metal.getName()));
			MetalPressRecipeBuilder pressBuilder = MetalPressRecipeBuilder.builder()
					.input(metal.getIngot())
					.mold(Molds.MOLD_PLATE)
					.output(plate, 1)
					.setEnergy(2400);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()))
						.addCondition(getTagCondition(plate));
			pressBuilder.build(out, toRL("metalpress/plate_"+metal.getName()));

			// Gear
			TagKey<Item> gear = createItemWrapper(IETags.getGear(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder()
					.input(metal.getIngot(), 4)
					.mold(Molds.MOLD_GEAR)
					.output(gear, 1)
					.setEnergy(2400);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()));
			pressBuilder.addCondition(getTagCondition(gear))
					.build(out, toRL("metalpress/gear_"+metal.getName()));

			// Rod
			TagKey<Item> rods = createItemWrapper(IETags.getRod(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder()
					.input(metal.getIngot())
					.mold(Molds.MOLD_ROD)
					.output(rods, 2)
					.setEnergy(2400);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()));
			pressBuilder.addCondition(getTagCondition(rods))
					.build(out, toRL("metalpress/rod_"+metal.getName()));

			// Wire
			TagKey<Item> wire = createItemWrapper(IETags.getWire(metal.getName()));
			pressBuilder = MetalPressRecipeBuilder.builder()
					.input(metal.getIngot())
					.mold(Molds.MOLD_WIRE)
					.output(wire, 2)
					.setEnergy(2400);
			if(!metal.isNative())
				pressBuilder.addCondition(getTagCondition(metal.getIngot()));
			pressBuilder.addCondition(getTagCondition(wire))
					.build(out, toRL("metalpress/wire_"+metal.getName()));

			AlloyProperties alloy = metal.getAlloyProperties();
			if(alloy!=null)
			{
				IngredientWithSize[] ingredients = alloy.getAlloyIngredients();
				if(alloy.isSimple())
				{
					AlloyRecipeBuilder alloyBuilder = AlloyRecipeBuilder.builder()
							.output(metal.getIngot(), alloy.getOutputSize());
					if(!metal.isNative())
						alloyBuilder.addCondition(getTagCondition(metal.getIngot()));
					for(ICondition condition : alloy.getConditions())
						alloyBuilder.addCondition(condition);
					for(IngredientWithSize ingr : ingredients)
						alloyBuilder.input(ingr);
					alloyBuilder.build(out, toRL("alloysmelter/"+metal.getName()));
				}

				arcBuilder = ArcFurnaceRecipeBuilder.builder()
						.output(metal.getIngot(), alloy.getOutputSize())
						.setTime(100)
						.setEnergy(51200)
						.input(ingredients[0]);
				if(!metal.isNative())
					arcBuilder.addCondition(getTagCondition(metal.getIngot()));
				for(ICondition condition : alloy.getConditions())
					arcBuilder.addCondition(condition);
				for(int i = 1; i < ingredients.length; i++)
					arcBuilder.additive(ingredients[i]);
				arcBuilder.build(out, toRL("arcfurnace/alloy_"+metal.getName()));
			}
		}

		// Non-metal ores
		for(RecipeOres ore : RecipeOres.values())
		{
			SecondaryOutput[] secondaryOutputs = ore.getSecondaryOutputs();
			CrusherRecipeBuilder oreCrushing = CrusherRecipeBuilder.builder()
					.input(ore.getOre())
					.output(ore.getOutput())
					.setEnergy(6000);
			if(!ore.isNative())
				oreCrushing.addCondition(getTagCondition(ore.getOre()));
			if(secondaryOutputs!=null)
				for(SecondaryOutput secondaryOutput : secondaryOutputs)
					oreCrushing.addSecondary(secondaryOutput.getItem(), secondaryOutput.getChance(), secondaryOutput.getConditions());
			oreCrushing.build(out, toRL("crusher/ore_"+ore.getName()));
		}
	}

	private void makeMaterialRecipes(RecipeOutput out)
	{
		for(EnumMetals metal : EnumMetals.values())
		{
			IETags.MetalTags tags = IETags.getTagsFor(metal);

			ItemLike rawOre = Metals.RAW_ORES.get(metal);
			ItemLike nugget = Metals.NUGGETS.get(metal);
			ItemLike ingot = Metals.INGOTS.get(metal);
			ItemLike plate = Metals.PLATES.get(metal);
			ItemLike dust = Metals.DUSTS.get(metal);
			BlockEntry<Block> block = IEBlocks.Metals.STORAGE.get(metal);
			BlockEntry<IEBaseBlock> sheetMetal = IEBlocks.Metals.SHEETMETAL.get(metal);
			if(metal.shouldAddNugget())
				add3x3Conversion(ingot, nugget, tags.nugget, out);
			if(!metal.isVanillaMetal())
				add3x3Conversion(block, ingot, tags.ingot, out);
			if(metal.shouldAddOre())
			{
				BlockEntry<Block> ore = IEBlocks.Metals.ORES.get(metal);
				addStandardSmeltingBlastingRecipe(ore, ingot, metal.smeltingXP, out);
				ore = IEBlocks.Metals.DEEPSLATE_ORES.get(metal);
				addStandardSmeltingBlastingRecipe(ore, ingot, metal.smeltingXP, out);
				addStandardSmeltingBlastingRecipe(Metals.RAW_ORES.get(metal), ingot, metal.smeltingXP, out);
				BlockEntry<Block> rawBlock = IEBlocks.Metals.RAW_ORES.get(metal);
				add3x3Conversion(rawBlock, rawOre, tags.rawOre, out);
			}
			addStandardSmeltingBlastingRecipe(dust, ingot, 0, out, "_from_dust");
//			addStandardSmeltingBlastingRecipe(dust, ingot, metal.smeltingXP, out, "_from_dust"); //TODO: remove this, if 0 XP on dust is intentional. this bugs out because the alloys do not have metal.smeltingXP
			shapelessMisc(plate)
					.requires(IETags.getTagsFor(metal).ingot)
					.requires(Tools.HAMMER)
					.unlockedBy("has_"+metal.tagName()+"_ingot", has(IETags.getTagsFor(metal).ingot))
					.save(out, toRL("plate_"+metal.tagName()+"_hammering"));
			shapedMisc(sheetMetal, 4)
					.pattern(" p ")
					.pattern("p p")
					.pattern(" p ")
					.define('p', IETags.getTagsFor(metal).plate)
					.unlockedBy("has_"+toPath(plate), has(plate))
					.save(out, toRL(toPath(sheetMetal)));
		}
	}
}
