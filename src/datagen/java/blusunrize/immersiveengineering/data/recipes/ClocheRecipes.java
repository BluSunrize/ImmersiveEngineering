/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.client.utils.ClocheRenderFunctions.*;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.data.recipes.builder.ClocheFertilizerBuilder;
import blusunrize.immersiveengineering.data.recipes.builder.ClocheRecipeBuilder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;

import java.util.concurrent.CompletableFuture;

public class ClocheRecipes extends IERecipeProvider
{
	public ClocheRecipes(PackOutput p_248933_, CompletableFuture<Provider> provider)
	{
		super(p_248933_, provider);
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		fertilizers(out);

		simpleCrops(out);
		stemCrops(out);

		ClocheRecipeBuilder.builder()
				.output(Items.TORCHFLOWER)
				.seed(Items.TORCHFLOWER_SEEDS)
				.soil(Blocks.DIRT)
				.setTime(1200)
				.setRender(new RenderFunctionCrop(Blocks.TORCHFLOWER_CROP))
				.build(out, toRL("cloche/torchflower"));
		stackingCrops(out);

		mushrooms(out);
		flowers(out);
		plants(out);
	}

	private void fertilizers(RecipeOutput out)
	{
		//Minor nutrients are 10% boost (calcium, magnesium, sulfur), major nutrients are 20% (phosphorous, nitrogen, potassium)
		//Single-nutrient fertilizers:
		ClocheFertilizerBuilder.builder(1.10f)
				.input(IETags.sulfurDust)
				.build(out, toRL("fertilizer/sulfur"));
		//Dual-nutrient fertilizers:
		//Slag: Phosphorous, Calcium
		ClocheFertilizerBuilder.builder(1.30f)
				.input(IETags.slag)
				.build(out, toRL("fertilizer/slag"));
		//Nitrate: Nitrogen, no Potassium because it can be many things including sodium - and the recipe is closest to Chilean saltpeter (NaNO3)
		ClocheFertilizerBuilder.builder(1.20f)
				.input(IETags.saltpeterDust)
				.build(out, toRL("fertilizer/saltpeter"));
		//Bonemeal: Calcium, Phosphorous
		ClocheFertilizerBuilder.builder(1.30f)
				.input(Items.BONE_MEAL)
				.build(out, toRL("fertilizer/bonemeal"));
		//Quad-nutrient fertilizers:
		//Industrial Fertilizer: Nitrogen, Phosphorous, Sulfur, Calcium
		ClocheFertilizerBuilder.builder(1.60f)
				.input(Misc.FERTILIZER)
				.build(out, toRL("fertilizer/fertilizer"));
	}

	private void plants(RecipeOutput out)
	{
		ClocheRecipeBuilder.builder()
				.output(Blocks.SHORT_GRASS)
				.seed(Blocks.SHORT_GRASS)
				.soil(Blocks.COARSE_DIRT)
				.setTime(480)
				.setRender(new RenderFunctionGeneric(Blocks.SHORT_GRASS))
				.build(out, toRL("cloche/"+Blocks.SHORT_GRASS.builtInRegistryHolder().key().location().getPath()));
		ClocheRecipeBuilder.builder()
				.output(Blocks.FERN)
				.seed(Blocks.FERN)
				.soil(Blocks.COARSE_DIRT)
				.setTime(480)
				.setRender(new RenderFunctionGeneric(Blocks.FERN))
				.build(out, toRL("cloche/"+Blocks.FERN.builtInRegistryHolder().key().location().getPath()));
	}

	private void flowers(RecipeOutput out)
	{
		flower(out, Blocks.RED_TULIP);
		flower(out, Blocks.ORANGE_TULIP);
		flower(out, Blocks.WHITE_TULIP);
		flower(out, Blocks.PINK_TULIP);
		flower(out, Blocks.DANDELION);
		flower(out, Blocks.POPPY);
		flower(out, Blocks.OXEYE_DAISY);
		flower(out, Blocks.AZURE_BLUET);
		flower(out, Blocks.ALLIUM);
		flower(out, Blocks.LILY_OF_THE_VALLEY);
		flower(out, Blocks.CORNFLOWER);
		flower(out, Blocks.BLUE_ORCHID, Blocks.MUD);
		doubleFlower(out, Blocks.LILAC);
		doubleFlower(out, Blocks.SUNFLOWER);
		doubleFlower(out, Blocks.ROSE_BUSH);
		doubleFlower(out, Blocks.PEONY);
		flower(out, Blocks.WITHER_ROSE, Blocks.SOUL_SOIL);
	}

