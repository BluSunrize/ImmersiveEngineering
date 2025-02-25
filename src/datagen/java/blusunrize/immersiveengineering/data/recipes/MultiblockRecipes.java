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
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.crafting.PotionHelper;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.BlockEntry;
import blusunrize.immersiveengineering.common.register.IEBlocks.StoneDecoration;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDecoration;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Molds;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.data.recipes.builder.*;
import blusunrize.immersiveengineering.data.resources.RecipeWoods;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static blusunrize.immersiveengineering.api.IETags.getItemTag;
import static blusunrize.immersiveengineering.api.IETags.getStorageBlock;
import static blusunrize.immersiveengineering.api.utils.TagUtils.createItemWrapper;

public class MultiblockRecipes extends IERecipeProvider
{
	// partial bucket values for bottling & mixing
	private static final int half_bucket = FluidType.BUCKET_VOLUME/2;
	private static final int quarter_bucket = FluidType.BUCKET_VOLUME/4;
	private static final int eighth_bucket = FluidType.BUCKET_VOLUME/8;

	public MultiblockRecipes(PackOutput p_248933_, CompletableFuture<Provider> provider)
	{
		super(p_248933_, provider);
	}

	@Override
	protected void buildRecipes(RecipeOutput out)
	{
		alloySmelter(out);
		arcFurnace(out);
		blastfurnace(out);
		bottling(out);
		cokeoven(out);
		crusher(out);
		fermenter(out);
		metalPress(out);
		mixer(out);
		sawmill(out);
		squeezer(out);
		refinery(out);
	}

	private void alloySmelter(RecipeOutput out)
	{
		AlloyRecipeBuilder.builder()
				.output(new ItemStack(StoneDecoration.INSULATING_GLASS.asItem(), 2))
				.input(new IngredientWithSize(Tags.Items.GLASS_BLOCKS, 2))
				.input(IETags.getTagsFor(EnumMetals.IRON).dust)
				.build(out, toRL("alloysmelter/"+toPath(StoneDecoration.INSULATING_GLASS)));
	}

	private void metalPress(RecipeOutput out)
	{
		MetalPressRecipeBuilder.builder()
				.input(IETags.getTagsFor(EnumMetals.COPPER).ingot)
				.mold(Molds.MOLD_BULLET_CASING)
				.output(new ItemStack(Ingredients.EMPTY_CASING, 2))
				.setEnergy(2400)
				.build(out, toRL("metalpress/bullet_casing"));

		ItemStack electrode = new ItemStack(Misc.GRAPHITE_ELECTRODE);
		electrode.setDamageValue(IEServerConfig.MACHINES.arcfurnace_electrodeDamage.getDefault()/2);
		MetalPressRecipeBuilder.builder()
				.input(IETags.hopGraphiteIngot, 4)
				.mold(Molds.MOLD_ROD)
				.output(electrode)
				.setEnergy(4800)
				.build(out, toRL("metalpress/electrode"));

		MetalPressRecipeBuilder.builder()
				.input(Tags.Items.INGOTS_NETHERITE)
				.mold(Molds.MOLD_ROD)
				.output(Ingredients.STICK_NETHERITE, 2)
				.setEnergy(2400)
				.build(out, toRL("metalpress/rod_netherite"));

		MetalPressRecipeBuilder.builder()
				.input(Items.MELON)
				.mold(Molds.MOLD_UNPACKING)
				.output(new ItemStack(Items.MELON_SLICE, 9))
				.setEnergy(3200)
				.build(out, toRL("metalpress/melon"));

		MetalPressRecipeBuilder.builder()
				.input(Items.BLAZE_POWDER, 5)
				.mold(Molds.MOLD_ROD)
				.output(Items.BLAZE_ROD)
				.setEnergy(3200)
				.build(out, toRL("metalpress/blaze_rod"));
	}

