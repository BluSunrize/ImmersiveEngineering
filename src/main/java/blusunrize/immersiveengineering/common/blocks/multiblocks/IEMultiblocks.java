/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.IETags.MetalTags;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher.Result;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.UnionMultiblock.TransformedMultiblock;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.Vec3i;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class IEMultiblocks
{
	public static final List<IMultiblock> IE_MULTIBLOCKS = new ArrayList<>();

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
	public static IETemplateMultiblock RADIO_TOWER;
	public static IETemplateMultiblock CHUNK_LOADER;

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
		ImmutableList.Builder<TagKey<Block>> genericTagsBuilder = ImmutableList.builder();
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
		List<TagKey<Block>> genericTags = genericTagsBuilder.build();
		BlockMatcher.addPredicate((expected, found, world, pos) -> {
			if(expected.getBlock()!=found.getBlock())
				for(TagKey<Block> t : genericTags)
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
		//Ignore sculk sensor properties
		BlockMatcher.addPreprocessor((expected, found, world, pos) -> {
			if(expected.getBlock()==Blocks.CALIBRATED_SCULK_SENSOR&&found.getBlock()==Blocks.CALIBRATED_SCULK_SENSOR)
				return expected;
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
		CRUSHER = register(new CrusherMultiblock());
		SAWMILL = register(new SawmillMultiblock());
		ALLOY_SMELTER = register(new AlloySmelterMultiblock());
		ARC_FURNACE = register(new ArcFurnaceMultiblock());
		ASSEMBLER = register(new AssemblerMultiblock());
		AUTO_WORKBENCH = register(new AutoWorkbenchMultiblock());
		BLAST_FURNACE = register(new BlastFurnaceMultiblock());
		ADVANCED_BLAST_FURNACE = register(new ImprovedBlastfurnaceMultiblock());
		BOTTLING_MACHINE = register(new BottlingMachineMultiblock());
		BUCKET_WHEEL = register(new BucketWheelMultiblock());
		COKE_OVEN = register(new CokeOvenMultiblock());
		DIESEL_GENERATOR = register(new DieselGeneratorMultiblock());
		EXCAVATOR = register(new ExcavatorMultiblock());
		FEEDTHROUGH = register(new FeedthroughMultiblock());
		FERMENTER = register(new FermenterMultiblock());
		LIGHTNING_ROD = register(new LightningRodMultiblock());
		METAL_PRESS = register(new MetalPressMultiblock());
		MIXER = register(new MixerMultiblock());
		REFINERY = register(new RefineryMultiblock());
		SHEETMETAL_TANK = register(new SheetmetalTankMultiblock());
		SILO = register(new SiloMultiblock());
		SQUEEZER = register(new SqueezerMultiblock());
		EXCAVATOR_DEMO = register(new UnionMultiblock(IEApi.ieLoc("excavator_demo"),
				ImmutableList.of(
						new TransformedMultiblock(EXCAVATOR, Vec3i.ZERO, Rotation.NONE),
						new TransformedMultiblock(BUCKET_WHEEL, new Vec3i(1, -2, 4), Rotation.COUNTERCLOCKWISE_90)
				)));
		RADIO_TOWER = register(new RadioTowerMultiblock());
		CHUNK_LOADER = register(new ChunkLoaderMultiblock());
	}

	private static <T extends IMultiblock>
	T register(T multiblock)
	{
		IE_MULTIBLOCKS.add(multiblock);
		MultiblockHandler.registerMultiblock(multiblock);
		return multiblock;
	}
}
