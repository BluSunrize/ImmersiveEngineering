/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides.Type;
import blusunrize.immersiveengineering.client.models.ModelConveyor.ConveyorLoader;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.SawdustBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import blusunrize.immersiveengineering.data.DataGenUtils;
import blusunrize.immersiveengineering.data.models.MultiLayerBuilder;
import blusunrize.immersiveengineering.data.models.SideConfigBuilder;
import blusunrize.immersiveengineering.data.models.SpecialModelBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.DyeColor;
import net.minecraft.state.Property;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;

public class BlockStates extends ExtendedBlockstateProvider
{
	private static final ResourceLocation ALU_FENCE_TEXTURE = rl("block/metal/storage_aluminum");
	private static final ResourceLocation STEEL_FENCE_TEXTURE = rl("block/metal/storage_steel");
	private static final ResourceLocation TREATED_FENCE_TEXTURE = rl("block/wooden_decoration/treated_wood_horizontal");
	private final ConfiguredModel EMPTY_MODEL;

	public BlockStates(DataGenerator gen, ExistingFileHelper exHelper)
	{
		super(gen, exHelper);
		EMPTY_MODEL = new ConfiguredModel(
				new ExistingModelFile(modLoc("block/ie_empty"), existingFileHelper)
		);
	}

	private void postBlock(Block b, ResourceLocation texture)
	{
		ResourceLocation model = rl("block/wooden_device/wooden_post.obj.ie");
		ImmutableList.Builder<Vector3i> parts = ImmutableList.builder();
		parts.add(new Vector3i(0, 0, 0))
				.add(new Vector3i(0, 1, 0))
				.add(new Vector3i(0, 2, 0))
				.add(new Vector3i(0, 3, 0));
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
			parts.add(new BlockPos(0, 3, 0).offset(d));
		ModelFile baseModel = ieObj(name(b), model)
				.texture("texture", texture);
		BlockModelBuilder builder = splitModel(
				name(b)+"_split", baseModel, parts.build(), true
		);
		getVariantBuilder(b)
				.partialState()
				.setModels(new ConfiguredModel(builder));
	}

