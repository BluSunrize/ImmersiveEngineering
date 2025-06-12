/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.api.EnumMetals;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides.Type;
import blusunrize.immersiveengineering.client.models.obj.callback.block.*;
import blusunrize.immersiveengineering.client.render.tile.TurretRenderer;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlock;
import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainBlock;
import blusunrize.immersiveengineering.common.blocks.generic.CatwalkBlock;
import blusunrize.immersiveengineering.common.blocks.generic.CatwalkStairsBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.WarningSignBlock.WarningSignIcon;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.BlueprintShelfBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.SawdustBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.common.register.IEBlocks.*;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import blusunrize.immersiveengineering.data.DataGenUtils;
import blusunrize.immersiveengineering.data.models.ConveyorModelBuilder;
import blusunrize.immersiveengineering.data.models.ModelProviderUtils;
import blusunrize.immersiveengineering.data.models.NongeneratedModels.NongeneratedModel;
import blusunrize.immersiveengineering.data.models.SideConfigBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.client.model.generators.ModelFile.ExistingModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.CompositeModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.ImmersiveEngineering.rl;
import static blusunrize.immersiveengineering.api.IEApi.ieLoc;
import static net.minecraft.client.renderer.RenderType.*;

public class BlockStates extends ExtendedBlockstateProvider
{
	private static final ResourceLocation ALU_FENCE_TEXTURE = rl("block/metal/storage_aluminum");
	private static final ResourceLocation STEEL_FENCE_TEXTURE = rl("block/metal/storage_steel");
	private static final ResourceLocation TREATED_FENCE_TEXTURE = rl("block/wooden_decoration/treated_wood_horizontal");
	private final ConfiguredModel EMPTY_MODEL;