	private void arcFurnace(RecipeOutput out)
	{
		ArcFurnaceRecipeBuilder.builder()
				.output(IETags.getTagsFor(EnumMetals.STEEL).ingot, 1)
				.input(Tags.Items.INGOTS_IRON)
				.additive(IETags.coalCokeDust)
				.slag(IETags.slag, 1)
				.setTime(400)
				.setEnergy(204800)
				.build(out, toRL("arcfurnace/steel"));

		ArcFurnaceRecipeBuilder.builder()
				.output(Items.NETHERITE_SCRAP, 2)
				.input(Items.ANCIENT_DEBRIS)
				.slag(IETags.slag, 1)
				.setTime(100)
				.setEnergy(512000)
				.build(out, toRL("arcfurnace/netherite_scrap"));

		ArcFurnaceRecipeBuilder.builder()
				.output(StoneDecoration.INSULATING_GLASS.asItem(), 2)
				.input(new IngredientWithSize(Tags.Items.GLASS_BLOCKS, 2))
				.additive(IETags.getTagsFor(EnumMetals.IRON).dust)
				.setTime(100)
				.setEnergy(51200)
				.build(out, toRL("arcfurnace/"+toPath(StoneDecoration.INSULATING_GLASS)));
	}

	private void bottling(RecipeOutput out)
	{
		BottlingMachineRecipeBuilder.builder()
				.output(Items.WET_SPONGE)
				.input(Items.SPONGE)
				.fluidInput(FluidTags.WATER, FluidType.BUCKET_VOLUME)
				.build(out, toRL("bottling/sponge"));
		BottlingMachineRecipeBuilder.builder()
				.output(Items.MUD)
				.input(Items.DIRT)
				.fluidInput(FluidTags.WATER, quarter_bucket)
				.build(out, toRL("bottling/mud"));
		BottlingMachineRecipeBuilder.builder()
				.output(Items.EXPOSED_COPPER)
				.input(Items.COPPER_BLOCK)
				.fluidInput(IETags.fluidRedstoneAcid, eighth_bucket)
				.build(out, toRL("bottling/copper_aging"));
		BottlingMachineRecipeBuilder.builder()
				.output(Items.WEATHERED_COPPER)
				.input(Items.EXPOSED_COPPER)
				.fluidInput(IETags.fluidRedstoneAcid, eighth_bucket)
				.build(out, toRL("bottling/copper_aging"));
		BottlingMachineRecipeBuilder.builder()
				.output(Items.OXIDIZED_COPPER)
				.input(Items.WEATHERED_COPPER)
				.fluidInput(IETags.fluidRedstoneAcid, eighth_bucket)
				.build(out, toRL("bottling/copper_aging"));
		BottlingMachineRecipeBuilder.builder()
				.output(Ingredients.ERSATZ_LEATHER.get())
				.output(Molds.MOLD_PLATE)
				.input(Molds.MOLD_PLATE)
				.input(IETags.fabricHemp)
				.fluidInput(IETags.fluidPlantoil, eighth_bucket)
				.build(out, toRL("bottling/"+toPath(Ingredients.ERSATZ_LEATHER)));
		BottlingMachineRecipeBuilder.builder()
				.output(Ingredients.DUROPLAST_PLATE.get())
				.output(Molds.MOLD_PLATE)
				.input(Molds.MOLD_PLATE)
				.fluidInput(IETags.fluidResin, quarter_bucket)
				.build(out, toRL("bottling/duroplast_plate"));
		BottlingMachineRecipeBuilder.builder()
				.output(StoneDecoration.DUROPLAST, 4)
				.output(Molds.MOLD_PACKING_4)
				.input(Molds.MOLD_PACKING_4)
				.fluidInput(IETags.fluidResin, FluidType.BUCKET_VOLUME*4)
				.build(out, toRL("bottling/duroplast_block"));
		BottlingMachineRecipeBuilder.builder()
				.output(Ingredients.EMPTY_SHELL, 2)
				.output(Molds.MOLD_BULLET_CASING)
				.input(Molds.MOLD_BULLET_CASING)
				.input(IETags.getTagsFor(EnumMetals.COPPER).nugget, 3)
				.fluidInput(IETags.fluidResin, quarter_bucket)
				.build(out, toRL("bottling/"+toPath(BulletHandler.emptyShell)));
		BottlingMachineRecipeBuilder.builder()
				.output(Tools.GRINDINGDISK.get())
				.output(Molds.MOLD_GEAR)
				.input(Molds.MOLD_GEAR)
				.input(IETags.getTagsFor(EnumMetals.ALUMINUM).dust, 6)
				.input(IETags.fiberHemp, 8)
				.fluidInput(IETags.fluidResin, half_bucket)
				.build(out, toRL("bottling/"+toPath(Tools.GRINDINGDISK)));


		// I can't believe this map didn't exist already...
		Map<Block, Block> vanillaConcrete = new HashMap<>();
		vanillaConcrete.put(Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE);
		vanillaConcrete.put(Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE);
		vanillaConcrete.put(Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE);
		vanillaConcrete.put(Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE);
		vanillaConcrete.put(Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE);
		vanillaConcrete.put(Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE);
		vanillaConcrete.put(Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE);
		vanillaConcrete.put(Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE);
		vanillaConcrete.put(Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE);
		vanillaConcrete.put(Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE);
		vanillaConcrete.put(Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE);
		vanillaConcrete.put(Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE);
		vanillaConcrete.put(Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE);
		vanillaConcrete.put(Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE);
		vanillaConcrete.put(Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE);
		vanillaConcrete.put(Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE);
		vanillaConcrete.forEach((powder, concrete) -> BottlingMachineRecipeBuilder.builder()
				.output(concrete)
				.input(powder)
				.fluidInput(FluidTags.WATER, eighth_bucket)
				.build(out, toRL("bottling/"+toPath(powder)))
		);
	}

