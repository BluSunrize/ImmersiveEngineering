/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.IETags.MetalTags;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher.Result;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.UnionMultiblock.TransformedMultiblock;
import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.properties.Property;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class IEMultiblocks
{
	public static IETemplateMultiblock CRUSHER;
	public static IETemplateMultiblock SAWMILL;
	public static IETemplateMultiblock ALLOY_SMELTER;
	public static IETemplateMultiblock ARC_FURNACE;
	public static IETemplateMultiblock ASSEMBLER;
	public static IETemplateMultiblock AUTO_WORKBENCH;
	public static IETemplateMultiblock BLAST_FURNACE;
	public static IETemplateMultiblock ADVANCED_BLAST_FURNACE;
	public static IETemplateMultiblock BOTTLING_MACHINE;
	public static IETemplateMultiblock BUCKET_WHEEL;
	public static IETemplateMultiblock COKE_OVEN;
	public static IETemplateMultiblock DIESEL_GENERATOR;
	public static IETemplateMultiblock EXCAVATOR;
	public static IMultiblock EXCAVATOR_DEMO;
	public static IMultiblock FEEDTHROUGH;
	public static IETemplateMultiblock FERMENTER;
	public static IETemplateMultiblock LIGHTNING_ROD;
	public static IETemplateMultiblock METAL_PRESS;
	public static IETemplateMultiblock MIXER;
	public static IETemplateMultiblock REFINERY;
	public static IETemplateMultiblock SHEETMETAL_TANK;
	public static IETemplateMultiblock SILO;
	public static IETemplateMultiblock SQUEEZER;

	public static void init()
	{
		//Add general matcher predicates
		//Basic blockstate matcher
		BlockMatcher.addPredicate((expected, found, world, pos) -> expected==found?Result.allow(1): Result.deny(1));
		//FourWayBlock (fences etc): allow additional connections
		List<Property<Boolean>> sideProperties = ImmutableList.of(
				CrossCollisionBlock.NORTH, CrossCollisionBlock.EAST, CrossCollisionBlock.SOUTH, CrossCollisionBlock.WEST
		);
		BlockMatcher.addPreprocessor((expected, found, world, pos) -> {
			if(expected.getBlock() instanceof CrossCollisionBlock&&expected.getBlock()==found.getBlock())
				for(Property<Boolean> side : sideProperties)
					if(!expected.getValue(side))
						found = found.setValue(side, false);
			return found;
		});
		//Tags
		ImmutableList.Builder<Tag<Block>> genericTagsBuilder = ImmutableList.builder();
		for(EnumMetals metal : EnumMetals.values())
		{
			MetalTags tags = IETags.getTagsFor(metal);
			genericTagsBuilder.add(tags.storage)
					.add(tags.sheetmetal);
		}
		genericTagsBuilder.add(IETags.scaffoldingAlu);
		genericTagsBuilder.add(IETags.scaffoldingSteel);
		genericTagsBuilder.add(IETags.treatedWoodSlab);
		genericTagsBuilder.add(IETags.treatedWood);
		genericTagsBuilder.add(IETags.fencesSteel);
		genericTagsBuilder.add(IETags.fencesAlu);
		List<Tag<Block>> genericTags = genericTagsBuilder.build();
		BlockMatcher.addPredicate((expected, found, world, pos) -> {
			if(expected.getBlock()!=found.getBlock())
				for(Tag<Block> t : genericTags)
					if(expected.is(t)&&found.is(t))
						return Result.allow(2);
			return Result.DEFAULT;
		});
		//Ignore hopper facing
		BlockMatcher.addPreprocessor((expected, found, world, pos) -> {
			if(expected.getBlock()==Blocks.HOPPER&&found.getBlock()==Blocks.HOPPER)
				return found.setValue(HopperBlock.FACING, expected.getValue(HopperBlock.FACING));
			return found;
		});
		//Allow multiblocks to be formed under water
		BlockMatcher.addPreprocessor((expected, found, world, pos) -> {
			// Un-waterlog if the expected state is dry, but the found one is not
			if(expected.hasProperty(WATERLOGGED)&&found.hasProperty(WATERLOGGED)
					&&!expected.getValue(WATERLOGGED)&&found.getValue(WATERLOGGED))
				return found.setValue(WATERLOGGED, false);
			else
				return found;
		});

		//Init IE multiblocks
		CRUSHER = new CrusherMultiblock();
		SAWMILL = new SawmillMultiblock();
		ALLOY_SMELTER = new AlloySmelterMultiblock();
		ARC_FURNACE = new ArcFurnaceMultiblock();
		ASSEMBLER = new AssemblerMultiblock();
		AUTO_WORKBENCH = new AutoWorkbenchMultiblock();
		BLAST_FURNACE = new BlastFurnaceMultiblock();
		ADVANCED_BLAST_FURNACE = new ImprovedBlastfurnaceMultiblock();
		BOTTLING_MACHINE = new BottlingMachineMultiblock();
		BUCKET_WHEEL = new BucketWheelMultiblock();
		COKE_OVEN = new CokeOvenMultiblock();
		DIESEL_GENERATOR = new DieselGeneratorMultiblock();
		EXCAVATOR = new ExcavatorMultiblock();
		FEEDTHROUGH = new FeedthroughMultiblock();
		FERMENTER = new FermenterMultiblock();
		LIGHTNING_ROD = new LightningRodMultiblock();
		METAL_PRESS = new MetalPressMultiblock();
		MIXER = new MixerMultiblock();
		REFINERY = new RefineryMultiblock();
		SHEETMETAL_TANK = new SheetmetalTankMultiblock();
		SILO = new SiloMultiblock();
		SQUEEZER = new SqueezerMultiblock();
		EXCAVATOR_DEMO = new UnionMultiblock(new ResourceLocation(ImmersiveEngineering.MODID, "excavator_demo"),
				ImmutableList.of(
						new TransformedMultiblock(EXCAVATOR, Vec3i.ZERO, Rotation.NONE),
						new TransformedMultiblock(BUCKET_WHEEL, new Vec3i(1, -2, 4), Rotation.COUNTERCLOCKWISE_90)
				));
	}
}