	@Override
	protected void registerStatesAndModels()
	{
		for(EnumMetals m : EnumMetals.values())
		{
			String name = m.tagName();
			if(!m.isVanillaMetal())
			{
				if(m.shouldAddOre())
					cubeAll(Metals.ores.get(m), modLoc("block/metal/ore_"+name));
				ResourceLocation defaultStorageTexture = modLoc("block/metal/storage_"+name);
				Block storage = Metals.storage.get(m);
				String storageName = name(storage);
				BlockModelBuilder storageModel;
				if(m==EnumMetals.URANIUM)
				{
					ResourceLocation side = modLoc("block/metal/storage_"+name+"_side");
					ResourceLocation top = modLoc("block/metal/storage_"+name+"_top");
					storageModel = models().cubeBottomTop(storageName, side, top, top);
					slabFor(storage, side, top, top);
				}
				else
				{
					storageModel = models().cubeAll(storageName, defaultStorageTexture);
					slabFor(storage, defaultStorageTexture);
				}
				simpleBlockItem(storage, storageModel);
			}
			ResourceLocation sheetmetalName = modLoc("block/metal/sheetmetal_"+name);
			cubeAll(Metals.sheetmetal.get(m), sheetmetalName);
			slabFor(Metals.sheetmetal.get(m), sheetmetalName);
		}
		for(DyeColor dye : DyeColor.values())
		{
			ResourceLocation sheetmetalName = modLoc("block/metal/sheetmetal_"+dye.getTranslationKey());
			cubeAll(MetalDecoration.coloredSheetmetal.get(dye), sheetmetalName);
			slabFor(MetalDecoration.coloredSheetmetal.get(dye), sheetmetalName);
		}
		fenceBlock(WoodenDecoration.treatedFence, TREATED_FENCE_TEXTURE);
		fenceBlock(MetalDecoration.steelFence, STEEL_FENCE_TEXTURE);
		fenceBlock(MetalDecoration.aluFence, ALU_FENCE_TEXTURE);

		cubeAll(StoneDecoration.cokebrick, rl("block/stone_decoration/cokebrick"));
		cubeAll(StoneDecoration.blastbrick, rl("block/stone_decoration/blastbrick"));
		cubeAll(StoneDecoration.blastbrickReinforced, rl("block/stone_decoration/blastbrick_reinforced"));
		cubeAll(StoneDecoration.coke, rl("block/stone_decoration/coke"));
		cubeAll(StoneDecoration.concrete, rl("block/stone_decoration/concrete"));
		cubeAll(StoneDecoration.concreteLeaded, rl("block/stone_decoration/concrete_leaded"));
		cubeAll(StoneDecoration.concreteTile, rl("block/stone_decoration/concrete_tile"));
		cubeAll(StoneDecoration.hempcrete, rl("block/stone_decoration/hempcrete"));
		cubeAll(StoneDecoration.insulatingGlass, rl("block/stone_decoration/insulating_glass"));
		cubeAll(StoneDecoration.alloybrick, rl("block/stone_decoration/alloybrick"));

		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			cubeAll(WoodenDecoration.treatedWood.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));
		cubeSideVertical(MetalDecoration.lvCoil, rl("block/metal_decoration/coil_lv_side"), rl("block/metal_decoration/coil_lv_top"));
		cubeSideVertical(MetalDecoration.mvCoil, rl("block/metal_decoration/coil_mv_side"), rl("block/metal_decoration/coil_mv_top"));
		cubeSideVertical(MetalDecoration.hvCoil, rl("block/metal_decoration/coil_hv_side"), rl("block/metal_decoration/coil_hv_top"));
		cubeAll(MetalDecoration.engineeringRS, rl("block/metal_decoration/redstone_engineering"));
		cubeAll(MetalDecoration.engineeringHeavy, rl("block/metal_decoration/heavy_engineering"));
		cubeAll(MetalDecoration.engineeringLight, rl("block/metal_decoration/light_engineering"));
		cubeAll(MetalDecoration.generator, rl("block/metal_decoration/generator"));
		cubeAll(MetalDecoration.radiator, rl("block/metal_decoration/radiator"));

		scaffold(WoodenDecoration.treatedScaffolding, rl("block/wooden_decoration/scaffolding"), rl("block/wooden_decoration/scaffolding_top"));

