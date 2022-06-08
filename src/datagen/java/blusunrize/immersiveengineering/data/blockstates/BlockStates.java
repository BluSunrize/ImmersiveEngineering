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
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides.Type;
import blusunrize.immersiveengineering.client.models.obj.callback.block.*;
import blusunrize.immersiveengineering.client.render.tile.TurretRenderer;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.SawdustBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.data.DataGenUtils;
import blusunrize.immersiveengineering.data.models.ConveyorModelBuilder;
import blusunrize.immersiveengineering.data.models.NongeneratedModels.NongeneratedModel;
import blusunrize.immersiveengineering.data.models.SideConfigBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder;
import net.minecraftforge.client.model.generators.loaders.MultiLayerModelBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

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

	private void postBlock(Supplier<? extends Block> b, ResourceLocation texture)
	{
		ResourceLocation model = rl("block/wooden_device/wooden_post.obj.ie");
		ImmutableList.Builder<Vec3i> parts = ImmutableList.builder();
		parts.add(new Vec3i(0, 0, 0))
				.add(new Vec3i(0, 1, 0))
				.add(new Vec3i(0, 2, 0))
				.add(new Vec3i(0, 3, 0));
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
			parts.add(new BlockPos(0, 3, 0).relative(d));
		NongeneratedModel baseModel = ieObjBuilder(name(b), model, innerModels)
				.callback(PostCallbacks.INSTANCE)
				.end()
				.texture("texture", texture);
		BlockModelBuilder builder = splitModel(
				name(b)+"_split", baseModel, parts.build(), true
		);
		getVariantBuilder(b.get())
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
				{
					cubeAll(Metals.ORES.get(m), modLoc("block/metal/ore_"+name));
					cubeAll(Metals.DEEPSLATE_ORES.get(m), modLoc("block/metal/deepslate_ore_"+name));
					cubeAll(Metals.RAW_ORES.get(m), modLoc("block/metal/raw_"+name));
				}
				ResourceLocation defaultStorageTexture = modLoc("block/metal/storage_"+name);
				BlockEntry<Block> storage = Metals.STORAGE.get(m);
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
				simpleBlockAndItem(storage, storageModel);
			}
			ResourceLocation sheetmetalName = modLoc("block/metal/sheetmetal_"+name);
			cubeAll(Metals.SHEETMETAL.get(m), sheetmetalName);
			slabFor(Metals.SHEETMETAL.get(m), sheetmetalName);
		}
		for(DyeColor dye : DyeColor.values())
		{
			ResourceLocation sheetmetalName = modLoc("block/metal/sheetmetal_"+dye.getName());
			cubeAll(MetalDecoration.COLORED_SHEETMETAL.get(dye), sheetmetalName);
			slabFor(MetalDecoration.COLORED_SHEETMETAL.get(dye), sheetmetalName);
		}
		fenceBlock(WoodenDecoration.TREATED_FENCE, TREATED_FENCE_TEXTURE);
		fenceBlock(MetalDecoration.STEEL_FENCE, STEEL_FENCE_TEXTURE);
		fenceBlock(MetalDecoration.ALU_FENCE, ALU_FENCE_TEXTURE);

		cubeAll(StoneDecoration.COKEBRICK, rl("block/stone_decoration/cokebrick"));
		cubeAll(StoneDecoration.BLASTBRICK, rl("block/stone_decoration/blastbrick"));
		cubeAll(StoneDecoration.BLASTBRICK_REINFORCED, rl("block/stone_decoration/blastbrick_reinforced"));
		cubeAll(StoneDecoration.COKE, rl("block/stone_decoration/coke"));
		cubeAll(StoneDecoration.CONCRETE, rl("block/stone_decoration/concrete"));
		cubeAll(StoneDecoration.CONCRETE_LEADED, rl("block/stone_decoration/concrete_leaded"));
		cubeAll(StoneDecoration.CONCRETE_TILE, rl("block/stone_decoration/concrete_tile"));
		cubeAll(StoneDecoration.HEMPCRETE, rl("block/stone_decoration/hempcrete"));
		cubeAll(StoneDecoration.INSULATING_GLASS, rl("block/stone_decoration/insulating_glass"));
		cubeAll(StoneDecoration.ALLOYBRICK, rl("block/stone_decoration/alloybrick"));

		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			cubeAll(WoodenDecoration.TREATED_WOOD.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));
		cubeSideVertical(MetalDecoration.LV_COIL, rl("block/metal_decoration/coil_lv_side"), rl("block/metal_decoration/coil_lv_top"));
		cubeSideVertical(MetalDecoration.MV_COIL, rl("block/metal_decoration/coil_mv_side"), rl("block/metal_decoration/coil_mv_top"));
		cubeSideVertical(MetalDecoration.HV_COIL, rl("block/metal_decoration/coil_hv_side"), rl("block/metal_decoration/coil_hv_top"));
		cubeAll(MetalDecoration.ENGINEERING_RS, rl("block/metal_decoration/redstone_engineering"));
		cubeAll(MetalDecoration.ENGINEERING_HEAVY, rl("block/metal_decoration/heavy_engineering"));
		cubeAll(MetalDecoration.ENGINEERING_LIGHT, rl("block/metal_decoration/light_engineering"));
		cubeAll(MetalDecoration.GENERATOR, rl("block/metal_decoration/generator"));
		cubeAll(MetalDecoration.RADIATOR, rl("block/metal_decoration/radiator"));

		scaffold(WoodenDecoration.TREATED_SCAFFOLDING, rl("block/wooden_decoration/scaffolding"), rl("block/wooden_decoration/scaffolding_top"));

		ResourceLocation aluSide = rl("block/metal_decoration/aluminum_scaffolding");
		ResourceLocation steelSide = rl("block/metal_decoration/steel_scaffolding");
		for(MetalScaffoldingType type : MetalScaffoldingType.values())
		{
			String suffix = "_"+type.name().toLowerCase(Locale.ENGLISH);
			ResourceLocation aluTop = rl("block/metal_decoration/aluminum_scaffolding_top"+suffix);
			ResourceLocation steelTop = rl("block/metal_decoration/steel_scaffolding_top"+suffix);
			scaffold(MetalDecoration.ALU_SCAFFOLDING.get(type), aluSide, aluTop);
			scaffold(MetalDecoration.STEEL_SCAFFOLDING.get(type), steelSide, steelTop);
			slabFor(MetalDecoration.ALU_SCAFFOLDING.get(type), aluSide, aluTop, aluSide);
			slabFor(MetalDecoration.STEEL_SCAFFOLDING.get(type), steelSide, steelTop, steelSide);
			stairsFor(MetalDecoration.ALU_SCAFFOLDING.get(type), aluSide, aluTop, aluSide);
			stairsFor(MetalDecoration.STEEL_SCAFFOLDING.get(type), steelSide, steelTop, steelSide);
		}
		slabFor(StoneDecoration.COKEBRICK, rl("block/stone_decoration/cokebrick"));
		slabFor(StoneDecoration.BLASTBRICK, rl("block/stone_decoration/blastbrick"));
		slabFor(StoneDecoration.BLASTBRICK_REINFORCED, rl("block/stone_decoration/blastbrick_reinforced"));
		slabFor(StoneDecoration.COKE, rl("block/stone_decoration/coke"));
		slabFor(StoneDecoration.CONCRETE, rl("block/stone_decoration/concrete"));
		slabFor(StoneDecoration.CONCRETE_TILE, rl("block/stone_decoration/concrete_tile"));
		slabFor(StoneDecoration.CONCRETE_LEADED, rl("block/stone_decoration/concrete_leaded"));
		slabFor(StoneDecoration.HEMPCRETE, rl("block/stone_decoration/hempcrete"));
		slabFor(StoneDecoration.INSULATING_GLASS, rl("block/stone_decoration/insulating_glass"));
		slabFor(StoneDecoration.ALLOYBRICK, rl("block/stone_decoration/alloybrick"));
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			slabFor(WoodenDecoration.TREATED_WOOD.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));

		stairsFor(StoneDecoration.HEMPCRETE, rl("block/stone_decoration/hempcrete"));
		stairsFor(StoneDecoration.CONCRETE, rl("block/stone_decoration/concrete"));
		stairsFor(StoneDecoration.CONCRETE_TILE, rl("block/stone_decoration/concrete_tile"));
		stairsFor(StoneDecoration.CONCRETE_LEADED, rl("block/stone_decoration/concrete_leaded"));
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			stairsFor(WoodenDecoration.TREATED_WOOD.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));

		postBlock(WoodenDecoration.TREATED_POST, rl("block/wooden_decoration/post"));
		postBlock(MetalDecoration.STEEL_POST, rl("block/metal_decoration/steel_post"));
		postBlock(MetalDecoration.ALU_POST, rl("block/metal_decoration/aluminum_post"));

		simpleBlock(Multiblocks.BUCKET_WHEEL.get(), emptyWithParticles("block/bucket_wheel", "block/multiblocks/bucket_wheel"));
		simpleBlock(
				MetalDevices.FLUID_PIPE.get(),
				ieObjBuilder("block/metal_device/fluid_pipe.obj.ie").callback(PipeCallbacks.INSTANCE).end()
		);

		TurretRenderer.MODEL_FILE_BY_BLOCK.forEach(this::turret);
		for(Entry<EnumMetals, BlockEntry<ChuteBlock>> chute : MetalDevices.CHUTES.entrySet())
		{
			ModelFile model = ieObjBuilder("block/metal_device/chute_"+chute.getKey().tagName(), rl("block/metal_device/chute.obj.ie"))
					.callback(ChuteCallbacks.INSTANCE)
					.end()
					.texture("texture", rl("block/metal/sheetmetal_"+chute.getKey().tagName()))
					.texture("particle", rl("block/metal/sheetmetal_"+chute.getKey().tagName()));
			simpleBlock(chute.getValue().get(), model);
		}

		simpleBlock(Misc.FAKE_LIGHT.get(), EMPTY_MODEL);

		createMultistateSingleModel(WoodenDevices.WINDMILL, emptyWithParticles(
				"block/windmill", "block/wooden_device/windmill"
		));
		createMultistateSingleModel(WoodenDevices.WATERMILL, emptyWithParticles(
				"block/watermill", "block/wooden_device/watermill"
		));
		createMultistateSingleModel(MetalDecoration.LANTERN, new ConfiguredModel(
				ieObjBuilder("block/lantern.obj.ie").callback(LanternCallbacks.INSTANCE).end()
		));

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
			BlockEntry<MetalLadderBlock> steel = MetalDecoration.METAL_LADDER.get(CoverType.STEEL);
			BlockEntry<MetalLadderBlock> alu = MetalDecoration.METAL_LADDER.get(CoverType.ALU);
			BlockEntry<MetalLadderBlock> none = MetalDecoration.METAL_LADDER.get(CoverType.NONE);
			createDirectionalBlock(none, IEProperties.FACING_HORIZONTAL, noneModel);
			createDirectionalBlock(alu, IEProperties.FACING_HORIZONTAL, aluModel);
			createDirectionalBlock(steel, IEProperties.FACING_HORIZONTAL, steelModel);
			itemModel(alu, aluModel);
			itemModel(steel, steelModel);
		}

		createWallmount(WoodenDevices.TREATED_WALLMOUNT, rl("block/wooden_device/wallmount"));
		{
			ModelFile turntableModel = models().cubeBottomTop("turntable",
					modLoc("block/wooden_device/turntable"),
					modLoc("block/wooden_device/turntable_bottom"),
					modLoc("block/wooden_device/turntable_top")
			);
			createRotatedBlock(WoodenDevices.TURNTABLE, turntableModel, IEProperties.FACING_ALL, ImmutableList.of(), -90, 0);
			itemModel(WoodenDevices.TURNTABLE, turntableModel);
		}
		createWallmount(MetalDecoration.ALU_WALLMOUNT, rl("block/metal_decoration/aluminum_wallmount"));
		createWallmount(MetalDecoration.STEEL_WALLMOUNT, rl("block/metal_decoration/steel_wallmount"));
		createStructuralArm("block/metal_decoration/steel_scaffolding", MetalDecoration.STEEL_SLOPE);
		createStructuralArm("block/metal_decoration/aluminum_scaffolding", MetalDecoration.ALU_SLOPE);

		createHorizontalRotatedBlock(StoneDecoration.CORESAMPLE, obj("block/coresample.obj"));
		ResourceLocation concreteTexture = rl("block/stone_decoration/concrete");
		simpleBlockAndItem(StoneDecoration.CONCRETE_SHEET, models().carpet("concrete_sheet", concreteTexture));
		simpleBlockAndItem(StoneDecoration.CONCRETE_QUARTER, quarter("concrete_quarter", concreteTexture));
		simpleBlockAndItem(StoneDecoration.CONCRETE_THREE_QUARTER, threeQuarter("concrete_three_quarter", concreteTexture));
		simpleBlock(StoneDecoration.CONCRETE_SPRAYED.get(), obj("block/sprayed_concrete.obj"));

		createHorizontalRotatedBlock(WoodenDevices.CRAFTING_TABLE, obj("block/wooden_device/craftingtable.obj"));
		cubeAll(WoodenDevices.CRATE, modLoc("block/wooden_device/crate"));
		cubeAll(WoodenDevices.REINFORCED_CRATE, modLoc("block/wooden_device/reinforced_crate"));
		{
			ModelFile gunpowderModel = models().cubeBottomTop(
					"gunpowder_barrel", rl("block/wooden_device/gunpowder_barrel"),
					rl("block/wooden_device/barrel_up_none"), rl("block/wooden_device/gunpowder_barrel_top")
			);
			createMultistateSingleModel(WoodenDevices.GUNPOWDER_BARREL, new ConfiguredModel(gunpowderModel));
			itemModel(WoodenDevices.GUNPOWDER_BARREL, gunpowderModel);
		}
		simpleBlockAndItem(WoodenDevices.SORTER, createRouterModel(rl("block/wooden_device/sorter"),
				"router"));
		{
			ModelFile batcherModel = models().cubeBottomTop("item_batcher",
					modLoc("block/wooden_device/item_batcher"),
					modLoc("block/wooden_device/item_batcher_in"),
					modLoc("block/wooden_device/item_batcher_out")
			);
			createRotatedBlock(WoodenDevices.ITEM_BATCHER, batcherModel, IEProperties.FACING_ALL, ImmutableList.of(), -90, 0);
			itemModel(WoodenDevices.ITEM_BATCHER, batcherModel);
		}
		simpleBlockAndItem(WoodenDevices.FLUID_SORTER, createRouterModel(rl("block/wooden_device/fluid_sorter"),
				"fluid_router"));
		simpleBlockAndItem(WoodenDevices.WOODEN_BARREL,
				models().getBuilder("wooden_devices/barrel")
						.customLoader(SideConfigBuilder::begin)
						.type(Type.VERTICAL)
						.baseName(modLoc("block/wooden_device/barrel"))
						.end()
		);
		createHorizontalRotatedBlock(
				WoodenDevices.LOGIC_UNIT,
				ieObjBuilder("block/wooden_device/logic_unit.obj.ie").callback(LogicUnitCallbacks.INSTANCE).end()
		);

		createHorizontalRotatedBlock(Cloth.STRIP_CURTAIN,
				state -> new ExistingModelFile(rl(
						state.getSetStates().get(StripCurtainBlock.CEILING_ATTACHED)==Boolean.FALSE?
								"block/stripcurtain":
								"block/stripcurtain_middle"
				), existingFileHelper),
				ImmutableList.of(StripCurtainBlock.CEILING_ATTACHED));
		cubeAll(Cloth.CUSHION, modLoc("block/cushion"));
		createMultistateSingleModel(Cloth.SHADER_BANNER, EMPTY_MODEL);
		createMultistateSingleModel(Cloth.SHADER_BANNER_WALL, EMPTY_MODEL);

		simpleBlockAndItem(MetalDevices.BARREL,
				models().getBuilder("metal_devices/barrel")
						.customLoader(SideConfigBuilder::begin)
						.type(Type.VERTICAL)
						.baseName(modLoc("block/metal_device/barrel"))
						.end());

		for(Entry<BlockEntry<? extends IEEntityBlock<? extends CapacitorBlockEntity>>, String> cap : ImmutableMap.of(
				MetalDevices.CAPACITOR_CREATIVE, "creative",
				MetalDevices.CAPACITOR_LV, "lv",
				MetalDevices.CAPACITOR_MV, "mv",
				MetalDevices.CAPACITOR_HV, "hv"
		).entrySet())
		{
			ModelFile model = models().getBuilder("block/metal_device/capacitor_"+cap.getValue())
					.customLoader(SideConfigBuilder::begin)
					.type(Type.SIDE_TOP_BOTTOM)
					.baseName(modLoc("block/metal_device/capacitor_"+cap.getValue()))
					.end();
			simpleBlockAndItem(cap.getKey(), model);
		}
		{
			ModelFile model = models().getBuilder("block/metal_device/fluid_placer")
					.customLoader(SideConfigBuilder::begin)
					.type(Type.ALL_SAME_TEXTURE)
					.baseName(modLoc("block/metal_device/fluid_placer"))
					.end();
			simpleBlockAndItem(MetalDevices.FLUID_PLACER, model);
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
			createRotatedBlock(MetalDevices.FURNACE_HEATER, props -> {
				if(props.getSetStates().get(IEProperties.ACTIVE)==Boolean.TRUE)
					return furnaceHeaterOn;
				else
					return furnaceHeaterOff;
			}, IEProperties.FACING_ALL, ImmutableList.of(IEProperties.ACTIVE), 180, 0);
			itemModel(MetalDevices.FURNACE_HEATER, furnaceHeaterOff);
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
			createHorizontalRotatedBlock(MetalDevices.DYNAMO, kineticDynamo);
			itemModel(MetalDevices.DYNAMO, kineticDynamo);
		}
		simpleBlockAndItem(MetalDevices.THERMOELECTRIC_GEN, new ConfiguredModel(models().cubeBottomTop(
				"thermoelectric_generator",
				modLoc("block/metal_device/thermoelectric_gen_side"),
				modLoc("block/metal_device/thermoelectric_gen_bottom"),
				modLoc("block/metal_device/thermoelectric_gen_top")
		)));
		{
			ModelFile full = createMultiLayer("metal_device/charging_station", ImmutableMap.of(
					RenderType.solid(), modLoc("block/metal_device/charging_station.obj"),
					RenderType.translucent(), modLoc("block/metal_device/charging_station_glass.obj")
			), modLoc("block/metal_device/charging_station.obj"));
			createHorizontalRotatedBlock(MetalDevices.CHARGING_STATION, full);
			itemModel(MetalDevices.CHARGING_STATION, full);
		}
		for(BlockEntry<ConveyorBlock> b : MetalDevices.CONVEYORS.values())
			createMultistateSingleModel(b, new ConfiguredModel(
					models().getBuilder(b.getId().getPath())
							.customLoader(ConveyorModelBuilder::begin)
							.type(b.get().getType())
							.end()
			));
		createHemp();
		simpleBlock(Misc.POTTED_HEMP.get(), models().withExistingParent("potted_hemp", mcLoc("block/flower_pot_cross"))
				.texture("plant", new ResourceLocation(ImmersiveEngineering.MODID, "block/hemp/potted")));
		createSawdust();

		for(IEFluids.FluidEntry entry : IEFluids.ALL_ENTRIES)
		{
			Fluid still = entry.getStill();
			ResourceLocation stillTexture = still.getAttributes().getStillTexture();
			ModelFile model = models().getBuilder("block/fluid/"+Registry.FLUID.getKey(still).getPath())
					.texture("particle", stillTexture);
			getVariantBuilder(entry.getBlock()).partialState().setModels(new ConfiguredModel(model));
		}
		createHorizontalRotatedBlock(MetalDevices.TOOLBOX, obj("block/toolbox.obj"));
	}

	public void createStructuralArm(String texture, Supplier<? extends Block> block)
	{
		ResourceLocation objFile = modLoc("block/slope.obj.ie");
		ResourceLocation textureRL = modLoc(texture);
		ModelFile steelModel = ieObjBuilder(name(block), objFile)
				.callback(StructuralArmCallbacks.INSTANCE)
				.end()
				.texture("texture", textureRL)
				.texture("particle", textureRL)
				.parent(new ExistingModelFile(mcLoc("block/block"), existingFileHelper));
		createMultistateSingleModel(block, new ConfiguredModel(steelModel));
		itemModel(block, obj(name(block)+"_item", objFile, ImmutableMap.of("texture", textureRL), models()));
	}

	public void turret(Supplier<? extends Block> b, String loc)
	{
		BlockModelBuilder masterModel = ieObjBuilder(loc).callback(TurretCallbacks.INSTANCE).end();
		ModelFile top = models().withExistingParent(name(b)+"_top", EMPTY_MODEL.model.getLocation())
				.texture("particle", generatedParticleTextures.get(masterModel.getLocation()));
		createHorizontalRotatedBlock(
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

	public void fenceBlock(Supplier<? extends FenceBlock> b, ResourceLocation texture)
	{
		super.fenceBlock(b.get(), texture);
		itemModel(b,
				models().withExistingParent(Registry.BLOCK.getKey(b.get()).getPath()+"_inventory", mcLoc("block/fence_inventory"))
						.texture("texture", texture));
	}

	private void createMultistateSingleModel(Supplier<? extends Block> block, ConfiguredModel model)
	{
		getVariantBuilder(block.get()).partialState().setModels(model);
	}

	private void createPump()
	{
		VariantBlockStateBuilder builder = getVariantBuilder(MetalDevices.FLUID_PUMP.get());
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

	private void createDirectionalBlock(Supplier<? extends Block> b, Property<Direction> prop, ModelFile model)
	{
		VariantBlockStateBuilder builder = getVariantBuilder(b.get());
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
			builder.partialState()
					.with(prop, d)
					.setModels(new ConfiguredModel(model, 0, getAngle(d, 180), true));
	}

	private void createWallmount(Supplier<? extends Block> b, ResourceLocation texture)
	{
		VariantBlockStateBuilder stateBuilder = getVariantBuilder(b.get());
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			int rotation = getAngle(d, 0);
			for(WallmountBlock.Orientation or : Orientation.values())
			{
				ResourceLocation modelLoc = rl("block/wooden_device/wallmount"+or.modelSuffix()+".obj");
				ModelFile model = obj(Registry.BLOCK.getKey(b.get()).getPath()+or.modelSuffix(), modelLoc,
						ImmutableMap.of("texture", texture), models());
				stateBuilder.partialState()
						.with(IEProperties.FACING_HORIZONTAL, d)
						.with(WallmountBlock.ORIENTATION, or)
						.setModels(new ConfiguredModel(model, 0, rotation, true));
			}
		}
	}

	protected ModelFile createMultiLayer(String path, Map<RenderType, ResourceLocation> modelGetter, ResourceLocation particle)
	{
		MultiLayerModelBuilder<BlockModelBuilder> modelBuilder = models().getBuilder(path)
				.customLoader(MultiLayerModelBuilder::begin);

		for(Entry<RenderType, ResourceLocation> entry : modelGetter.entrySet())
		{
			ResourceLocation rl = entry.getValue();
			modelBuilder.submodel(entry.getKey(), obj(new BlockModelBuilder(rl("temp"), existingFileHelper), rl, ImmutableMap.of()));
		}
		return modelBuilder.end()
				.parent(new ExistingModelFile(mcLoc("block/block"), existingFileHelper))
				.texture("particle", DataGenUtils.getTextureFromObj(particle, existingFileHelper));
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
		VariantBlockStateBuilder builder = getVariantBuilder(Misc.HEMP_PLANT.get());
		for(EnumHempGrowth g : EnumHempGrowth.values())
		{
			ModelFile model = models().withExistingParent("block/hemp/"+g.getSerializedName(),
					new ResourceLocation("block/crop"))
					.texture("crop", g.getTextureName());
			builder.partialState()
					.with(HempBlock.GROWTH, g)
					.setModels(new ConfiguredModel(model));
		}
	}

	private void createSawdust()
	{
		VariantBlockStateBuilder builder = getVariantBuilder(WoodenDecoration.SAWDUST.get());
		ResourceLocation sawdustTexture = new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_decoration/sawdust");
		ModelFile singleModel = null;
		for(int layer : SawdustBlock.LAYERS.getPossibleValues())
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
		itemModel(WoodenDecoration.SAWDUST, singleModel);
	}

	private ModelFile createRouterModel(ResourceLocation baseTexName, String outName)
	{
		BlockModelBuilder builder = models().withExistingParent(outName, modLoc("block/ie_six_sides"));
		for(Direction d : DirectionUtils.VALUES)
			builder.texture(d.getSerializedName(), new ResourceLocation(baseTexName.getNamespace(),
					baseTexName.getPath()+"_"+d.ordinal()));
		builder.texture("particle", new ResourceLocation(baseTexName.getNamespace(),
				baseTexName.getPath()+"_0"));
		return builder;
	}
}