	private void flower(RecipeOutput out, Block flowerBlock, ItemLike soil)
	{
		ClocheRecipeBuilder.builder()
				.output(flowerBlock)
				.seed(flowerBlock)
				.soil(soil)
				.setTime(480)
				.setRender(new RenderFunctionGeneric(flowerBlock))
				.build(out, toRL("cloche/"+flowerBlock.builtInRegistryHolder().key().location().getPath()));
	}

	private void doubleFlower(RecipeOutput out, Block flowerBlock, ItemLike soil)
	{
		ClocheRecipeBuilder.builder()
				.output(flowerBlock)
				.seed(flowerBlock)
				.soil(soil)
				.setTime(480)
				.setRender(new RenderFunctionDoubleFlower(flowerBlock))
				.build(out, toRL("cloche/"+flowerBlock.builtInRegistryHolder().key().location().getPath()));
	}

	private void flower(RecipeOutput out, Block flowerBlock)
	{
		flower(out, flowerBlock, Blocks.DIRT);
	}

	private void doubleFlower(RecipeOutput out, Block flowerBlock)
	{
		doubleFlower(out, flowerBlock, Blocks.DIRT);
	}

	private void mushrooms(RecipeOutput out)
	{
		Ingredient shroomSoil = Ingredient.of(Blocks.MYCELIUM, Blocks.PODZOL);
		ClocheRecipeBuilder.builder()
				.output(Items.RED_MUSHROOM)
				.seed(Items.RED_MUSHROOM)
				.soil(shroomSoil)
				.setTime(480)
				.setRender(new RenderFunctionGeneric(Blocks.RED_MUSHROOM))
				.build(out, toRL("cloche/red_mushroom"));
		ClocheRecipeBuilder.builder()
				.output(Items.BROWN_MUSHROOM)
				.seed(Items.BROWN_MUSHROOM)
				.soil(shroomSoil)
				.setTime(480)
				.setRender(new RenderFunctionGeneric(Blocks.BROWN_MUSHROOM))
				.build(out, toRL("cloche/brown_mushroom"));
		ClocheRecipeBuilder.builder()
				.output(Items.WARPED_FUNGUS)
				.seed(Items.WARPED_FUNGUS)
				.soil(Blocks.WARPED_NYLIUM)
				.setTime(560)
				.setRender(new RenderFunctionGeneric(Blocks.WARPED_FUNGUS))
				.build(out, toRL("cloche/warped_fungus"));
		ClocheRecipeBuilder.builder()
				.output(Items.CRIMSON_FUNGUS)
				.seed(Items.CRIMSON_FUNGUS)
				.soil(Blocks.CRIMSON_NYLIUM)
				.setTime(560)
				.setRender(new RenderFunctionGeneric(Blocks.CRIMSON_FUNGUS))
				.build(out, toRL("cloche/crimson_fungus"));
		ClocheRecipeBuilder.builder()
				.output(Items.MOSS_BLOCK)
				.seed(Items.MOSS_BLOCK)
				.soil(Tags.Items.COBBLESTONES)
				.setTime(1200)
				.setRender(new RenderFunctionGeneric(Blocks.MOSS_CARPET))
				.build(out, toRL("cloche/moss"));
	}

	private void stackingCrops(RecipeOutput out)
	{
		ClocheRecipeBuilder.builder()
				.output(Items.SUGAR_CANE)
				.seed(Items.SUGAR_CANE)
				.soil(Tags.Items.SANDS)
				.setTime(560)
				.setRender(new RenderFunctionStacking(Blocks.SUGAR_CANE))
				.build(out, toRL("cloche/sugar_cane"));
		ClocheRecipeBuilder.builder()
				.output(Items.CACTUS)
				.seed(Items.CACTUS)
				.soil(Tags.Items.SANDS)
				.setTime(560)
				.setRender(new RenderFunctionStacking(Blocks.CACTUS))
				.build(out, toRL("cloche/cactus"));
		ClocheRecipeBuilder.builder()
				.output(Items.BAMBOO)
				.seed(Items.BAMBOO)
				.soil(Blocks.DIRT)
				.setTime(560)
				.setRender(new RenderFunctionStacking(Blocks.BAMBOO))
				.build(out, toRL("cloche/bamboo"));
		ClocheRecipeBuilder.builder()
				.output(Items.CHORUS_FRUIT)
				.seed(Items.CHORUS_FLOWER)
				.soil(Blocks.END_STONE)
				.setTime(480)
				.setRender(new RenderFunctionChorus())
				.build(out, toRL("cloche/chorus_fruit"));
		ClocheRecipeBuilder.builder()
				.output(Ingredients.HEMP_FIBER)
				.output(Misc.HEMP_SEEDS, 0.5f)
				.seed(Misc.HEMP_SEEDS)
				.soil(Blocks.DIRT)
				.setTime(800)
				.setRender(new RenderFunctionDoubleCrop(IEBlocks.Misc.HEMP_PLANT.get(), 3))
				.build(out, toRL("cloche/hemp"));
		ClocheRecipeBuilder.builder()
				.output(Items.PITCHER_PLANT)
				.seed(Items.PITCHER_POD)
				.soil(Blocks.DIRT)
				.setTime(1200)
				.setRender(new RenderFunctionDoubleCrop(Blocks.PITCHER_CROP, 3))
				.build(out, toRL("cloche/pitcher_plant"));
	}