		ResourceLocation aluSide = rl("block/metal_decoration/aluminum_scaffolding");
		ResourceLocation steelSide = rl("block/metal_decoration/steel_scaffolding");
		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			String suffix = "_"+type.name().toLowerCase(Locale.ENGLISH);
			ResourceLocation aluTop = rl("block/metal_decoration/aluminum_scaffolding_top"+suffix);
			ResourceLocation steelTop = rl("block/metal_decoration/steel_scaffolding_top"+suffix);
			scaffold(MetalDecoration.aluScaffolding.get(type), aluSide, aluTop);
			scaffold(MetalDecoration.steelScaffolding.get(type), steelSide, steelTop);
			slabFor(MetalDecoration.aluScaffolding.get(type), aluSide, aluTop, aluSide);
			slabFor(MetalDecoration.steelScaffolding.get(type), steelSide, steelTop, steelSide);
			stairs(MetalDecoration.aluScaffoldingStair.get(type), aluSide, aluTop, aluSide);
			stairs(MetalDecoration.steelScaffoldingStair.get(type), steelSide, steelTop, steelSide);
		}
		slabFor(StoneDecoration.cokebrick, rl("block/stone_decoration/cokebrick"));
		slabFor(StoneDecoration.blastbrick, rl("block/stone_decoration/blastbrick"));
		slabFor(StoneDecoration.blastbrickReinforced, rl("block/stone_decoration/blastbrick_reinforced"));
		slabFor(StoneDecoration.coke, rl("block/stone_decoration/coke"));
		slabFor(StoneDecoration.concrete, rl("block/stone_decoration/concrete"));
		slabFor(StoneDecoration.concreteTile, rl("block/stone_decoration/concrete_tile"));
		slabFor(StoneDecoration.concreteLeaded, rl("block/stone_decoration/concrete_leaded"));
		slabFor(StoneDecoration.hempcrete, rl("block/stone_decoration/hempcrete"));
		slabFor(StoneDecoration.insulatingGlass, rl("block/stone_decoration/insulating_glass"));
		slabFor(StoneDecoration.alloybrick, rl("block/stone_decoration/alloybrick"));
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			slabFor(WoodenDecoration.treatedWood.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));

		stairs(StoneDecoration.hempcreteStairs, rl("block/stone_decoration/hempcrete"));
		stairs(StoneDecoration.concreteStairs[0], rl("block/stone_decoration/concrete"));
		stairs(StoneDecoration.concreteStairs[1], rl("block/stone_decoration/concrete_tile"));
		stairs(StoneDecoration.concreteStairs[2], rl("block/stone_decoration/concrete_leaded"));
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			stairs(WoodenDecoration.treatedStairs.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));

		postBlock(WoodenDecoration.treatedPost, rl("block/wooden_decoration/post"));
		postBlock(MetalDecoration.steelPost, rl("block/metal_decoration/steel_post"));
		postBlock(MetalDecoration.aluPost, rl("block/metal_decoration/aluminum_post"));

		simpleBlock(Multiblocks.bucketWheel, emptyWithParticles("block/bucket_wheel", "block/multiblocks/bucket_wheel"));
		simpleBlock(MetalDevices.fluidPipe, ieObj("block/metal_device/fluid_pipe.obj.ie"));

		turret(MetalDevices.turretChem, ieObj("block/metal_device/chem_turret.obj.ie"));
		turret(MetalDevices.turretGun, ieObj("block/metal_device/gun_turret.obj.ie"));
		for(Entry<EnumMetals, Block> chute : MetalDevices.chutes.entrySet())
		{
			ModelFile model = ieObj("block/metal_device/chute_"+chute.getKey().tagName(), rl("block/metal_device/chute.obj.ie"))
					.texture("texture", rl("block/metal/sheetmetal_"+chute.getKey().tagName()))
					.texture("particle", rl("block/metal/sheetmetal_"+chute.getKey().tagName()));
			simpleBlock(chute.getValue(), model);
		}

		simpleBlock(Misc.fakeLight, EMPTY_MODEL);

		createMultistateSingleModel(WoodenDevices.windmill, emptyWithParticles(
				"block/windmill", "block/wooden_device/windmill"
		));
		createMultistateSingleModel(WoodenDevices.watermill, emptyWithParticles(
				"block/watermill", "block/wooden_device/watermill"
		));
		createMultistateSingleModel(MetalDecoration.lantern,
				new ConfiguredModel(ieObj("block/lantern.obj.ie")));

		{
			ModelFile noneModel = createMetalLadder("metal_ladder", null, null);
			ModelFile aluModel = createMetalLadder(
					"metal_ladder_alu",
					rl("block/metal_decoration/aluminum_scaffolding_open"),
					rl("block/metal_decoration/aluminum_scaffolding"));
			ModelFile steelModel = createMetalLadder(
					"metal_ladder_steel",
					rl("block/metal_decoration/steel_scaffolding_open"),
					rl("block/metal_decoration/steel_scaffolding"));
			Block steel = MetalDecoration.metalLadder.get(CoverType.STEEL);
			Block alu = MetalDecoration.metalLadder.get(CoverType.ALU);
			Block none = MetalDecoration.metalLadder.get(CoverType.NONE);
			createDirectionalBlock(none, IEProperties.FACING_HORIZONTAL, noneModel);
			createDirectionalBlock(alu, IEProperties.FACING_HORIZONTAL, aluModel);
			createDirectionalBlock(steel, IEProperties.FACING_HORIZONTAL, steelModel);
			itemModel(alu, aluModel);
			itemModel(steel, steelModel);
		}

		createWallmount(WoodenDevices.treatedWallmount, rl("block/wooden_device/wallmount"));
		{
			ModelFile turntableModel = models().cubeBottomTop("turntable",
					modLoc("block/wooden_device/turntable"),
					modLoc("block/wooden_device/turntable_bottom"),
					modLoc("block/wooden_device/turntable_top")
			);
			createRotatedBlock(WoodenDevices.turntable, s -> turntableModel, IEProperties.FACING_ALL, ImmutableList.of(), -90, 0);
			itemModel(WoodenDevices.turntable, turntableModel);
		}
		createWallmount(MetalDecoration.aluWallmount, rl("block/metal_decoration/aluminum_wallmount"));
		createWallmount(MetalDecoration.steelWallmount, rl("block/metal_decoration/steel_wallmount"));
		{
			ModelFile steelModel = ieObj("block/slope.obj.ie")
					.texture("texture", modLoc("block/metal_decoration/steel_scaffolding"))
					.texture("particle", modLoc("block/metal_decoration/steel_scaffolding"))
					.parent(new ExistingModelFile(mcLoc("block/block"), existingFileHelper));
			ModelFile aluModel = ieObj("slope_alu", modLoc("block/slope.obj.ie"))
					.texture("texture", modLoc("block/metal_decoration/aluminum_scaffolding"))
					.texture("particle", modLoc("block/metal_decoration/aluminum_scaffolding"))
					.parent(new ExistingModelFile(mcLoc("block/block"), existingFileHelper));
			createMultistateSingleModel(MetalDecoration.slopeSteel, new ConfiguredModel(steelModel));
			itemModel(MetalDecoration.slopeSteel, steelModel);
			createMultistateSingleModel(MetalDecoration.slopeAlu, new ConfiguredModel(aluModel));
			itemModel(MetalDecoration.slopeAlu, aluModel);
		}

		createRotatedBlock(StoneDecoration.coresample, map -> obj("block/coresample.obj"),
				ImmutableList.of());
		ResourceLocation concreteTexture = rl("block/stone_decoration/concrete");
		simpleBlockItem(StoneDecoration.concreteSheet, models().carpet("concrete_sheet", concreteTexture));
		simpleBlockItem(StoneDecoration.concreteQuarter, quarter("concrete_quarter", concreteTexture));
		simpleBlockItem(StoneDecoration.concreteThreeQuarter, threeQuarter("concrete_three_quarter", concreteTexture));
		simpleBlock(StoneDecoration.concreteSprayed, obj("block/sprayed_concrete.obj"));

		createRotatedBlock(WoodenDevices.craftingTable, state -> obj("block/wooden_device/craftingtable.obj"),
				ImmutableList.of());
		cubeAll(WoodenDevices.crate, modLoc("block/wooden_device/crate"));
		cubeAll(WoodenDevices.reinforcedCrate, modLoc("block/wooden_device/reinforced_crate"));
		{
			ModelFile gunpowderModel = models().cubeBottomTop(
					"gunpowder_barrel", rl("block/wooden_device/gunpowder_barrel"),
					rl("block/wooden_device/barrel_up_none"), rl("block/wooden_device/gunpowder_barrel_top")
			);
			createMultistateSingleModel(WoodenDevices.gunpowderBarrel, new ConfiguredModel(gunpowderModel));
			itemModel(WoodenDevices.gunpowderBarrel, gunpowderModel);
		}
		simpleBlockItem(WoodenDevices.sorter, createRouterModel(rl("block/wooden_device/sorter"),
				"router"));
		{
			ModelFile batcherModel = models().cubeBottomTop("item_batcher",
					modLoc("block/wooden_device/item_batcher"),
					modLoc("block/wooden_device/item_batcher_in"),
					modLoc("block/wooden_device/item_batcher_out")
			);
			createRotatedBlock(WoodenDevices.itemBatcher, s -> batcherModel, IEProperties.FACING_ALL, ImmutableList.of(), -90, 0);
			itemModel(WoodenDevices.itemBatcher, batcherModel);
		}
		simpleBlockItem(WoodenDevices.fluidSorter, createRouterModel(rl("block/wooden_device/fluid_sorter"),
				"fluid_router"));
		simpleBlockItem(WoodenDevices.woodenBarrel,
				models().getBuilder("wooden_devices/barrel")
						.customLoader(SideConfigBuilder::begin)
						.type(Type.VERTICAL)
						.baseName(modLoc("block/wooden_device/barrel"))
						.end()
		);
		createRotatedBlock(WoodenDevices.logicUnit, state -> ieObj("block/wooden_device/logic_unit.obj.ie"), ImmutableList.of());

		createRotatedBlock(Cloth.curtain,
				state -> new ExistingModelFile(rl(
						state.getSetStates().get(StripCurtainBlock.CEILING_ATTACHED)==Boolean.FALSE?
								"block/stripcurtain":
								"block/stripcurtain_middle"
				), existingFileHelper),
				ImmutableList.of(StripCurtainBlock.CEILING_ATTACHED));
		cubeAll(Cloth.cushion, modLoc("block/cushion"));
		createMultistateSingleModel(Cloth.shaderBanner, EMPTY_MODEL);
		createMultistateSingleModel(Cloth.shaderBannerWall, EMPTY_MODEL);

		simpleBlockItem(MetalDevices.barrel,
				models().getBuilder("metal_devices/barrel")
						.customLoader(SideConfigBuilder::begin)
						.type(Type.VERTICAL)
						.baseName(modLoc("block/metal_device/barrel"))
						.end());

		for(Entry<Block, String> cap : ImmutableMap.of(
				MetalDevices.capacitorCreative, "creative",
				MetalDevices.capacitorLV, "lv",
				MetalDevices.capacitorMV, "mv",
				MetalDevices.capacitorHV, "hv"
		).entrySet())
		{
			ModelFile model = models().getBuilder("block/metal_device/capacitor_"+cap.getValue())
					.customLoader(SideConfigBuilder::begin)
					.type(Type.SIDE_TOP_BOTTOM)
					.baseName(modLoc("block/metal_device/capacitor_"+cap.getValue()))
					.end();
			simpleBlockItem(cap.getKey(), model);
		}
		{
			ModelFile model = models().getBuilder("block/metal_device/fluid_placer")
					.customLoader(SideConfigBuilder::begin)
					.type(Type.ALL_SAME_TEXTURE)
					.baseName(modLoc("block/metal_device/fluid_placer"))
					.end();
			simpleBlockItem(MetalDevices.fluidPlacer, model);
		}
		{
			ModelFile furnaceHeaterOn = models().withExistingParent("furnace_heater_on", rl("block/ie_six_sides_overlay_all_but_one"))
					.texture("block_all", rl("block/metal_device/furnace_heater_active"))
					.texture("block_north", rl("block/metal_device/furnace_heater_socket"))
					.texture("overlay_all", rl("block/metal_device/furnace_heater_active_overlay"));
			ModelFile furnaceHeaterOff = models().withExistingParent("furnace_heater_off", rl("block/ie_six_sides_overlay_all_but_one"))
					.texture("block_all", rl("block/metal_device/furnace_heater"))
					.texture("block_north", rl("block/metal_device/furnace_heater_socket"))
					.texture("overlay_all", rl("block/metal_device/furnace_heater_overlay"));
			createRotatedBlock(MetalDevices.furnaceHeater, props -> {
				if(props.getSetStates().get(IEProperties.ACTIVE)==Boolean.TRUE)
					return furnaceHeaterOn;
				else
					return furnaceHeaterOff;
			}, IEProperties.FACING_ALL, ImmutableList.of(IEProperties.ACTIVE), 180, 0);
			itemModel(MetalDevices.furnaceHeater, furnaceHeaterOff);
		}
		createPump();
		{
			ModelFile kineticDynamo = models().withExistingParent("kinetic_dynamo", mcLoc("block/cube"))
					.texture("down", modLoc("block/metal_device/dynamo_top"))
					.texture("south", modLoc("block/metal_device/dynamo_top"))
					.texture("up", modLoc("block/metal_device/dynamo_top"))
					.texture("north", modLoc("block/metal_device/dynamo_front"))
					.texture("west", modLoc("block/metal_device/dynamo_side"))
					.texture("east", modLoc("block/metal_device/dynamo_side"))
					.texture("particle", modLoc("block/metal_device/dynamo_side"));
			createRotatedBlock(MetalDevices.dynamo, state -> kineticDynamo,
					ImmutableList.of());
			itemModel(MetalDevices.dynamo, kineticDynamo);
		}
		simpleBlockItem(MetalDevices.thermoelectricGen, new ConfiguredModel(models().cubeBottomTop(
				"thermoelectric_generator",
				modLoc("block/metal_device/thermoelectric_gen_side"),
				modLoc("block/metal_device/thermoelectric_gen_bottom"),
				modLoc("block/metal_device/thermoelectric_gen_top")
		)));
		{
			ModelFile full = createMultiLayer("metal_device/charging_station", renderType -> {
				if("solid".equals(renderType))
					return modLoc("models/block/metal_device/charging_station.obj");
				else if("translucent".equals(renderType))
					return modLoc("models/block/metal_device/charging_station_glass.obj");
				return null;
			}, modLoc("block/metal_device/charging_station.obj"));
			createRotatedBlock(MetalDevices.chargingStation,
					state -> full,
					ImmutableList.of()
			);
			itemModel(MetalDevices.chargingStation, full);
		}
		for(Block b : MetalDevices.CONVEYORS.values())
			createMultistateSingleModel(b, new ConfiguredModel(
					models().getBuilder("metal_device/conveyor")
							.customLoader(SpecialModelBuilder.forLoader(ConveyorLoader.LOCATION))
							.end()
			));
		createHemp();
		simpleBlock(Misc.pottedHemp, models().withExistingParent("potted_hemp", mcLoc("block/flower_pot_cross"))
				.texture("plant", new ResourceLocation(ImmersiveEngineering.MODID, "block/hemp/potted")));
		createSawdust();

		for(IEFluid f : IEFluid.IE_FLUIDS)
		{
			ResourceLocation stillTexture = f.getAttributes().getStillTexture();
			ModelFile model = models().getBuilder("block/fluid/"+f.getRegistryName().getPath())
					.texture("particle", stillTexture);
			getVariantBuilder(f.block).partialState().setModels(new ConfiguredModel(model));
		}
		createRotatedBlock(MetalDevices.toolbox, state -> obj("block/toolbox.obj"),
				ImmutableList.of());
	}

	public void turret(Block b, ModelFile masterModel)
	{
		ModelFile top = models().withExistingParent(name(b)+"_top", EMPTY_MODEL.model.getLocation())
				.texture("particle", generatedParticleTextures.get(masterModel.getLocation()));
		createRotatedBlock(
				b,
				s -> {
					if(s.getSetStates().get(IEProperties.MULTIBLOCKSLAVE)==Boolean.TRUE)
						return top;
					else
						return masterModel;
				},
				ImmutableList.of(IEProperties.MULTIBLOCKSLAVE)
		);
	}

	public void fenceBlock(FenceBlock b, ResourceLocation texture)
	{
		super.fenceBlock(b, texture);
		itemModel(b,
				models().withExistingParent(b.getRegistryName().getPath()+"_inventory", mcLoc("block/fence_inventory"))
						.texture("texture", texture));
	}

	private void createMultistateSingleModel(Block block, ConfiguredModel model)
	{
		getVariantBuilder(block)
				.partialState()
				.setModels(model);
	}

	private void createPump()
	{
		VariantBlockStateBuilder builder = getVariantBuilder(MetalDevices.fluidPump);
		builder.partialState()
				.with(IEProperties.MULTIBLOCKSLAVE, true)
				.setModels(new ConfiguredModel(obj("block/metal_device/fluid_pump.obj"),
						0, 0, false));
		builder.partialState()
				.with(IEProperties.MULTIBLOCKSLAVE, false)
				.setModels(new ConfiguredModel(
						models().getBuilder("metal_device/pump_bottom")
								.customLoader(SideConfigBuilder::begin)
								.type(Type.SIDE_VERTICAL)
								.baseName(modLoc("block/metal_device/fluid_pump"))
								.end()
				));
	}

	private void createRotatedBlock(Block block, Function<PartialBlockstate, ModelFile> model, List<Property<?>> additionalProps)
	{
		createRotatedBlock(block, model, IEProperties.FACING_HORIZONTAL, additionalProps, 0, 180);
	}

	private void createRotatedBlock(Block block, Function<PartialBlockstate, ModelFile> model, Property<Direction> facing,
									List<Property<?>> additionalProps, int offsetRotX, int offsetRotY)
	{
		VariantBlockStateBuilder stateBuilder = getVariantBuilder(block);
		forEachState(stateBuilder.partialState(), additionalProps, state -> {
			ModelFile modelLoc = model.apply(state);
			for(Direction d : facing.getAllowedValues())
			{
				int x;
				int y;
				switch(d)
				{
					case UP:
						x = 90;
						y = 0;
						break;
					case DOWN:
						x = -90;
						y = 0;
						break;
					default:
						y = getAngle(d, offsetRotY);
						x = 0;
				}
				state.with(facing, d).setModels(new ConfiguredModel(modelLoc, x+offsetRotX, y, false));
			}
		});
	}

	public ModelFile createMetalLadder(String name, @Nullable ResourceLocation bottomTop, @Nullable ResourceLocation sides)
	{
		Map<String, ResourceLocation> textures = new HashMap<>();
		ResourceLocation parent;
		if(bottomTop!=null)
		{
			Preconditions.checkNotNull(sides);
			parent = new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_scaffoldladder");
			textures.put("top", bottomTop);
			textures.put("bottom", bottomTop);
			textures.put("side", sides);
		}
		else
			parent = new ResourceLocation(ImmersiveEngineering.MODID, "block/ie_ladder");
		textures.put("ladder", rl("block/metal_decoration/metal_ladder"));
		BlockModelBuilder ret = models().withExistingParent(name, parent);
		for(Entry<String, ResourceLocation> e : textures.entrySet())
			ret.texture(e.getKey(), e.getValue());
		return ret;
	}

	private void createDirectionalBlock(Block b, Property<Direction> prop, ModelFile model)
	{
		VariantBlockStateBuilder builder = getVariantBuilder(b);
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
			builder.partialState()
					.with(prop, d)
					.setModels(new ConfiguredModel(model, 0, getAngle(d, 180), true));
	}

	private void createWallmount(Block b, ResourceLocation texture)
	{
		VariantBlockStateBuilder stateBuilder = getVariantBuilder(b);
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			int rotation = getAngle(d, 0);
			for(WallmountBlock.Orientation or : Orientation.values())
			{
				ResourceLocation modelLoc = rl("block/wooden_device/wallmount"+or.modelSuffix()+".obj");
				ModelFile model = obj(b.getRegistryName().getPath()+or.modelSuffix(), modelLoc,
						ImmutableMap.of("texture", texture));
				stateBuilder.partialState()
						.with(IEProperties.FACING_HORIZONTAL, d)
						.with(WallmountBlock.ORIENTATION, or)
						.setModels(new ConfiguredModel(model, 0, rotation, true));
			}
		}
	}

	private final static String[] RENDER_LAYERS = {"solid", "cutout_mipped", "cutout", "translucent"};

	protected ModelFile createMultiLayer(String path, Function<String, ResourceLocation> modelGetter, ResourceLocation particle)
	{
		MultiLayerBuilder<BlockModelBuilder> modelBuilder = models().getBuilder(path)
				.customLoader(MultiLayerBuilder::begin);

		for(String renderType : RENDER_LAYERS)
		{
			ResourceLocation rl = modelGetter.apply(renderType);
			if(rl!=null)
			{
				JsonObject object = new JsonObject();
				object.addProperty("loader", forgeLoc("obj").toString());
				object.addProperty("detectCullableFaces", false);
				object.addProperty("flip-v", true);
				object.addProperty("model", rl.toString());
				modelBuilder.addLayer(renderType, object);
			}
		}
		return modelBuilder.end()
				.parent(new ExistingModelFile(mcLoc("block/block"), existingFileHelper))
				.texture("particle", DataGenUtils.getTextureFromObj(particle, existingFileHelper));
	}

	public static <T extends Comparable<T>> void forEach(PartialBlockstate base, Property<T> prop,
														 List<Property<?>> remaining, Consumer<PartialBlockstate> out)
	{
		for(T value : prop.getAllowedValues())
			forEachState(base, remaining, map -> {
				map = map.with(prop, value);
				out.accept(map);
			});
	}

	public static void forEachState(PartialBlockstate base, List<Property<?>> props, Consumer<PartialBlockstate> out)
	{
		if(props.size() > 0)
		{
			List<Property<?>> remaining = props.subList(1, props.size());
			Property<?> main = props.get(0);
			forEach(base, main, remaining, out);
		}
		else
			out.accept(base);
	}

	private ModelFile quarter(String out, ResourceLocation texture)
	{
		return models().withExistingParent(out, modLoc("block/ie_quarter_block"))
				.texture("texture", texture);
	}

	private ModelFile threeQuarter(String out, ResourceLocation texture)
	{
		return models().withExistingParent(out, modLoc("block/ie_three_quarter_block"))
				.texture("texture", texture);
	}

	private void createHemp()
	{
		VariantBlockStateBuilder builder = getVariantBuilder(Misc.hempPlant);
		for(EnumHempGrowth g : EnumHempGrowth.values())
		{
			ModelFile model = models().withExistingParent("block/hemp/"+g.getString(),
					new ResourceLocation("block/crop"))
					.texture("crop", g.getTextureName());
			builder.partialState()
					.with(HempBlock.GROWTH, g)
					.setModels(new ConfiguredModel(model));
		}
	}

	private void createSawdust()
	{
		VariantBlockStateBuilder builder = getVariantBuilder(WoodenDecoration.sawdust);
		ResourceLocation sawdustTexture = new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_decoration/sawdust");
		ModelFile singleModel = null;
		for(int layer : SawdustBlock.LAYERS.getAllowedValues())
		{
			String name = "block/sawdust_"+layer;
			ModelFile model;
			if(layer==9)
				model = models().cubeAll(name, sawdustTexture);
			else
			{
				int height = layer*2-1;
				model = models().withExistingParent(name, new ResourceLocation("block/thin_block"))
						.texture("particle", sawdustTexture)
						.texture("texture", sawdustTexture)
						.element().from(0, 0, 0).to(16, height, 16).allFaces((direction, faceBuilder) -> {
							if(direction.getAxis()==Axis.Y)
								faceBuilder.uvs(0, 0, 16, 16).texture("#texture");
							else
								faceBuilder.uvs(0, 16-height, 16, 16).texture("#texture").cullface(direction);
							if(direction!=Direction.UP)
								faceBuilder.cullface(direction);
						})
						.end();
			}
			if(layer==1)
				singleModel = model;
			builder.partialState()
					.with(SawdustBlock.LAYERS, layer)
					.setModels(new ConfiguredModel(model));
		}
		itemModel(WoodenDecoration.sawdust, singleModel);
	}

	private ModelFile createRouterModel(ResourceLocation baseTexName, String outName)
	{
		BlockModelBuilder builder = models().withExistingParent(outName, modLoc("block/ie_six_sides"));
		for(Direction d : DirectionUtils.VALUES)
			builder.texture(d.getString(), new ResourceLocation(baseTexName.getNamespace(),
					baseTexName.getPath()+"_"+d.ordinal()));
		builder.texture("particle", new ResourceLocation(baseTexName.getNamespace(),
				baseTexName.getPath()+"_0"));
		return builder;
	}
}
