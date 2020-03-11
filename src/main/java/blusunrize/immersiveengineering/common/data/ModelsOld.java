/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.data.model_old.ModelFile;
import blusunrize.immersiveengineering.common.data.model_old.ModelFile.ExistingModelFileIE;
import blusunrize.immersiveengineering.common.data.model_old.ModelFile.GeneratedModelFile;
import blusunrize.immersiveengineering.common.data.model_old.ModelGenerator;
import blusunrize.immersiveengineering.common.data.model_old.ModelHelperOld;
import blusunrize.immersiveengineering.common.items.IEItems;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.items.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class ModelsOld extends ModelGenerator
{
	final Map<EnumHempGrowth, ModelFile> hempGrowth = new EnumMap<>(EnumHempGrowth.class);

	final GeneratedModelFile blastFurnaceOff = ModelHelperOld.createThreeCubed(rl("block/multiblocks/blast_furnace_off"),
			rl("block/multiblocks/blast_furnace"), rl("block/multiblocks/blast_furnace_off"));
	final GeneratedModelFile blastFurnaceOn = ModelHelperOld.createThreeCubed(rl("block/multiblocks/blast_furnace_on"),
			rl("block/multiblocks/blast_furnace"), rl("block/multiblocks/blast_furnace_on"));
	final GeneratedModelFile cokeOvenOff = ModelHelperOld.createThreeCubed(rl("block/multiblocks/coke_oven_off"),
			rl("block/multiblocks/coke_oven"), rl("block/multiblocks/coke_oven_off"));
	final GeneratedModelFile cokeOvenOn = ModelHelperOld.createThreeCubed(rl("block/multiblocks/coke_oven_on"),
			rl("block/multiblocks/coke_oven"), rl("block/multiblocks/coke_oven_on"));
	final GeneratedModelFile alloySmelterOff = ModelHelperOld.createTwoCubed(rl("block/multiblocks/alloy_smelter_off"),
			rl("block/multiblocks/alloy_smelter_bottom"), rl("block/multiblocks/alloy_smelter_top"),
			rl("block/multiblocks/alloy_smelter_side"), rl("block/multiblocks/alloy_smelter_off"));
	final GeneratedModelFile alloySmelterOn = ModelHelperOld.createTwoCubed(rl("block/multiblocks/alloy_smelter_on"),
			rl("block/multiblocks/alloy_smelter_bottom"), rl("block/multiblocks/alloy_smelter_top"),
			rl("block/multiblocks/alloy_smelter_side"), rl("block/multiblocks/alloy_smelter_on"));

	final GeneratedModelFile metalLadderNone = ModelHelperOld.createMetalLadder(
			locForItemModel(MetalDecoration.metalLadder.get(CoverType.NONE)),
			null, null);
	final GeneratedModelFile metalLadderAlu = ModelHelperOld.createMetalLadder(
			locForItemModel(MetalDecoration.metalLadder.get(CoverType.ALU)),
			rl("block/metal_decoration/aluminum_scaffolding_open"),
			rl("block/metal_decoration/aluminum_scaffolding"));
	final GeneratedModelFile metalLadderSteel = ModelHelperOld.createMetalLadder(
			locForItemModel(MetalDecoration.metalLadder.get(CoverType.STEEL)),
			rl("block/metal_decoration/steel_scaffolding_open"),
			rl("block/metal_decoration/steel_scaffolding"));

	final GeneratedModelFile quarterConcreteBlock = ModelHelperOld.createQuarterBlock(rl("block/stone_decoration/concrete"),
			locForItemModel(StoneDecoration.concreteQuarter));
	final GeneratedModelFile threeQuarterConcreteBlock = ModelHelperOld.createThreeQuarterBlock(rl("block/stone_decoration/concrete"),
			locForItemModel(StoneDecoration.concreteThreeQuarter));
	final GeneratedModelFile sheetConcreteBlock = ModelHelperOld.createCarpetBlock(rl("block/stone_decoration/concrete"),
			locForItemModel(StoneDecoration.concreteSheet));


	final GeneratedModelFile gunpowderBarrel = ModelHelperOld.createBasicCube(rl("block/wooden_device/gunpowder_barrel"),
			rl("block/wooden_device/gunpowder_barrel_top"), rl("block/wooden_device/barrel_up_none"),
			locForItemModel(WoodenDevices.gunpowderBarrel));
	final GeneratedModelFile crate = ModelHelperOld.createBasicCube(rl("block/wooden_device/crate"),
			locForItemModel(WoodenDevices.crate));
	final GeneratedModelFile reinforcedCrate = ModelHelperOld.createBasicCube(rl("block/wooden_device/reinforced_crate"),
			locForItemModel(WoodenDevices.reinforcedCrate));
	final GeneratedModelFile router = createRouterModel(rl("block/wooden_device/sorter"),
			locForItemModel(WoodenDevices.sorter));
	final GeneratedModelFile fluidRouter = createRouterModel(rl("block/wooden_device/fluid_sorter"),
			locForItemModel(WoodenDevices.fluidSorter));
	final GeneratedModelFile furnaceHeaterOff = ModelHelperOld.create(
			locForItemModel(MetalDevices.furnaceHeater),
			rl("block/ie_six_sides_overlay_all_but_one"),
			ImmutableMap.of(
					"block_all", rl("block/metal_device/furnace_heater"),
					"block_north", rl("block/metal_device/furnace_heater_socket"),
					"overlay_all", rl("block/metal_device/furnace_heater_overlay")
			),
			true
	);
	final GeneratedModelFile furnaceHeaterOn = ModelHelperOld.create(
			rl("block/furnace_heater_on"),
			rl("block/ie_six_sides_overlay_all_but_one"),
			ImmutableMap.of(
					"block_all", rl("block/metal_device/furnace_heater_active"),
					"block_north", rl("block/metal_device/furnace_heater_socket"),
					"overlay_all", rl("block/metal_device/furnace_heater_active_overlay")
			),
			true
	);
	final GeneratedModelFile kineticDynamo = ModelHelperOld.createBasicCube(side -> {
		switch(Preconditions.checkNotNull(side))
		{
			case DOWN:
			case SOUTH:
				return rl("block/metal_device/dynamo_bottom");
			case UP:
				return rl("block/metal_device/dynamo_top");
			case NORTH:
				return rl("block/metal_device/dynamo_front");
			case WEST:
			case EAST:
				return rl("block/metal_device/dynamo_side");
			default:
				throw new IllegalArgumentException();
		}
	}, locForItemModel(MetalDevices.dynamo));
	final GeneratedModelFile thermoelectricGen = ModelHelperOld.createBasicCube(
			rl("block/metal_device/thermoelectric_gen_side"),
			rl("block/metal_device/thermoelectric_gen_top"),
			rl("block/metal_device/thermoelectric_gen_bottom"),
			locForItemModel(MetalDevices.thermoelectricGen)
	);
	final GeneratedModelFile chargingStation = ModelHelperOld.createMultilayer(
			locForItemModel(MetalDevices.chargingStation),
			ImmutableMap.of(
					BlockRenderLayer.SOLID, new ExistingModelFileIE(rl("block/metal_device/charging_station.obj")),
					BlockRenderLayer.TRANSLUCENT, new ExistingModelFileIE(rl("block/metal_device/charging_station_glass.obj"))
			),
			rl("item/block")
	);

	final GeneratedModelFile cushion = ModelHelperOld.createBasicCube(rl("block/cushion"),
			locForItemModel(Cloth.cushion));

	final Map<Block, ModelFile> fluidModels = new HashMap<>();

	public ModelsOld(DataGenerator gen)
	{
		super(gen);
	}

	@Override
	protected void registerModels(Consumer<GeneratedModelFile> out)
	{
		/* ITEMS */

		out.accept(quarterConcreteBlock);
		out.accept(threeQuarterConcreteBlock);
		out.accept(sheetConcreteBlock);

		out.accept(gunpowderBarrel);
		out.accept(crate);
		out.accept(reinforcedCrate);

		out.accept(router);
		out.accept(fluidRouter);

	}

	private GeneratedModelFile createRouterModel(ResourceLocation baseTexName, ResourceLocation outName)
	{
		Map<String, ResourceLocation> textures = new HashMap<>();
		for(Direction d : Direction.VALUES)
			textures.put(d.getName(), new ResourceLocation(baseTexName.getNamespace(),
					baseTexName.getPath()+"_"+d.ordinal()));
		textures.put("particle", textures.get("down"));
		return ModelHelperOld.create(outName, rl("block/ie_six_sides"), textures, true);
	}

	private ResourceLocation locForItemModel(Block b)
	{
		return locForItemModel(Item.getItemFromBlock(b));
	}

	private static ResourceLocation locForItemModel(Item item)
	{
		ResourceLocation itemName = item.getRegistryName();
		return new ResourceLocation(itemName.getNamespace(), "item/"+itemName.getPath());
	}

}
