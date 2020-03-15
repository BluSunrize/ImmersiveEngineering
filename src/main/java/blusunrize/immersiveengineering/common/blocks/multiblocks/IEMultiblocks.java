/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.multiblocks;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher;
import blusunrize.immersiveengineering.api.multiblocks.BlockMatcher.Result;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.common.blocks.multiblocks.UnionMultiblock.TransformedMultiblock;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.FourWayBlock;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.Vec3i;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.List;

public class IEMultiblocks
{
	public static IETemplateMultiblock CRUSHER;
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
		List<Property<Boolean>> sideProperties = ImmutableList.of(FourWayBlock.NORTH,
				FourWayBlock.EAST,
				FourWayBlock.SOUTH,
				FourWayBlock.WEST
		);
		BlockMatcher.addPredicate((expected, found, world, pos) -> {
			if(expected.getBlock() instanceof FourWayBlock&&expected.getBlock()==found.getBlock())
			{
				for(Property<Boolean> side : sideProperties)
					if(expected.get(side)&&!found.get(side))
						return Result.deny(2);
				return Result.allow(2);
			}
			return Result.DEFAULT;
		});

		//Init IE multiblocks
		CRUSHER = new CrusherMultiblock();
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
						new TransformedMultiblock(EXCAVATOR, Vec3i.NULL_VECTOR, Rotation.NONE),
						new TransformedMultiblock(BUCKET_WHEEL, new Vec3i(1, -2, 4), Rotation.COUNTERCLOCKWISE_90)
				));
	}
}