	private void crusher(RecipeOutput out)
	{
		CrusherRecipeBuilder.builder()
				.output(Items.COBBLED_DEEPSLATE)
				.input(Items.DEEPSLATE)
				.setEnergy(2000)
				.build(out, toRL("crusher/deepslate"));
		CrusherRecipeBuilder.builder()
				.output(Items.COBBLESTONE)
				.input(Items.STONE)
				.setEnergy(1600)
				.build(out, toRL("crusher/stone"));
		CrusherRecipeBuilder.builder()
				.output(Items.GRAVEL)
				.input(Tags.Items.COBBLESTONES)
				.setEnergy(1600)
				.build(out, toRL("crusher/cobblestone"));
		CrusherRecipeBuilder.builder()
				.output(Items.SAND)
				.addSecondary(Items.FLINT, .1f)
				.input(Tags.Items.GRAVELS)
				.setEnergy(1600)
				.build(out, toRL("crusher/gravel"));
		CrusherRecipeBuilder.builder()
				.output(StoneDecoration.SLAG_GRAVEL.asItem())
				.input(IETags.slag)
				.setEnergy(1600)
				.build(out, toRL("crusher/slag"));
		CrusherRecipeBuilder.builder()
				.output(StoneDecoration.SLAG_GRAVEL.asItem())
				.input(StoneDecoration.SLAG_BRICK)
				.setEnergy(1600)
				.build(out, toRL("crusher/slag_brick"));
		CrusherRecipeBuilder.builder()
				.output(Items.SAND)
				.input(Tags.Items.GLASS_BLOCKS)
				.setEnergy(3200)
				.build(out, toRL("crusher/glass"));
		CrusherRecipeBuilder.builder()
				.output(Items.SAND, 2)
				.addSecondary(IETags.saltpeterDust, .5f)
				.input(Tags.Items.SANDSTONE_UNCOLORED_BLOCKS)
				.setEnergy(3200)
				.build(out, toRL("crusher/sandstone"));
		CrusherRecipeBuilder.builder()
				.output(Items.RED_SAND, 2)
				.addSecondary(IETags.saltpeterDust, .5f)
				.input(Tags.Items.SANDSTONE_RED_BLOCKS)
				.setEnergy(3200)
				.build(out, toRL("crusher/red_sandstone"));
		CrusherRecipeBuilder.builder()
				.output(Items.CLAY_BALL, 4)
				.input(IETags.getItemTag(IETags.clayBlock))
				.setEnergy(1600)
				.build(out, toRL("crusher/clay"));
		CrusherRecipeBuilder.builder()
				.output(Items.DIRT)
				.input(Items.COARSE_DIRT)
				.setEnergy(1600)
				.build(out, toRL("crusher/coarse_dirt"));
		CrusherRecipeBuilder.builder()
				.output(Items.AMETHYST_SHARD, 4)
				.input(Items.AMETHYST_BLOCK)
				.setEnergy(3200)
				.build(out, toRL("crusher/amethyst"));
		CrusherRecipeBuilder.builder()
				.output(Items.QUARTZ, 4)
				.input(Items.QUARTZ_BLOCK)
				.setEnergy(3200)
				.build(out, toRL("crusher/quartz"));
		CrusherRecipeBuilder.builder()
				.output(Items.GLOWSTONE_DUST, 4)
				.input(Blocks.GLOWSTONE)
				.setEnergy(3200)
				.build(out, toRL("crusher/glowstone"));
		CrusherRecipeBuilder.builder()
				.output(Items.PRISMARINE_SHARD, 4)
				.input(Blocks.PRISMARINE)
				.setEnergy(3200)
				.build(out, toRL("crusher/prismarine"));
		CrusherRecipeBuilder.builder()
				.output(Items.PRISMARINE_SHARD, 8)
				.input(Blocks.DARK_PRISMARINE)
				.setEnergy(3200)
				.build(out, toRL("crusher/dark_prismarine"));
		CrusherRecipeBuilder.builder()
				.output(Items.PRISMARINE_SHARD, 9)
				.input(Blocks.PRISMARINE_BRICKS)
				.setEnergy(3200)
				.build(out, toRL("crusher/prismarine_brick"));
		CrusherRecipeBuilder.builder()
				.output(Items.BLAZE_POWDER, 4)
				.addSecondary(IETags.sulfurDust, .5f)
				.input(Tags.Items.RODS_BLAZE)
				.setEnergy(1600)
				.build(out, toRL("crusher/blaze_powder"));
		CrusherRecipeBuilder.builder()
				.output(Items.BONE_MEAL, 6)
				.input(Items.BONE)
				.setEnergy(1600)
				.build(out, toRL("crusher/bone_meal"));
		CrusherRecipeBuilder.builder()
				.output(IETags.coalCokeDust, 1)
				.input(IETags.coalCoke)
				.setEnergy(2400)
				.build(out, toRL("crusher/coke"));
		CrusherRecipeBuilder.builder()
				.output(IETags.coalCokeDust, 9)
				.input(IETags.getItemTag(IETags.coalCokeBlock))
				.setEnergy(4800)
				.build(out, toRL("crusher/coke_block"));

		TagKey<Item> coal_dust = createItemWrapper(IETags.getDust("coal"));
		CrusherRecipeBuilder.builder()
				.output(coal_dust, 1)
				.addCondition(getTagCondition(coal_dust))
				.input(Items.COAL)
				.setEnergy(2400)
				.build(out, toRL("crusher/coal"));
		CrusherRecipeBuilder.builder()
				.output(coal_dust, 9)
				.addCondition(getTagCondition(coal_dust))
				.input(Items.COAL_BLOCK)
				.setEnergy(4800)
				.build(out, toRL("crusher/coal_block"));

		CrusherRecipeBuilder.builder()
				.output(Items.STRING, 4)
				.input(ItemTags.WOOL)
				.setEnergy(3200)
				.build(out, toRL("crusher/wool"));

		CrusherRecipeBuilder.builder()
				.output(IETags.getTagsFor(EnumMetals.GOLD).dust, 2)
				.input(Items.NETHER_GOLD_ORE)
				.setEnergy(4200)
				.build(out, toRL("crusher/nether_gold"));

		CrusherRecipeBuilder.builder()
				.output(Items.NETHER_WART, 9)
				.input(Items.NETHER_WART_BLOCK)
				.setEnergy(1600)
				.build(out, toRL("crusher/nether_wart"));

		CrusherRecipeBuilder.builder()
				.output(new ItemStack(Items.BLACK_DYE, 1))
				.addSecondary(Items.GRAY_DYE, .2f)
				.input(Items.CHARCOAL)
				.setEnergy(1600)
				.build(out, toRL("crusher/black_dye"));
		CrusherRecipeBuilder.builder()
				.output(new ItemStack(Items.BLUE_DYE, 2))
				.addSecondary(Items.LIGHT_GRAY_DYE, .1f)
				.input(Tags.Items.GEMS_LAPIS)
				.setEnergy(1600)
				.build(out, toRL("crusher/blue_dye"));

		CrusherRecipeBuilder.builder()
				.output(new ItemStack(Items.WHITE_DYE, 2))
				.addSecondary(Items.LIGHT_GRAY_DYE, .1f)
				.input(Items.BONE_MEAL)
				.setEnergy(1600)
				.build(out, toRL("crusher/white_dye"));
	}