	private void stemCrops(RecipeOutput out)
	{
		ClocheRecipeBuilder.builder()
				.output(Items.PUMPKIN)
				.seed(Items.PUMPKIN_SEEDS)
				.soil(Blocks.DIRT)
				.setTime(800)
				.setRender(new RenderFunctionStem(Blocks.PUMPKIN, Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM))
				.build(out, toRL("cloche/pumpkin"));
		ClocheRecipeBuilder.builder()
				.output(Items.MELON)
				.seed(Items.MELON_SEEDS)
				.soil(Blocks.DIRT)
				.setTime(800)
				.setRender(new RenderFunctionStem(Blocks.MELON, Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM))
				.build(out, toRL("cloche/melon"));
	}

	private void simpleCrops(RecipeOutput out)
	{
		ClocheRecipeBuilder.builder()
				.output(Items.WHEAT, 2)
				.output(Items.WHEAT_SEEDS, 0.25f)
				.seed(Items.WHEAT_SEEDS)
				.soil(Blocks.DIRT)
				.setTime(640)
				.setRender(new RenderFunctionCrop(Blocks.WHEAT))
				.build(out, toRL("cloche/wheat"));
		ClocheRecipeBuilder.builder()
				.output(new ItemStack(Items.POTATO, 2))
				.seed(Items.POTATO)
				.soil(Blocks.DIRT)
				.setTime(800)
				.setRender(new RenderFunctionCrop(Blocks.POTATOES))
				.build(out, toRL("cloche/potato"));
		ClocheRecipeBuilder.builder()
				.output(new ItemStack(Items.CARROT, 2))
				.seed(Items.CARROT)
				.soil(Blocks.DIRT)
				.setTime(800)
				.setRender(new RenderFunctionCrop(Blocks.CARROTS))
				.build(out, toRL("cloche/carrot"));
		ClocheRecipeBuilder.builder()
				.output(new ItemStack(Items.BEETROOT, 2))
				.output(Items.BEETROOT_SEEDS, 0.25f)
				.seed(Items.BEETROOT_SEEDS)
				.soil(Blocks.DIRT)
				.setTime(800)
				.setRender(new RenderFunctionCrop(Blocks.BEETROOTS))
				.build(out, toRL("cloche/beetroot"));
		ClocheRecipeBuilder.builder()
				.output(new ItemStack(Items.NETHER_WART, 2))
				.seed(Items.NETHER_WART)
				.soil(Blocks.SOUL_SAND)
				.setTime(800)
				.setRender(new RenderFunctionCrop(Blocks.NETHER_WART))
				.build(out, toRL("cloche/nether_wart"));
		ClocheRecipeBuilder.builder()
				.output(new ItemStack(Items.SWEET_BERRIES, 2))
				.seed(Items.SWEET_BERRIES)
				.soil(Blocks.DIRT)
				.setTime(560)
				.setRender(new RenderFunctionCrop(Blocks.SWEET_BERRY_BUSH))
				.build(out, toRL("cloche/sweet_berries"));
		ClocheRecipeBuilder.builder()
				.output(new ItemStack(Items.GLOW_BERRIES, 1))
				.seed(Items.GLOW_BERRIES)
				.soil(Blocks.MOSS_BLOCK)
				.setTime(640)
				.setRender(new RenderFunctionCrop(Blocks.CAVE_VINES))
				.build(out, toRL("cloche/glow_berries"));
	}
}
