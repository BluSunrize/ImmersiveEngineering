/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.blocks.generic.IEFenceBlock;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public final class IEBlocks
{
	private IEBlocks()
	{
	}

	public static Map<Block, SlabBlock> toSlab = new IdentityHashMap<>();

	public static final class StoneDecoration
	{
		public static Block cokebrick;
		public static Block blastbrick;
		public static Block blastbrickReinforced;
		public static Block coke;
		public static Block hempcrete;
		public static Block concrete;
		public static Block concreteTile;
		public static Block concreteLeaded;
		public static Block insulatingGlass;
		public static Block concreteSprayed;
		public static Block alloybrick;
		public static StairsBlock hempcreteStairs;
		public static StairsBlock[] concreteStairs = new StairsBlock[3];

		//TODO possibly merge into a single block with "arbitrary" height?
		public static Block concreteSheet;
		public static Block concreteQuarter;
		public static Block concreteThreeQuarter;

		public static Block coresample;
	}

	public static final class Multiblocks
	{
		public static Block cokeOven;
		public static Block blastFurnace;
		public static Block alloySmelter;
		public static Block blastFurnaceAdv;

		public static Block metalPress;
		public static Block crusher;
		public static Block tank;
		public static Block silo;
		public static Block assembler;
		public static Block autoWorkbench;
		public static Block bottlingMachine;
		public static Block squeezer;
		public static Block fermenter;
		public static Block refinery;
		public static Block dieselGenerator;
		public static Block excavator;
		public static Block bucketWheel;
		public static Block arcFurnace;
		public static Block lightningrod;
		public static Block mixer;
	}

	public static final class Metals
	{
		public static Map<EnumMetals, Block> ores = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, Block> storage = new EnumMap<>(EnumMetals.class);
		public static Map<EnumMetals, Block> sheetmetal = new EnumMap<>(EnumMetals.class);
	}

	public static final class WoodenDecoration
	{
		public static Map<TreatedWoodStyles, Block> treatedWood = new EnumMap<>(TreatedWoodStyles.class);
		public static Map<TreatedWoodStyles, StairsBlock> treatedStairs = new EnumMap<>(TreatedWoodStyles.class);
		public static IEFenceBlock treatedFence;
		public static Block treatedScaffolding;
		public static Block treatedPost;
	}

	public static final class WoodenDevices
	{
		public static Block craftingTable;
		public static Block workbench;
		public static Block gunpowderBarrel;
		public static Block woodenBarrel;
		public static Block turntable;
		public static Block crate;
		public static Block reinforcedCrate;
		public static Block sorter;
		public static Block itemBatcher;
		public static Block fluidSorter;
		public static Block windmill;
		public static Block watermill;
		//TODO move to deco?
		public static Block treatedWallmount;
	}


	public static final class MetalDecoration
	{
		public static Block lvCoil;
		public static Block mvCoil;
		public static Block hvCoil;
		public static Block engineeringRS;
		public static Block engineeringHeavy;
		public static Block engineeringLight;
		public static Block generator;
		public static Block radiator;
		public static IEFenceBlock steelFence;
		public static IEFenceBlock aluFence;
		public static Block steelWallmount;
		public static Block aluWallmount;
		public static Block steelPost;
		public static Block aluPost;
		public static Block lantern;
		public static Block slopeSteel;
		public static Block slopeAlu;
		public static Map<CoverType, Block> metalLadder = new EnumMap<>(CoverType.class);
		public static Map<MetalScaffoldingType, Block> steelScaffolding = new EnumMap<>(MetalScaffoldingType.class);
		public static Map<MetalScaffoldingType, Block> aluScaffolding = new EnumMap<>(MetalScaffoldingType.class);
		public static Map<MetalScaffoldingType, StairsBlock> steelScaffoldingStair = new EnumMap<>(MetalScaffoldingType.class);
		public static Map<MetalScaffoldingType, StairsBlock> aluScaffoldingStair = new EnumMap<>(MetalScaffoldingType.class);
	}

	public static final class MetalDevices
	{
		public static Block razorWire;
		public static Block toolbox;
		public static Block capacitorLV;
		public static Block capacitorMV;
		public static Block capacitorHV;
		public static Block capacitorCreative;
		public static Block barrel;
		public static Block fluidPump;
		public static Block fluidPlacer;
		public static Block blastFurnacePreheater;
		public static Block furnaceHeater;
		public static Block dynamo;
		public static Block thermoelectricGen;
		public static Block electricLantern;
		public static Block chargingStation;
		public static Block fluidPipe;
		public static Block sampleDrill;
		public static Block teslaCoil;
		public static Block floodlight;
		public static Block turretChem;
		public static Block turretGun;
		public static Block cloche;
		public static final Map<ResourceLocation, Block> CONVEYORS = new HashMap<>();
		public static Map<EnumMetals, Block> chutes = new EnumMap<>(EnumMetals.class);
	}

	public static final class Connectors
	{
		public static final BiMap<Pair<String, Boolean>, Block> ENERGY_CONNECTORS = HashBiMap.create();
		public static Block connectorStructural;
		public static Block transformer;
		public static Block postTransformer;
		public static Block transformerHV;
		public static Block breakerswitch;
		public static Block redstoneBreaker;
		public static Block currentTransformer;
		public static Block connectorRedstone;
		public static Block connectorProbe;
		public static Block connectorBundled;
		public static Block feedthrough;

		public static Block getEnergyConnector(String cat, boolean relay)
		{
			return ENERGY_CONNECTORS.get(new ImmutablePair<>(cat, relay));
		}
	}

	public static final class Cloth
	{
		public static Block cushion;
		public static Block balloon;
		public static Block curtain;
		public static Block shaderBanner;
		public static Block shaderBannerWall;
	}

	public static final class Misc
	{
		public static Block hempPlant;
		public static Block fakeLight;
	}
}