	private void sawmill(RecipeOutput out)
	{
		for(RecipeWoods wood : RecipeWoods.values())
		{
			// Basic log
			if(wood.getLog()!=null)
			{
				SawmillRecipeBuilder sawmillBuilder = SawmillRecipeBuilder.builder()
						.output(wood.getPlank(), wood.plankCount())
						.input(wood.getLog())
						.setEnergy(1600);
				if(wood.getStripped()!=null)
				{
					sawmillBuilder.addStripped(wood.getStripped());
					if(wood.produceSawdust())
						sawmillBuilder.addStripSecondary(IETags.sawdust);
				}
				if(wood.produceSawdust())
					sawmillBuilder.addSawSecondary(IETags.sawdust);
				sawmillBuilder.build(out, toRL("sawmill/"+wood.getName()+"_log"));
			}
			// All-bark block
			if(wood.getWood()!=null)
			{
				var sawmillBuilder = SawmillRecipeBuilder.builder()
						.output(wood.getPlank(), wood.plankCount())
						.input(wood.getWood())
						.setEnergy(1600);
				if(wood.getStrippedWood()!=null)
				{
					sawmillBuilder.addStripped(wood.getStrippedWood());
					if(wood.produceSawdust())
						sawmillBuilder.addStripSecondary(IETags.sawdust);
				}
				if(wood.produceSawdust())
					sawmillBuilder.addSawSecondary(IETags.sawdust);
				sawmillBuilder.build(out, toRL("sawmill/"+wood.getName()+"_wood"));
			}
			// Already stripped log
			if(wood.getStripped()!=null)
			{
				var sawmillBuilder = SawmillRecipeBuilder.builder()
						.output(wood.getPlank(), wood.plankCount())
						.setEnergy(800);
				if(wood.getWood()!=null)
					sawmillBuilder.input(Ingredient.of(wood.getStripped(), wood.getStrippedWood()));
				else
					sawmillBuilder.input(wood.getStripped());
				if(wood.produceSawdust())
					sawmillBuilder.addSawSecondary(IETags.sawdust);
				sawmillBuilder.build(out, toRL("sawmill/stripped_"+wood.getName()+"_log"));
			}
			// Door
			if(wood.getDoor()!=null)
			{
				var sawmillBuilder = SawmillRecipeBuilder.builder()
						.output(wood.getPlank(), 1)
						.input(wood.getDoor())
						.setEnergy(800);
				if(wood.produceSawdust())
					sawmillBuilder.addSawSecondary(IETags.sawdust);
				sawmillBuilder.build(out, toRL("sawmill/"+wood.getName()+"_door"));
			}
			// Stairs
			if(wood.getStairs()!=null)
			{
				var sawmillBuilder = SawmillRecipeBuilder.builder()
						.output(wood.getPlank(), 1)
						.input(wood.getStairs())
						.setEnergy(1600);
				if(wood.produceSawdust())
					sawmillBuilder.addStripSecondary(IETags.sawdust);
				sawmillBuilder.build(out, toRL("sawmill/"+wood.getName()+"_stairs"));
			}
			// Slabs
			if(wood.getSlab()!=null)
			{
				var sawmillBuilder = SawmillRecipeBuilder.builder()
						.output(wood.getSlab(), 2)
						.input(wood.getPlank())
						.setEnergy(800);
				if(wood.produceSawdust())
					sawmillBuilder.addStripSecondary(IETags.sawdust);
				sawmillBuilder.build(out, toRL("sawmill/"+wood.getName()+"_slab"));
			}
		}
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
		{
			BlockEntry<IEBaseBlock> plank = WoodenDecoration.TREATED_WOOD.get(style);
			SawmillRecipeBuilder.builder()
					.output(IEBlocks.TO_SLAB.get(plank.getId()), 2)
					.input(plank.get())
					.setEnergy(800)
					.addStripSecondary(IETags.sawdust)
					.build(out, toRL("sawmill/treated_wood_"+style.name().toLowerCase(Locale.ROOT)+"_slab"));

			SawmillRecipeBuilder.builder()
					.output(plank.get(), 1)
					.input(IEBlocks.TO_STAIRS.get(plank.getId()))
					.setEnergy(1600)
					.addStripSecondary(IETags.sawdust)
					.build(out, toRL("sawmill/treated_wood_"+style.name().toLowerCase(Locale.ROOT)+"_stairs"));
		}
		SawmillRecipeBuilder.builder()
				.output(Items.OAK_PLANKS, 4)
				.input(Items.BOOKSHELF)
				.addStripSecondary(IETags.sawdust)
				.addStripSecondary(Items.BOOK, 3)
				.setEnergy(1600)
				.build(out, toRL("sawmill/bookshelf"));
	}

