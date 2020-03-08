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
		out.accept(kineticDynamo);
		out.accept(thermoelectricGen);

		out.accept(blastFurnaceOn);
		out.accept(cokeOvenOn);
		out.accept(alloySmelterOn);
		out.accept(blastFurnaceOff);
		out.accept(cokeOvenOff);
		out.accept(alloySmelterOff);

		out.accept(metalLadderNone);
		out.accept(metalLadderAlu);
		out.accept(metalLadderSteel);
		out.accept(furnaceHeaterOff);
		out.accept(furnaceHeaterOn);

		out.accept(ModelHelperOld.create(
				locForItemModel(Cloth.curtain),
				rl("block/stripcurtain"),
				ImmutableMap.of(),
				rl("item/stripcurtain"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Cloth.balloon),
				rl("block/balloon.obj.ie"),
				ImmutableMap.of(),
				rl("item/block"),
				true
		));
		out.accept(cushion);

		/* ITEMS */

		out.accept(ModelHelperOld.createWithDynamicModel(rl("coresample"), locForItemModel(IEItems.Misc.coresample)));

		for(IEFluid f : IEFluid.IE_FLUIDS)
			out.accept(ModelHelperOld.createBucket(locForItemModel(f.getFilledBucket()), f));
		out.accept(ModelHelperOld.create(
				locForItemModel(Tools.toolbox),
				rl("item/toolbox.obj"),
				ImmutableMap.of(),
				rl("item/toolbox"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Tools.voltmeter),
				rl("item/voltmeter.obj"),
				ImmutableMap.of(),
				rl("item/voltmeter"),
				true
		));

		out.accept(ModelHelperOld.create(
				locForItemModel(StoneDecoration.concreteSprayed),
				rl("block/sprayed_concrete.obj"),
				ImmutableMap.of(),
				rl("item/block"),
				true
		));
		out.accept(quarterConcreteBlock);
		out.accept(threeQuarterConcreteBlock);
		out.accept(sheetConcreteBlock);

		out.accept(gunpowderBarrel);
		out.accept(crate);
		out.accept(reinforcedCrate);
		out.accept(ModelHelperOld.create(
				locForItemModel(WoodenDevices.workbench),
				rl("block/wooden_device/workbench.obj.ie"),
				ImmutableMap.of(),
				rl("item/workbench"),
				true
		));
		out.accept(router);
		out.accept(fluidRouter);
		out.accept(ModelHelperOld.createBasicCube(
				rl("block/wooden_device/barrel_side"),
				rl("block/wooden_device/barrel_up_none"),
				rl("block/wooden_device/barrel_up_none"),
				locForItemModel(WoodenDevices.woodenBarrel)));

		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.belljar),
				rl("block/metal_device/belljar.obj.ie"),
				ImmutableMap.of(),
				rl("item/belljar"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.teslaCoil),
				rl("block/metal_device/teslacoil.obj"),
				ImmutableMap.of(),
				rl("item/teslacoil"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.floodlight),
				rl("block/metal_device/floodlight.obj.ie"),
				ImmutableMap.of(),
				rl("item/floodlight"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.turretChem),
				rl("block/metal_device/chem_turret_inv.obj"),
				ImmutableMap.of(),
				rl("item/turret"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.turretGun),
				rl("block/metal_device/gun_turret_inv.obj"),
				ImmutableMap.of(),
				rl("item/turret"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.fluidPipe),
				rl("block/metal_device/fluid_pipe.obj.ie"),
				ImmutableMap.of(),
				rl("item/block"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.fluidPump),
				rl("block/metal_device/fluid_pump_inv.obj"),
				ImmutableMap.of(),
				rl("item/fluid_pump"),
				true
		));
		out.accept(ModelHelperOld.createBasicCube(
				rl("block/metal_device/barrel_side"),
				rl("block/metal_device/barrel_up_none"),
				rl("block/metal_device/barrel_up_none"),
				locForItemModel(MetalDevices.barrel)));
		for(Entry<Block, String> cap : ImmutableMap.of(
				MetalDevices.capacitorCreative, "creative",
				MetalDevices.capacitorLV, "lv",
				MetalDevices.capacitorMV, "mv",
				MetalDevices.capacitorHV, "hv"
		).entrySet())
			out.accept(ModelHelperOld.createWithDynamicModel(rl("smartmodel/conf_sides_hud_metal_device/capacitor_"+cap.getValue()),
					locForItemModel(cap.getKey())));
		for(Block b : MetalDevices.CONVEYORS.values())
			out.accept(ModelHelperOld.createWithDynamicModel(rl("conveyor"), locForItemModel(b)));

		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.getEnergyConnector(WireType.LV_CATEGORY, false)),
				ImmutableMap.of(),
				rl("block/connector/connector_lv.obj")
		));
		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true)),
				ImmutableMap.of("#immersiveengineering:block/connector/connector_lv", rl("block/connector/relay_lv")),
				rl("block/connector/connector_lv.obj")
		));

		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false)),
				ImmutableMap.of(),
				rl("block/connector/connector_mv.obj")
		));
		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true)),
				ImmutableMap.of("#immersiveengineering:block/connector/connector_mv", rl("block/connector/relay_mv")),
				rl("block/connector/connector_mv.obj")
		));

		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false)),
				ImmutableMap.of(),
				rl("block/connector/connector_hv.obj")
		));
		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true)),
				ImmutableMap.of(),
				rl("block/connector/relay_hv.obj")
		));

		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.connectorRedstone),
				ImmutableMap.of(),
				rl("block/connector/connector_redstone.obj.ie")
		));
		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.connectorProbe),
				ImmutableMap.of(),
				rl("block/connector/connector_probe.obj.ie")
		));
		out.accept(ModelHelperOld.createConnectorModel(
				locForItemModel(Connectors.connectorStructural),
				ImmutableMap.of(),
				rl("block/connector/connector_structural.obj.ie")
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Connectors.transformer),
				rl("block/connector/transformer_mv_left.obj"),
				ImmutableMap.of(),
				rl("item/transformer"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Connectors.transformerHV),
				rl("block/connector/transformer_hv_left.obj"),
				ImmutableMap.of(),
				rl("item/transformer"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Connectors.redstoneBreaker),
				rl("block/connector/redstone_breaker.obj.ie"),
				ImmutableMap.of(),
				rl("item/redstone_breaker"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Connectors.currentTransformer),
				rl("block/connector/e_meter.obj"),
				ImmutableMap.of(),
				rl("item/current_transformer"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Connectors.breakerswitch),
				rl("block/connector/breaker_switch_off.obj.ie"),
				ImmutableMap.of(),
				rl("item/breaker_switch"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.razorWire),
				rl("block/razor_wire.obj.ie"),
				ImmutableMap.of(),
				rl("item/block"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.blastFurnacePreheater),
				rl("block/metal_device/blastfurnace_preheater.obj"),
				ImmutableMap.of(),
				rl("item/blastfurnace_preheater"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.electricLantern),
				rl("block/metal_device/e_lantern.obj"),
				ImmutableMap.of(),
				rl("item/block"),
				true
		));
		out.accept(chargingStation);
		out.accept(ModelHelperOld.create(
				locForItemModel(MetalDevices.sampleDrill),
				rl("block/metal_device/core_drill.obj"),
				ImmutableMap.of(),
				rl("item/sampledrill"),
				true
		));

		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.alloySmelter),
				alloySmelterOn,
				ImmutableMap.of(),
				rl("item/alloysmelter")
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.blastFurnace),
				blastFurnaceOn,
				ImmutableMap.of(),
				rl("item/blastfurnace")
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.cokeOven),
				cokeOvenOn,
				ImmutableMap.of(),
				rl("item/blastfurnace")
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.blastFurnaceAdv),
				rl("block/blastfurnace_advanced.obj"),
				ImmutableMap.of(),
				rl("item/multiblock"),
				true
		));

		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.metalPress),
				rl("block/metal_multiblock/metal_press.obj"),
				ImmutableMap.of(),
				rl("item/multiblock"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.crusher),
				rl("block/metal_multiblock/crusher.obj"),
				ImmutableMap.of(),
				rl("item/crusher"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.tank),
				rl("block/metal_multiblock/tank.obj"),
				ImmutableMap.of(),
				rl("item/tank"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.silo),
				rl("block/metal_multiblock/silo.obj"),
				ImmutableMap.of(),
				rl("item/silo"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.assembler),
				rl("block/metal_multiblock/assembler.obj"),
				ImmutableMap.of(),
				rl("item/multiblock"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.autoWorkbench),
				rl("block/metal_multiblock/auto_workbench.obj"),
				ImmutableMap.of(),
				rl("item/multiblock"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.bottlingMachine),
				rl("block/metal_multiblock/bottling_machine.obj"),
				ImmutableMap.of(),
				rl("item/bottling_machine"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.squeezer),
				rl("block/metal_multiblock/squeezer.obj"),
				ImmutableMap.of(),
				rl("item/multiblock"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.fermenter),
				rl("block/metal_multiblock/fermenter.obj"),
				ImmutableMap.of(),
				rl("item/multiblock"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.refinery),
				rl("block/metal_multiblock/refinery.obj"),
				ImmutableMap.of(),
				rl("item/refinery"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.dieselGenerator),
				rl("block/metal_multiblock/diesel_generator.obj"),
				ImmutableMap.of(),
				rl("item/crusher"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.excavator),
				rl("block/metal_multiblock/excavator.obj"),
				ImmutableMap.of(),
				rl("item/excavator"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.bucketWheel),
				rl("block/metal_multiblock/bucket_wheel.obj.ie"),
				ImmutableMap.of(),
				rl("item/bucket_wheel"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.arcFurnace),
				rl("block/metal_multiblock/arc_furnace.obj"),
				ImmutableMap.of(),
				rl("item/arc_furnace"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.lightningrod),
				rl("block/metal_multiblock/lightningrod.obj"),
				ImmutableMap.of(),
				rl("item/multiblock"),
				true
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Multiblocks.mixer),
				rl("block/metal_multiblock/mixer.obj"),
				ImmutableMap.of(),
				rl("item/multiblock"),
				true
		));
		for(EnumHempGrowth g : EnumHempGrowth.values())
		{
			GeneratedModelFile gen = ModelHelperOld.create(
					rl("block/hemp/"+g.getName()),
					new ResourceLocation("block/crop"),
					ImmutableMap.of(
							"crop", g.getTextureName()
					),
					null, true
			);
			out.accept(gen);
			hempGrowth.put(g, gen);
		}
		out.accept(ModelHelperOld.createTEIR_IEOBJ(
				locForItemModel(Misc.fluorescentTube),
				rl("item/fluorescent_tube.obj.ie"),
				rl("item/fluorescent_tube")
		));
		out.accept(ModelHelperOld.create(
				locForItemModel(Misc.shield),
				rl("item/shield.obj.ie"),
				ImmutableMap.of(),
				rl("item/shield"),
				true
		));

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