	public BlockStates(PackOutput output, ExistingFileHelper exHelper)
	{
		super(output, exHelper);
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
		fenceGateBlock(WoodenDecoration.TREATED_FENCE_GATE, TREATED_FENCE_TEXTURE);
		fenceGateBlock(MetalDecoration.STEEL_FENCE_GATE, STEEL_FENCE_TEXTURE);
		fenceGateBlock(MetalDecoration.ALU_FENCE_GATE, ALU_FENCE_TEXTURE);

		cubeAll(StoneDecoration.COKEBRICK, rl("block/stone_decoration/cokebrick"));
		cubeAll(StoneDecoration.BLASTBRICK, rl("block/stone_decoration/blastbrick"));
		cubeAll(StoneDecoration.BLASTBRICK_REINFORCED, rl("block/stone_decoration/blastbrick_reinforced"));
		multiEightCubeAll(StoneDecoration.SLAG_BRICK, rl("block/stone_decoration/slag_brick/slag_brick"));
		multiEightCubeAll(StoneDecoration.CLINKER_BRICK, rl("block/stone_decoration/clinker_brick/clinker_brick"));
		simpleBlockAndItem(StoneDecoration.CLINKER_BRICK_SILL, new ConfiguredModel(models().cubeBottomTop(
				"clinker_brick_sill",
				modLoc("block/stone_decoration/clinker_brick/clinker_brick_sill_side"),
				modLoc("block/stone_decoration/clinker_brick/clinker_brick0"),
				modLoc("block/stone_decoration/clinker_brick/clinker_brick_sill_top")
		)));
		multiEightCubeAll(StoneDecoration.SLAG_GRAVEL, rl("block/stone_decoration/slag_gravel/slag_gravel"));
		multiEightCubeAll(StoneDecoration.GRIT_SAND, rl("block/stone_decoration/grit_sand/grit_sand"));
		cubeAll(StoneDecoration.COKE, rl("block/stone_decoration/coke"));
		multiEightCubeAll(StoneDecoration.CONCRETE, rl("block/stone_decoration/concrete/concrete"));
		multiEightCubeAll(StoneDecoration.CONCRETE_BRICK, rl("block/stone_decoration/concrete_brick/concrete_brick"));
		cubeAll(StoneDecoration.CONCRETE_BRICK_CRACKED, rl("block/stone_decoration/concrete_brick_cracked"));
		cubeAll(StoneDecoration.CONCRETE_CHISELED, rl("block/stone_decoration/concrete_chiseled"));
		cubeSideVertical(StoneDecoration.CONCRETE_PILLAR, rl("block/stone_decoration/concrete_pillar"), rl("block/stone_decoration/concrete_tile/concrete_tile0"));
		cubeAll(StoneDecoration.CONCRETE_LEADED, rl("block/stone_decoration/concrete_leaded"));
		cubeAll(StoneDecoration.CONCRETE_REINFORCED, rl("block/stone_decoration/concrete_reinforced"));
		cubeAll(StoneDecoration.CONCRETE_REINFORCED_TILE, rl("block/stone_decoration/concrete_reinforced_tile"));
		multiEightCubeAll(StoneDecoration.CONCRETE_TILE, rl("block/stone_decoration/concrete_tile/concrete_tile"));
		multiEightCubeAll(StoneDecoration.HEMPCRETE, rl("block/stone_decoration/hempcrete/hempcrete"));
		multiEightCubeAll(StoneDecoration.HEMPCRETE_BRICK, rl("block/stone_decoration/hempcrete_brick/hempcrete_brick"));
		cubeAll(StoneDecoration.HEMPCRETE_BRICK_CRACKED, rl("block/stone_decoration/hempcrete_brick_cracked"));
		cubeAll(StoneDecoration.HEMPCRETE_CHISELED, rl("block/stone_decoration/hempcrete_chiseled"));
		cubeSideVertical(StoneDecoration.HEMPCRETE_PILLAR, rl("block/stone_decoration/hempcrete_pillar"), rl("block/stone_decoration/hempcrete/hempcrete0"));
		cubeAll(StoneDecoration.INSULATING_GLASS, rl("block/stone_decoration/insulating_glass"), translucent());
		cubeAll(StoneDecoration.SLAG_GLASS, rl("block/stone_decoration/slag_glass"), translucent());
		cubeAll(StoneDecoration.ALLOYBRICK, rl("block/stone_decoration/alloybrick"));
		cubeAll(StoneDecoration.DUROPLAST, rl("block/stone_decoration/duroplast"), translucent());

		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			cubeAll(WoodenDecoration.TREATED_WOOD.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));
		cubeAll(WoodenDecoration.FIBERBOARD, rl("block/wooden_decoration/fiberboard"));
		cubeAll(WoodenDecoration.BASIC_ENGINEERING, rl("block/wooden_decoration/basic_engineering"));
		cubeSideVertical(MetalDecoration.LV_COIL, rl("block/metal_decoration/coil_lv_side"), rl("block/metal_decoration/coil_lv_top"));
		cubeSideVertical(MetalDecoration.MV_COIL, rl("block/metal_decoration/coil_mv_side"), rl("block/metal_decoration/coil_mv_top"));
		cubeSideVertical(MetalDecoration.HV_COIL, rl("block/metal_decoration/coil_hv_side"), rl("block/metal_decoration/coil_hv_top"));
		cubeAll(MetalDecoration.ENGINEERING_RS, rl("block/metal_decoration/redstone_engineering"));
		cubeAll(MetalDecoration.ENGINEERING_HEAVY, rl("block/metal_decoration/heavy_engineering"));
		cubeAll(MetalDecoration.ENGINEERING_LIGHT, rl("block/metal_decoration/light_engineering"));
		cubeAll(MetalDecoration.ENGINEERING_RESONANZ, rl("block/metal_decoration/resonanz_engineering"));
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
			slabFor(MetalDecoration.ALU_SCAFFOLDING.get(type), aluSide, aluTop, aluSide, cutout());
			slabFor(MetalDecoration.STEEL_SCAFFOLDING.get(type), steelSide, steelTop, steelSide, cutout());
			stairsFor(MetalDecoration.ALU_SCAFFOLDING.get(type), aluSide, aluTop, aluSide, cutout());
			stairsFor(MetalDecoration.STEEL_SCAFFOLDING.get(type), steelSide, steelTop, steelSide, cutout());
		}
		slabFor(StoneDecoration.COKEBRICK, rl("block/stone_decoration/cokebrick"));
		slabFor(StoneDecoration.BLASTBRICK, rl("block/stone_decoration/blastbrick"));
		slabFor(StoneDecoration.BLASTBRICK_REINFORCED, rl("block/stone_decoration/blastbrick_reinforced"));
		slabForMultiEightAll(StoneDecoration.SLAG_BRICK, rl("block/stone_decoration/slag_brick/slag_brick"));
		slabForMultiEightAll(StoneDecoration.CLINKER_BRICK, rl("block/stone_decoration/clinker_brick/clinker_brick"));
		slabFor(StoneDecoration.COKE, rl("block/stone_decoration/coke"));
		slabForMultiEightAll(StoneDecoration.CONCRETE, rl("block/stone_decoration/concrete/concrete"));
		slabForMultiEightAll(StoneDecoration.CONCRETE_BRICK, rl("block/stone_decoration/concrete_brick/concrete_brick"));
		slabForMultiEightAll(StoneDecoration.CONCRETE_TILE, rl("block/stone_decoration/concrete_tile/concrete_tile"));
		slabFor(StoneDecoration.CONCRETE_LEADED, rl("block/stone_decoration/concrete_leaded"));
		slabFor(StoneDecoration.CONCRETE_REINFORCED, rl("block/stone_decoration/concrete_reinforced"));
		slabFor(StoneDecoration.CONCRETE_REINFORCED_TILE, rl("block/stone_decoration/concrete_reinforced_tile"));
		slabForMultiEightAll(StoneDecoration.HEMPCRETE, rl("block/stone_decoration/hempcrete/hempcrete"));
		slabForMultiEightAll(StoneDecoration.HEMPCRETE_BRICK, rl("block/stone_decoration/hempcrete_brick/hempcrete_brick"));
		slabFor(StoneDecoration.INSULATING_GLASS, rl("block/stone_decoration/insulating_glass"), translucent());
		slabFor(StoneDecoration.ALLOYBRICK, rl("block/stone_decoration/alloybrick"));
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			slabFor(WoodenDecoration.TREATED_WOOD.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));

		stairsForMultiEightAll(StoneDecoration.SLAG_BRICK, rl("block/stone_decoration/slag_brick/slag_brick"));
		stairsForMultiEightAll(StoneDecoration.CLINKER_BRICK, rl("block/stone_decoration/clinker_brick/clinker_brick"));
		stairsForMultiEightAll(StoneDecoration.HEMPCRETE, rl("block/stone_decoration/hempcrete/hempcrete"));
		stairsForMultiEightAll(StoneDecoration.HEMPCRETE_BRICK, rl("block/stone_decoration/hempcrete_brick/hempcrete_brick"));
		stairsForMultiEightAll(StoneDecoration.CONCRETE, rl("block/stone_decoration/concrete/concrete"));
		stairsForMultiEightAll(StoneDecoration.CONCRETE_BRICK, rl("block/stone_decoration/concrete_brick/concrete_brick"));
		stairsForMultiEightAll(StoneDecoration.CONCRETE_TILE, rl("block/stone_decoration/concrete_tile/concrete_tile"));
		stairsFor(StoneDecoration.CONCRETE_LEADED, rl("block/stone_decoration/concrete_leaded"));
		for(TreatedWoodStyles style : TreatedWoodStyles.values())
			stairsFor(WoodenDecoration.TREATED_WOOD.get(style), rl("block/wooden_decoration/treated_wood_"+style.name().toLowerCase(Locale.ENGLISH)));

		wallForMultiEight(StoneDecoration.SLAG_BRICK,
				rl("block/stone_decoration/slag_brick/slag_brick"),
				rl("block/stone_decoration/slag_brick/slag_brick_wall"),
				rl("block/stone_decoration/slag_brick/slag_brick_top"));
		wallForMultiEight(StoneDecoration.CLINKER_BRICK,
				rl("block/stone_decoration/clinker_brick/clinker_brick"),
				rl("block/stone_decoration/clinker_brick/clinker_brick_wall"),
				rl("block/stone_decoration/clinker_brick/clinker_brick_top"));
		wallForMultiEight(StoneDecoration.HEMPCRETE,
				rl("block/stone_decoration/hempcrete/hempcrete"),
				rl("block/stone_decoration/hempcrete/hempcrete"),
				rl("block/stone_decoration/hempcrete/hempcrete"));
		wallForMultiEight(StoneDecoration.HEMPCRETE_BRICK,
				rl("block/stone_decoration/hempcrete_brick/hempcrete_brick"),
				rl("block/stone_decoration/hempcrete_brick/hempcrete_brick"),
				rl("block/stone_decoration/hempcrete_brick/hempcrete_brick"));
		wallForMultiEight(StoneDecoration.CONCRETE,
				rl("block/stone_decoration/concrete/concrete"),
				rl("block/stone_decoration/concrete/concrete"),
				rl("block/stone_decoration/concrete/concrete"));
		wallForMultiEight(StoneDecoration.CONCRETE_BRICK,
				rl("block/stone_decoration/concrete_brick/concrete_brick"),
				rl("block/stone_decoration/concrete_brick/concrete_brick"),
				rl("block/stone_decoration/concrete_brick/concrete_brick"));
		wallForMultiEight(StoneDecoration.CONCRETE_TILE,
				rl("block/stone_decoration/concrete_tile/concrete_tile"),
				rl("block/stone_decoration/concrete_tile/concrete_tile"),
				rl("block/stone_decoration/concrete_tile/concrete_tile"));
		wallForSingle(StoneDecoration.CONCRETE_LEADED,
				rl("block/stone_decoration/concrete_leaded"),
				rl("block/stone_decoration/concrete_leaded"),
				rl("block/stone_decoration/concrete_leaded_wall_top"));

		postBlock(WoodenDecoration.TREATED_POST, rl("block/wooden_decoration/post"));
		postBlock(MetalDecoration.STEEL_POST, rl("block/metal_decoration/steel_post"));
		postBlock(MetalDecoration.ALU_POST, rl("block/metal_decoration/aluminum_post"));

		simpleBlock(
				IEMultiblockLogic.BUCKET_WHEEL.block().get(),
				emptyWithParticles("block/bucket_wheel", "block/multiblocks/bucket_wheel")
		);
		simpleBlock(
				MetalDevices.FLUID_PIPE.get(),
				ieObjBuilder("block/metal_device/fluid_pipe.obj.ie").callback(PipeCallbacks.INSTANCE).layer(cutout()).end()
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
		for(Entry<DyeColor, BlockEntry<ChuteBlock>> chute : MetalDevices.DYED_CHUTES.entrySet())
		{
			ModelFile model = ieObjBuilder("block/metal_device/chute_colored_"+chute.getKey().getName(), rl("block/metal_device/chute.obj.ie"))
					.callback(ChuteCallbacks.INSTANCE)
					.end()
					.texture("texture", rl("block/metal/sheetmetal_"+chute.getKey().getName()))
					.texture("particle", rl("block/metal/sheetmetal_"+chute.getKey().getName()));
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
//			BlockModelBuilder offModel = obj(
//					"block/cagelamp_off", rl("block/cagelamp.obj"),
//					ImmutableMap.of("texture", modLoc("block/metal_decoration/cagelamp")),
//					models()
//			);
//			BlockModelBuilder onModel = obj(
//					"block/cagelamp_on", rl("block/cagelamp.obj"),
//					ImmutableMap.of("texture", modLoc("block/metal_decoration/cagelamp_on")),
//					models()
//			);

			BlockModelBuilder offModel = ieObjBuilder(
					"block/cagelamp_off", rl("block/cagelamp.obj")
			).callback(CagelampCallbacks.INSTANCE).end().texture("texture", modLoc("block/metal_decoration/cagelamp"));
			BlockModelBuilder onModel = ieObjBuilder(
					"block/cagelamp_on", rl("block/cagelamp.obj")
			).callback(CagelampCallbacks.INSTANCE).end().texture("texture", modLoc("block/metal_decoration/cagelamp_on"));

			createRotatedBlock(
					MetalDecoration.CAGELAMP,
					state -> state.getSetStates().get(IEProperties.ACTIVE)==Boolean.TRUE?onModel: offModel,
					IEProperties.FACING_ALL,
					List.of(IEProperties.ACTIVE),
					270, 0
			);
		}

		{
			ModelFile noneModel = createMetalLadder("metal_ladder", null, null, null);
			ModelFile aluModel = createMetalLadder(
					"metal_ladder_alu",
					rl("block/metal_decoration/aluminum_scaffolding_open"),
					rl("block/metal_decoration/aluminum_scaffolding"),
					null);
			ModelFile aluModelOpen = createMetalLadder(
					"metal_ladder_alu_open",
					rl("block/metal_decoration/aluminum_scaffolding_open_u"),
					rl("block/metal_decoration/aluminum_scaffolding"),
					rl("block/metal_decoration/aluminum_scaffolding_open_sides"));
			ModelFile steelModel = createMetalLadder(
					"metal_ladder_steel",
					rl("block/metal_decoration/steel_scaffolding_open"),
					rl("block/metal_decoration/steel_scaffolding"),
					null);
			ModelFile steelModelOpen = createMetalLadder(
					"metal_ladder_steel_open",
					rl("block/metal_decoration/steel_scaffolding_open_u"),
					rl("block/metal_decoration/steel_scaffolding"),
					rl("block/metal_decoration/steel_scaffolding_open_sides"));
			BlockEntry<MetalLadderBlock> steel = MetalDecoration.METAL_LADDER.get(CoverType.STEEL);
			BlockEntry<MetalLadderBlock> alu = MetalDecoration.METAL_LADDER.get(CoverType.ALU);
			BlockEntry<MetalLadderBlock> none = MetalDecoration.METAL_LADDER.get(CoverType.NONE);
			createDirectionalBlock(none, LadderBlock.FACING, noneModel);
			createLadderBlock(alu, aluModel, aluModelOpen);
			createLadderBlock(steel, steelModel, steelModelOpen);
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

		{
			ModelFile windowModel = models()
					.withExistingParent("treated_window", modLoc("block/window_thick"))
					.texture("frame", "immersiveengineering:block/wooden_decoration/treated_wood_vertical");
			createRotatedBlock(WoodenDecoration.WINDOW, windowModel, IEProperties.FACING_ALL, List.of(), 0, 180);
			itemModel(WoodenDecoration.WINDOW, windowModel);
		}
		{
			ModelFile windowModel = models()
					.withExistingParent("steel_window", modLoc("block/window_thin"))
					.texture("frame", "immersiveengineering:block/metal/storage_steel");
			createRotatedBlock(MetalDecoration.STEEL_WINDOW, windowModel, IEProperties.FACING_ALL, List.of(), 0, 180);
			itemModel(MetalDecoration.STEEL_WINDOW, windowModel);
		}
		{
			ModelFile windowModel = models()
					.withExistingParent("alu_window", modLoc("block/window_thin"))
					.texture("frame", "immersiveengineering:block/metal/storage_aluminum");
			createRotatedBlock(MetalDecoration.ALU_WINDOW, windowModel, IEProperties.FACING_ALL, List.of(), 0, 180);
			itemModel(MetalDecoration.ALU_WINDOW, windowModel);
		}
		{
			ModelFile windowModel = models()
					.withExistingParent("reinforced_window", modLoc("block/window_thick"))
					.texture("glass", "immersiveengineering:block/panzerglass")
					.texture("frame", "minecraft:block/netherite_block");
			createRotatedBlock(MetalDecoration.REINFORCED_WINDOW, windowModel, IEProperties.FACING_ALL, List.of(), 0, 180);
			itemModel(MetalDecoration.REINFORCED_WINDOW, windowModel);
		}
		createCatwalk(WoodenDecoration.CATWALK,
				"immersiveengineering:block/wooden_decoration/scaffolding_top",
				"immersiveengineering:block/wooden_decoration/scaffolding"
		);
		createCatwalkStairs(WoodenDecoration.CATWALK_STAIRS,
				"immersiveengineering:block/wooden_decoration/scaffolding_top",
				"immersiveengineering:block/wooden_decoration/scaffolding"
		);
		createCatwalk(MetalDecoration.STEEL_CATWALK,
				"immersiveengineering:block/metal_decoration/steel_scaffolding_top_grate_top",
				"immersiveengineering:block/metal_decoration/steel_scaffolding"
		);
		createCatwalkStairs(MetalDecoration.STEEL_CATWALK_STAIRS,
				"immersiveengineering:block/metal_decoration/steel_scaffolding_top_grate_top",
				"immersiveengineering:block/metal_decoration/steel_scaffolding"
		);
		createCatwalk(MetalDecoration.ALU_CATWALK,
				"immersiveengineering:block/metal_decoration/aluminum_scaffolding_top_grate_top",
				"immersiveengineering:block/metal_decoration/aluminum_scaffolding"
		);
		createCatwalkStairs(MetalDecoration.ALU_CATWALK_STAIRS,
				"immersiveengineering:block/metal_decoration/aluminum_scaffolding_top_grate_top",
				"immersiveengineering:block/metal_decoration/aluminum_scaffolding"
		);

		createDoor(WoodenDecoration.DOOR, "block/wooden_decoration/treated_door");
		createDoor(WoodenDecoration.DOOR_FRAMED, "block/wooden_decoration/treated_door_framed");
		createDoor(MetalDecoration.STEEL_DOOR, "block/metal_decoration/steel_door");
		createTrapdoor(WoodenDecoration.TRAPDOOR, "block/wooden_decoration/treated_trapdoor");
		createTrapdoor(WoodenDecoration.TRAPDOOR_FRAMED, "block/wooden_decoration/treated_trapdoor_framed");
		createTrapdoor(MetalDecoration.STEEL_TRAPDOOR, "block/metal_decoration/steel_trapdoor");

		createSigns(WoodenDecoration.SIGN, "block/wooden_decoration/treated_wood_horizontal");
		createSigns(MetalDecoration.STEEL_SIGN, "block/metal/storage_steel");
		createSigns(MetalDecoration.ALU_SIGN, "block/metal/storage_aluminum");

		for(Entry<WarningSignIcon, BlockEntry<IEBaseBlock>> warningSign : MetalDecoration.WARNING_SIGNS.entrySet())
		{
			String name = warningSign.getKey().getSerializedName();
			ModelFile model = models()
					.withExistingParent("warning_sign_"+name, modLoc("block/warning_sign"))
					.texture("icon", rl("block/metal_decoration/sign/icon_"+name));
			createHorizontalRotatedBlock(warningSign.getValue(), model);
		}

		createHorizontalRotatedBlock(StoneDecoration.CORESAMPLE, obj("block/coresample.obj"));
		ResourceLocation concreteTexture = rl("block/stone_decoration/concrete/concrete0");
		simpleBlockAndItem(StoneDecoration.CONCRETE_SHEET, models().carpet("concrete_sheet", concreteTexture));
		simpleBlockAndItem(StoneDecoration.CONCRETE_QUARTER, quarter("concrete_quarter", concreteTexture));
		simpleBlockAndItem(StoneDecoration.CONCRETE_THREE_QUARTER, threeQuarter("concrete_three_quarter", concreteTexture));
		simpleBlock(StoneDecoration.CONCRETE_SPRAYED.get(), obj("block/sprayed_concrete.obj", cutout()));

		createHorizontalRotatedBlock(WoodenDevices.CRAFTING_TABLE, obj("block/wooden_device/craftingtable.obj"));
		{
			MultiPartBlockStateBuilder multipartBuilder = getMultipartBuilder(WoodenDevices.BLUEPRINT_SHELF.get());
			for(Direction d : Direction.values())
			{
				int rotX = d.getAxis()==Axis.Y?-90: 0;
				int rotY = d.getAxis()==Axis.Y?0: getAngle(d, 180);
				// add the frame
				multipartBuilder.part()
						.modelFile(models().getExistingFile(modLoc("block/blueprint_shelf/frame")))
						.rotationX(rotX)
						.rotationY(rotY)
						.addModel()
						.condition(IEProperties.FACING_ALL, d)
						.end();
				// add the blueprints
				for(int i = 0; i < BlueprintShelfBlock.BLUEPRINT_SLOT_FILLED.length; i++)
					multipartBuilder.part()
							.modelFile(models().getExistingFile(modLoc("block/blueprint_shelf/blueprint_"+i)))
							.rotationX(rotX)
							.rotationY(rotY)
							.addModel()
							.condition(IEProperties.FACING_ALL, d)
							.condition(BlueprintShelfBlock.BLUEPRINT_SLOT_FILLED[i], true)
							.end();
			}
			itemModel(WoodenDevices.BLUEPRINT_SHELF, models().getExistingFile(modLoc("block/blueprint_shelf/frame")));
		}
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
				ieObjBuilder("block/wooden_device/logic_unit.obj.ie")
						.callback(LogicUnitCallbacks.INSTANCE)
						.layer(solid(), translucent())
						.end()
		);
		{
			ModelFile machineInterfaceModel = models().cubeBottomTop("machine_interface",
					modLoc("block/wooden_device/machine_interface"),
					modLoc("block/wooden_device/machine_interface_back"),
					modLoc("block/wooden_device/machine_interface_front")
			);
			createRotatedBlock(WoodenDevices.MACHINE_INTERFACE, machineInterfaceModel, IEProperties.FACING_HORIZONTAL, ImmutableList.of(), -90, 0);
			itemModel(WoodenDevices.MACHINE_INTERFACE, machineInterfaceModel);
		}

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
					.end()
					.renderType(ModelProviderUtils.getName(cutout()));
			simpleBlockAndItem(MetalDevices.FLUID_PLACER, model);
		}
		{
			BlockModelBuilder furnaceHeaterOn = models().withExistingParent("furnace_heater_on", rl("block/ie_six_sides_overlay_all_but_one"))
					.texture("block_all", rl("block/metal_device/furnace_heater_active"))
					.texture("block_north", rl("block/metal_device/furnace_heater_socket"))
					.texture("overlay_all", rl("block/metal_device/furnace_heater_active_overlay"));
			BlockModelBuilder furnaceHeaterOff = models().withExistingParent("furnace_heater_off", rl("block/ie_six_sides_overlay_all_but_one"))
					.texture("block_all", rl("block/metal_device/furnace_heater"))
					.texture("block_north", rl("block/metal_device/furnace_heater_socket"))
					.texture("overlay_all", rl("block/metal_device/furnace_heater_overlay"));
			setRenderType(RenderType.cutout(), furnaceHeaterOn, furnaceHeaterOff);
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
					solid(), modLoc("block/metal_device/charging_station.obj"),
					translucent(), modLoc("block/metal_device/charging_station_glass.obj")
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
		{
			ModelFile magnetModel = models().cubeBottomTop("electromagnet",
					modLoc("block/metal_device/electromagnet"),
					modLoc("block/metal_device/electromagnet_bottom"),
					modLoc("block/metal_device/electromagnet_top")
			);
			createRotatedBlock(MetalDevices.ELECTROMAGNET, magnetModel, IEProperties.FACING_ALL, ImmutableList.of(), -90, 0);
			itemModel(MetalDevices.ELECTROMAGNET, magnetModel);
		}

		createHemp();
		{
			final var pottedHempModel = models()
					.withExistingParent("potted_hemp", mcLoc("block/flower_pot_cross"))
					.texture("plant", ieLoc("block/hemp/potted"))
					.renderType(ModelProviderUtils.getName(cutout()));
			simpleBlock(Misc.POTTED_HEMP.get(), pottedHempModel);
		}
		createSawdust();

		for(IEFluids.FluidEntry entry : IEFluids.ALL_ENTRIES)
		{
			Fluid still = entry.getStill();
			ModelFile model = models().getBuilder("block/fluid/"+BuiltInRegistries.FLUID.getKey(still).getPath())
					.texture("particle", entry.stillTexture());
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
				.layer(cutout())
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
				models().withExistingParent(BuiltInRegistries.BLOCK.getKey(b.get()).getPath()+"_inventory", mcLoc("block/fence_inventory"))
						.texture("texture", texture));
	}

	public void fenceGateBlock(Supplier<? extends FenceGateBlock> b, ResourceLocation texture)
	{
		super.fenceGateBlock(b.get(), texture);
		itemModel(b, models().getExistingFile(rl("block/"+BuiltInRegistries.BLOCK.getKey(b.get()).getPath())));
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

	public ModelFile createMetalLadder(String name, @Nullable ResourceLocation bottomTop, @Nullable ResourceLocation sides, @Nullable ResourceLocation front)
	{
		Map<String, ResourceLocation> textures = new HashMap<>();
		ResourceLocation parent;
		if(bottomTop!=null)
		{
			Preconditions.checkNotNull(sides);
			textures.put("top", bottomTop);
			textures.put("bottom", bottomTop);
			textures.put("side", sides);
			if(front!=null)
			{
				parent = ieLoc("block/ie_scaffoldladder_open");
				textures.put("front", front);
			}
			else
				parent = ieLoc("block/ie_scaffoldladder");
		}
		else
			parent = ieLoc("block/ie_ladder");
		textures.put("ladder", rl("block/metal_decoration/metal_ladder"));
		BlockModelBuilder ret = models().withExistingParent(name, parent);
		for(Entry<String, ResourceLocation> e : textures.entrySet())
			ret.texture(e.getKey(), e.getValue());
		ret.renderType(ModelProviderUtils.getName(cutout()));
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

	private void createLadderBlock(Supplier<? extends Block> b, ModelFile model, ModelFile modelOpen)
	{
		VariantBlockStateBuilder builder = getVariantBuilder(b.get());
		for(Direction d : DirectionUtils.BY_HORIZONTAL_INDEX)
		{
			builder.partialState()
					.with(LadderBlock.FACING, d)
					.with(MetalLadderBlock.OPEN, false)
					.setModels(new ConfiguredModel(model, 0, getAngle(d, 180), true));
			builder.partialState()
					.with(LadderBlock.FACING, d)
					.with(MetalLadderBlock.OPEN, true)
					.setModels(new ConfiguredModel(modelOpen, 0, getAngle(d, 180), false));
		}
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
				ModelFile model = obj(BuiltInRegistries.BLOCK.getKey(b.get()).getPath()+or.modelSuffix(), modelLoc,
						ImmutableMap.of("texture", texture), models());
				stateBuilder.partialState()
						.with(IEProperties.FACING_HORIZONTAL, d)
						.with(WallmountBlock.ORIENTATION, or)
						.setModels(new ConfiguredModel(model, 0, rotation, true));
			}
		}
	}

	private void createCatwalk(Supplier<? extends Block> block, String textureTop, String textureSide)
	{
		// prep textured elements
		String name = BuiltInRegistries.BLOCK.getKey(block.get()).getPath();
		BlockModelBuilder base = models()
				.withExistingParent(name+"_base", modLoc("block/catwalk_base"))
				.texture("top", textureTop)
				.texture("side", textureSide);
		BlockModelBuilder railing = models()
				.withExistingParent(name+"_railing", modLoc("block/catwalk_railing"))
				.texture("top", textureTop)
				.texture("side", textureSide);
		// assemble multipart
		MultiPartBlockStateBuilder multipartBuilder = getMultipartBuilder(block.get());
		multipartBuilder.part().modelFile(base).addModel().end();
		CatwalkBlock.RAILING_PROPERTIES.forEach((direction, booleanProperty) ->
				multipartBuilder.part().modelFile(railing).rotationY(getAngle(direction, 180))
						.addModel().condition(booleanProperty, true).end());
		// assemble item model
		itemModel(block, models().withExistingParent(name+"_item", "block/block")
				.customLoader(CompositeModelBuilder::begin)
				.child("base", base)
				.child("railing", railing)
				.end());
	}

	private void createCatwalkStairs(Supplier<? extends Block> block, String textureTop, String textureSide)
	{
		// prep textured elements
		String name = BuiltInRegistries.BLOCK.getKey(block.get()).getPath();
		ModelFile base = models().withExistingParent(name+"_base", modLoc("block/catwalk_stairs"))
				.texture("top", textureTop)
				.texture("side", textureSide);
		ModelFile railing_r = models().withExistingParent(name+"_railing_r", modLoc("block/catwalk_stairs_railing_r"))
				.texture("top", textureTop)
				.texture("side", textureSide);
		ModelFile railing_l = models().withExistingParent(name+"_railing_l", modLoc("block/catwalk_stairs_railing_l"))
				.texture("top", textureTop)
				.texture("side", textureSide);
		// create blockstate
		MultiPartBlockStateBuilder multipartBuilder = getMultipartBuilder(block.get());
		for(Direction direction : IEProperties.FACING_HORIZONTAL.getPossibleValues())
		{
			multipartBuilder.part().modelFile(base)
					.rotationY(getAngle(direction, 180))
					.addModel()
					.condition(IEProperties.FACING_HORIZONTAL, direction)
					.end();
			multipartBuilder.part().modelFile(railing_r)
					.rotationY(getAngle(direction, 180))
					.addModel()
					.condition(IEProperties.FACING_HORIZONTAL, direction)
					.condition(CatwalkStairsBlock.RAILING_RIGHT, true)
					.end();
			multipartBuilder.part().modelFile(railing_l)
					.rotationY(getAngle(direction, 180))
					.addModel()
					.condition(IEProperties.FACING_HORIZONTAL, direction)
					.condition(CatwalkStairsBlock.RAILING_LEFT, true)
					.end();
		}
		itemModel(block, base);
	}

	protected ModelFile createMultiLayer(String path, Map<RenderType, ResourceLocation> modelGetter, ResourceLocation particle)
	{
		CompositeModelBuilder<BlockModelBuilder> modelBuilder = models().getBuilder(path)
				.customLoader(CompositeModelBuilder::begin);

		for(Entry<RenderType, ResourceLocation> entry : modelGetter.entrySet())
		{
			ResourceLocation rl = entry.getValue();
			String layer = ModelProviderUtils.getName(entry.getKey());
			modelBuilder.child(
					layer,
					obj(new BlockModelBuilder(rl("temp"), existingFileHelper), rl, ImmutableMap.of()).renderType(layer)
			);
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


	private void createDoor(Supplier<? extends DoorBlock> block, String texture)
	{
		doorBlockWithRenderType(block.get(), rl(texture+"_bottom"), rl(texture+"_top"), "cutout");
	}

	private void createTrapdoor(Supplier<? extends TrapDoorBlock> block, String texture)
	{
		trapdoorBlockWithRenderType(block.get(), rl(texture), true, "cutout");
		itemModel(block, models().getExistingFile(rl(BuiltInRegistries.BLOCK.getKey(block.get()).getPath()+"_bottom")));
	}

	private void createSigns(IEBlocks.SignHolder holder, String particle)
	{
		signBlock(holder.sign().get(), holder.wall().get(), rl(particle));
		hangingSignBlock(holder.hanging().get(), holder.wallHanging().get(), rl(particle));
	}

	private void createHemp()
	{
		VariantBlockStateBuilder builder = getVariantBuilder(Misc.HEMP_PLANT.get());
		// Top
		ModelFile model = models()
				.withExistingParent("block/hemp/top", ResourceLocation.withDefaultNamespace("block/crop"))
				.texture("crop", ieLoc("block/hemp/top0"))
				.renderType(ModelProviderUtils.getName(cutout()));
		builder.partialState().with(HempBlock.HALF, DoubleBlockHalf.UPPER).setModels(new ConfiguredModel(model));

		// Bottoms
		for(int i = 0; i <= 4; i++)
		{
			model = models()
					.withExistingParent("block/hemp/bottom"+i, ResourceLocation.withDefaultNamespace("block/crop"))
					.texture("crop", ieLoc("block/hemp/bottom"+i))
					.renderType(ModelProviderUtils.getName(cutout()));
			builder.partialState().with(HempBlock.HALF, DoubleBlockHalf.LOWER).with(HempBlock.AGE, i).setModels(new ConfiguredModel(model));
		}
	}

	private void createSawdust()
	{
		VariantBlockStateBuilder builder = getVariantBuilder(WoodenDecoration.SAWDUST.get());
		ResourceLocation sawdustTexture = ieLoc("block/wooden_decoration/sawdust");
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
				model = models().withExistingParent(name, ResourceLocation.withDefaultNamespace("block/thin_block"))
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
			builder.texture(d.getSerializedName(), baseTexName.withSuffix("_"+d.ordinal()));
		builder.texture("particle", baseTexName.withSuffix("_0"));
		return builder;
	}
}