	private void squeezer(RecipeOutput out)
	{
		Fluid plantOil = IEFluids.PLANTOIL.getStill();
		SqueezerRecipeBuilder.builder()
				.output(plantOil, 80)
				.input(Items.WHEAT_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/wheat_seeds"));
		SqueezerRecipeBuilder.builder()
				.output(plantOil, 60)
				.input(Items.BEETROOT_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/beetroot_seeds"));
		SqueezerRecipeBuilder.builder()
				.output(plantOil, 40)
				.input(Items.PUMPKIN_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/pumpkin_seeds"));
		SqueezerRecipeBuilder.builder()
				.output(plantOil, 20)
				.input(Items.MELON_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/melon_seeds"));
		SqueezerRecipeBuilder.builder()
				.output(plantOil, 120)
				.input(Misc.HEMP_SEEDS)
				.setEnergy(6400)
				.build(out, toRL("squeezer/hemp_seeds"));
		SqueezerRecipeBuilder.builder()
				.output(new IngredientWithSize(IETags.hopGraphiteDust))
				.input(new IngredientWithSize(IETags.coalCokeDust, 8))
				.setEnergy(19200)
				.build(out, toRL("squeezer/graphite_dust"));
	}

	private void fermenter(RecipeOutput out)
	{
		Fluid ethanol = IEFluids.ETHANOL.getStill();
		FermenterRecipeBuilder.builder()
				.output(ethanol, 80)
				.input(Items.SUGAR_CANE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/sugar_cane"));
		FermenterRecipeBuilder.builder()
				.output(ethanol, 20)
				.input(Items.MELON_SLICE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/melon_slice"));
		FermenterRecipeBuilder.builder()
				.output(ethanol, 80)
				.input(Items.APPLE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/apple"));
		FermenterRecipeBuilder.builder()
				.output(ethanol, 80)
				.input(Tags.Items.CROPS_POTATO)
				.setEnergy(6400)
				.build(out, toRL("fermenter/potato"));
		FermenterRecipeBuilder.builder()
				.output(ethanol, 40)
				.input(Tags.Items.CROPS_BEETROOT)
				.setEnergy(6400)
				.build(out, toRL("fermenter/beetroot"));
		FermenterRecipeBuilder.builder()
				.output(ethanol, 50)
				.input(Items.SWEET_BERRIES)
				.setEnergy(6400)
				.build(out, toRL("fermenter/sweet_berries"));
		FermenterRecipeBuilder.builder()
				.output(ethanol, 100)
				.input(Items.GLOW_BERRIES)
				.setEnergy(6400)
				.build(out, toRL("fermenter/glow_berries"));
		FermenterRecipeBuilder.builder()
				.output(ethanol, 250)
				.output(Items.GLASS_BOTTLE)
				.input(Items.HONEY_BOTTLE)
				.setEnergy(6400)
				.build(out, toRL("fermenter/honey"));
	}

	private void refinery(RecipeOutput out)
	{
		RefineryRecipeBuilder.builder()
				.output(IEFluids.BIODIESEL.getStill(), 16)
				.catalyst(IETags.saltpeterDust)
				.input(IETags.fluidPlantoil, 8)
				.input(IETags.fluidEthanol, 8)
				.setEnergy(80)
				.build(out, toRL("refinery/biodiesel"));
		RefineryRecipeBuilder.builder()
				.output(IEFluids.HIGH_POWER_BIODIESEL.getStill(), 100)
				.input(IETags.fluidBiodiesel, 95)
				.input(PotionHelper.getFluidTagForType(Potions.STRENGTH, 5))
				.setEnergy(80)
				.build(out, toRL("refinery/high_power_biodiesel"));
		RefineryRecipeBuilder.builder()
				.output(IEFluids.ACETALDEHYDE.getStill(), 8)
				.catalyst(IETags.getTagsFor(EnumMetals.SILVER).plate)
				.input(IETags.fluidEthanol, 8)
				.setEnergy(120)
				.build(out, toRL("refinery/acetaldehyde"));
		RefineryRecipeBuilder.builder()
				.output(IEFluids.PHENOLIC_RESIN.getStill(), 8)
				.input(IETags.fluidAcetaldehyde, 12)
				.input(IETags.fluidCreosote, 8)
				.setEnergy(240)
				.build(out, toRL("refinery/resin"));
	}

	private void mixer(RecipeOutput out)
	{
		Fluid concrete = IEFluids.CONCRETE.getStill();
		MixerRecipeBuilder.builder()
				.output(concrete, half_bucket)
				.fluidInput(FluidTags.WATER, half_bucket)
				.input(new IngredientWithSize(Tags.Items.SANDS, 2))
				.input(Tags.Items.GRAVELS)
				.input(IETags.clay)
				.setEnergy(3200)
				.build(out, toRL("mixer/concrete"));
		MixerRecipeBuilder.builder()
				.output(IEFluids.HERBICIDE.getStill(), half_bucket)
				.fluidInput(IETags.fluidEthanol, half_bucket)
				.input(IETags.sulfurDust)
				.input(IETags.getTagsFor(EnumMetals.COPPER).dust)
				.setEnergy(3200)
				.build(out, toRL("mixer/herbicide"));
		MixerRecipeBuilder.builder()
				.output(IEFluids.REDSTONE_ACID.getStill(), quarter_bucket)
				.fluidInput(FluidTags.WATER, quarter_bucket)
				.input(Tags.Items.DUSTS_REDSTONE)
				.setEnergy(1600)
				.build(out, toRL("mixer/redstone_acid"));
	}

	private void blastfurnace(RecipeOutput out)
	{
		out.accept(
				toRL("blastfurnace/fuel_coke"), new BlastFurnaceFuel(Ingredient.of(IETags.coalCoke), 1200), null
		);
		out.accept(
				toRL("blastfurnace/fuel_coke_block"), new BlastFurnaceFuel(Ingredient.of(getItemTag(IETags.coalCokeBlock)), 10*1200), null
		);
		out.accept(
				toRL("blastfurnace/fuel_charcoal"), new BlastFurnaceFuel(Ingredient.of(IETags.charCoal), 300), null
		);
		TagKey<Item> charCoalBlocks = createItemWrapper(getStorageBlock("charcoal"));
		out.accept(
				toRL("blastfurnace/fuel_charcoal_block"),
				new BlastFurnaceFuel(Ingredient.of(charCoalBlocks), 10*300),
				null,
				getTagCondition(charCoalBlocks.location())
		);

		BlastFurnaceRecipeBuilder.builder()
				.output(IETags.getTagsFor(EnumMetals.STEEL).ingot, 1)
				.input(Tags.Items.INGOTS_IRON)
				.slag(IETags.slag, 1)
				.setTime(1200)
				.build(out, toRL("blastfurnace/steel"));

		BlastFurnaceRecipeBuilder.builder()
				.output(IETags.getItemTag(IETags.getTagsFor(EnumMetals.STEEL).storage), 1)
				.input(Tags.Items.STORAGE_BLOCKS_IRON)
				.slag(IETags.slag, 9)
				.setTime(9*1200)
				.build(out, toRL("blastfurnace/steel_block"));
	}

	private void cokeoven(RecipeOutput out)
	{
		CokeOvenRecipeBuilder.builder()
				.output(IETags.coalCoke, 1)
				.input(Items.COAL)
				.creosoteAmount(FluidType.BUCKET_VOLUME/2)
				.setTime(1800)
				.build(out, toRL("cokeoven/coke"));
		CokeOvenRecipeBuilder.builder()
				.output(IETags.getItemTag(IETags.coalCokeBlock), 1)
				.input(Blocks.COAL_BLOCK)
				.creosoteAmount(FluidType.BUCKET_VOLUME*5)
				.setTime(9*1800)
				.build(out, toRL("cokeoven/coke_block"));
		CokeOvenRecipeBuilder.builder()
				.output(Items.CHARCOAL)
				.input(ItemTags.LOGS_THAT_BURN)
				.creosoteAmount(FluidType.BUCKET_VOLUME/4)
				.setTime(900)
				.build(out, toRL("cokeoven/charcoal"));
	}
}
