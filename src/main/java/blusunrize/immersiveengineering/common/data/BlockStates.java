/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainBlock;
import blusunrize.immersiveengineering.common.blocks.generic.IEFenceBlock;
import blusunrize.immersiveengineering.common.blocks.generic.PostBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.data.Models.MetalModels;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.IVariantModelGenerator;
import blusunrize.immersiveengineering.common.data.blockstate.BlockstateGenerator.MultiPart;
import blusunrize.immersiveengineering.common.data.blockstate.VariantBlockstate.Builder;
import blusunrize.immersiveengineering.common.data.loadermodels.LoadedModelBuilder;
import blusunrize.immersiveengineering.common.data.model.ModelFile.ExistingModelFileIE;
import blusunrize.immersiveengineering.common.data.model.ModelFile.UncheckedModelFile;
import blusunrize.immersiveengineering.common.data.model.ModelHelper;
import blusunrize.immersiveengineering.common.data.model.ModelHelper.BasicStairsShape;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.data.DataGenerator;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.Half;
import net.minecraft.state.properties.SlabType;
import net.minecraft.state.properties.StairsShape;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.common.data.IEDataGenerator.rl;

public class BlockStates extends BlockStateProvider
{
	private static final ResourceLocation ALU_FENCE_TEXTURE = rl("block/metal/storage_aluminum");
	private static final ResourceLocation STEEL_FENCE_TEXTURE = rl("block/metal/storage_steel");
	private static final ResourceLocation TREATED_FENCE_TEXTURE = rl("block/wooden_decoration/treated_wood_horizontal");
	private final ConfiguredModel EMPTY_MODEL = new ConfiguredModel(
			new ExistingModelFile(modLoc("block/ie_empty"), existingFileHelper)
	);
	private final LoadedModels loadedModels;

	public BlockStates(DataGenerator gen, ExistingFileHelper exHelper, LoadedModels loaded)
	{
		super(gen, MODID, exHelper);
		loadedModels = loaded;
	}

	private String name(Block b)
	{
		return b.getRegistryName().getPath();
	}

	private void cubeSideVertical(Block b, ResourceLocation side, ResourceLocation vertical)
	{
		simpleBlock(b, cubeBottomTop(name(b), side, vertical, vertical));
	}

	private void cubeAll(Block b, ResourceLocation texture)
	{
		simpleBlock(b, cubeAll(name(b), texture));
	}

	private void scaffold(Block b, ResourceLocation others, ResourceLocation top)
	{
		simpleBlock(
				b,
				withExistingParent(name(b), modLoc("block/ie_scaffolding"))
						.texture("side", others)
						.texture("bottom", others)
						.texture("top", top)
		);
	}

	private void slabFor(Block b, ResourceLocation texture) {
		slabFor(b, texture, texture, texture);
	}

	private void slabFor(Block b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom) {
		slab(IEBlocks.toSlab.get(b), side, top, bottom);
	}

	private void slab(SlabBlock b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom) {
		slabBlock(
				b,
				cubeBottomTop(name(b)+"_double", side, bottom, top),
				slabTop(name(b)+"_top", side, bottom, top),
				slab(name(b)+"_bottom", side, bottom, top)
		);
	}

	private void stairs(StairsBlock b, ResourceLocation texture) {
		stairs(b, texture, texture, texture);
	}

	private void stairs(StairsBlock b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom) {
		stairsBlock(b, name(b), side, bottom, top);
	}

	private ResourceLocation forgeLoc(String path) {
		return new ResourceLocation("forge", path);
	}

	private ResourceLocation addModelsPrefix(ResourceLocation in) {
		return new ResourceLocation(in.getNamespace(), "models/"+in.getPath());
	}

	private void postBlock(Block b, ResourceLocation texture) {
		ResourceLocation model = rl("block/wooden_device/wooden_post.obj.ie");
		ModelHelper.assertModelExists(model);
		LoadedModelBuilder modelFile = loadedModels.withExistingParent(name(b), mcLoc("block"))
				.loader(modLoc("ie_obj"))
				.additional("model", addModelsPrefix(model))
				.additional("flip-v", true)
				.texture("texture", texture);
		getVariantBuilder(b)
				.partialState()
				.with(PostBlock.POST_SLAVE, 0)
				.setModels(new ConfiguredModel(modelFile));
		for (int i = 1;i<=3;++i)
			getVariantBuilder(b)
					.partialState()
					.with(PostBlock.POST_SLAVE, i)
					.setModels(EMPTY_MODEL);
	}

	private ModelFile obj(String loc) {
		Preconditions.checkArgument(loc.endsWith(".obj"));
		return obj(loc.substring(0, loc.length()-4), modLoc(loc));
	}
		private ModelFile obj(String name, ResourceLocation model) {
		return loadedModels.withExistingParent(name, mcLoc("block"))
				.loader(forgeLoc("obj"))
				.additional("model", addModelsPrefix(model))
				.additional("flip-v", true);
	}

	@Override
	protected void registerStatesAndModels()
	{
		BiConsumer<Block, IVariantModelGenerator> variantBased = null;
		BiConsumer<Block, List<MultiPart>> multipartBased = null;
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
					storageModel = cubeBottomTop(storageName, side, top, top);
					slabFor(storage, side, top, top);
				}
				else
				{
					storageModel = cubeAll(storageName, defaultStorageTexture);
					slabFor(storage, defaultStorageTexture);
				}
				simpleBlock(storage, storageModel);
			}
			ResourceLocation sheetmetalName = modLoc("block/metal/sheetmetal_"+name);
			cubeAll(Metals.sheetmetal.get(m), sheetmetalName);
			slabFor(Metals.sheetmetal.get(m), sheetmetalName);
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
		createMultiblock(Multiblocks.excavator, obj("block/metal_multiblock/excavator.obj"),
				obj("block/metal_multiblock/excavator_mirrored.obj"));
		createMultiblock(Multiblocks.crusher, obj("block/metal_multiblock/crusher_mirrored.obj"),
				obj("block/metal_multiblock/crusher.obj"));
		createMultiblock(Multiblocks.metalPress, obj("block/metal_multiblock/metal_press.obj"));
		createMultiblock(Multiblocks.assembler, obj("block/metal_multiblock/assembler.obj"));
		/*
		{
			IVariantModelGenerator gen = new Builder(Multiblocks.bucketWheel)
					.setForAllWithState(ImmutableMap.of(), EMPTY_MODEL)
					.build();
			variantBased.accept(Multiblocks.bucketWheel, gen);
		}
		createMultiblock(Multiblocks.arcFurnace, new ExistingModelFileIE(rl("block/metal_multiblock/arc_furnace.obj")),
				new ExistingModelFileIE(rl("block/metal_multiblock/arc_furnace_mirrored.obj")), variantBased);

		createMultiblock(Multiblocks.blastFurnaceAdv, new ExistingModelFileIE(rl("block/blastfurnace_advanced.obj")), variantBased);
		createMultiblock(Multiblocks.cokeOven, models.cokeOvenOff, models.cokeOvenOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
		createMultiblock(Multiblocks.alloySmelter, models.alloySmelterOff, models.alloySmelterOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
		createMultiblock(Multiblocks.blastFurnace, models.blastFurnaceOff, models.blastFurnaceOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180, variantBased);
		createMultiblock(Multiblocks.silo, new ExistingModelFileIE(rl("block/metal_multiblock/silo.obj")), variantBased);
		createMultiblock(Multiblocks.tank, new ExistingModelFileIE(rl("block/metal_multiblock/tank.obj")), variantBased);
		createMultiblock(Multiblocks.bottlingMachine,
				new ExistingModelFileIE(rl("block/metal_multiblock/bottling_machine.obj")),
				new ExistingModelFileIE(rl("block/metal_multiblock/bottling_machine_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.fermenter,
				new ExistingModelFileIE(rl("block/metal_multiblock/fermenter.obj")),
				new ExistingModelFileIE(rl("block/metal_multiblock/fermenter_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.squeezer,
				new ExistingModelFileIE(rl("block/metal_multiblock/squeezer.obj")),
				new ExistingModelFileIE(rl("block/metal_multiblock/squeezer_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.mixer,
				new ExistingModelFileIE(rl("block/metal_multiblock/mixer.obj")),
				new ExistingModelFileIE(rl("block/metal_multiblock/mixer_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.refinery,
				new ExistingModelFileIE(rl("block/metal_multiblock/refinery.obj")),
				new ExistingModelFileIE(rl("block/metal_multiblock/refinery_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.dieselGenerator,
				new ExistingModelFileIE(rl("block/metal_multiblock/diesel_generator.obj")),
				new ExistingModelFileIE(rl("block/metal_multiblock/diesel_generator_mirrored.obj")), variantBased);
		createMultiblock(Multiblocks.lightningrod,
				new ExistingModelFileIE(rl("block/metal_multiblock/lightningrod.obj")), variantBased);
		createMultiblock(WoodenDevices.workbench, new ExistingModelFileIE(rl("block/wooden_device/workbench.obj.ie")),
				null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180,
				variantBased);
		createMultiblock(MetalDevices.sampleDrill, new ExistingModelFileIE(rl("block/metal_device/core_drill.obj")),
				null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180,
				variantBased);
		createBasicBlock(MetalDevices.fluidPipe, new ExistingModelFileIE(rl("block/metal_device/fluid_pipe.obj.ie")),
				variantBased);
		createConnector(
				MetalDevices.floodlight,
				rl("block/metal_device/floodlight.obj.ie"),
				ImmutableMap.of(),
				variantBased,
				BlockRenderLayer.TRANSLUCENT, BlockRenderLayer.SOLID
		);
		createMultiblock(
				MetalDevices.belljar,
				new ExistingModelFileIE(rl("block/metal_device/belljar.obj.ie")),
				variantBased
		);
		createMultiblock(
				MetalDevices.turretChem,
				new ExistingModelFileIE(rl("block/metal_device/chem_turret.obj.ie")),
				variantBased
		);
		createMultiblock(
				MetalDevices.turretGun,
				new ExistingModelFileIE(rl("block/metal_device/gun_turret.obj.ie")),
				variantBased
		);
		createMultiblock(MetalDevices.teslaCoil, new ExistingModelFileIE(rl("block/metal_device/teslacoil.obj")),
				null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_ALL, null,
				180, variantBased);
		createBasicBlock(Misc.fakeLight, EMPTY_MODEL, variantBased);

		createPostBlock(MetalDecoration.aluPost, new ExistingModelFileIE(rl("block/wooden_device/wooden_post.obj.ie")),
				rl("block/metal_decoration/aluminum_post"),
				variantBased);
		createPostBlock(MetalDecoration.steelPost, new ExistingModelFileIE(rl("block/wooden_device/wooden_post.obj.ie")),
				rl("block/metal_decoration/steel_post"),
				variantBased);
		createPostBlock(WoodenDecoration.treatedPost, new ExistingModelFileIE(rl("block/wooden_device/wooden_post.obj.ie")),
				rl("block/wooden_decoration/post"),
				variantBased);
		createMultistateSingleModel(WoodenDevices.windmill, EMPTY_MODEL, variantBased);
		createMultistateSingleModel(WoodenDevices.watermill, EMPTY_MODEL, variantBased);
		createMultistateSingleModel(MetalDecoration.lantern,
				new ConfiguredModel(new ExistingModelFileIE(rl("block/lantern.obj.ie"))),
				variantBased);

		createDirectionalBlock(MetalDecoration.metalLadder.get(CoverType.NONE), IEProperties.FACING_HORIZONTAL,
				models.metalLadderNone, variantBased);
		createDirectionalBlock(MetalDecoration.metalLadder.get(CoverType.ALU), IEProperties.FACING_HORIZONTAL,
				models.metalLadderAlu, variantBased);
		createDirectionalBlock(MetalDecoration.metalLadder.get(CoverType.STEEL), IEProperties.FACING_HORIZONTAL,
				models.metalLadderSteel, variantBased);

		createWallmount(WoodenDevices.treatedWallmount, rl("block/wooden_device/wallmount"), variantBased);
		createWallmount(MetalDecoration.aluWallmount, rl("block/metal_decoration/aluminum_wallmount"), variantBased);
		createWallmount(MetalDecoration.steelWallmount, rl("block/metal_decoration/steel_wallmount"), variantBased);
		createMultistateSingleModel(
				MetalDecoration.slopeSteel,
				new ConfiguredModel(new ExistingModelFileIE(rl("block/slope.obj.ie"))),
				variantBased
		);
		createMultistateSingleModel(
				MetalDecoration.slopeAlu,
				new ConfiguredModel(
						new ExistingModelFileIE(rl("block/slope.obj.ie")),
						0,
						0,
						true,
						ImmutableMap.of(),
						ImmutableMap.of(
								MODID+":block/metal_decoration/steel_scaffolding",
								MODID+":block/metal_decoration/aluminum_scaffolding"
						)
				),
				variantBased
		);

		createConnector(Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), rl("block/connector/connector_lv.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), rl("block/connector/connector_lv.obj"),
				ImmutableMap.of("#immersiveengineering:block/connector/connector_lv", "immersiveengineering:block/connector/relay_lv"),
				variantBased, BlockRenderLayer.SOLID);

		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), rl("block/connector/connector_mv.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("#immersiveengineering:block/connector/connector_mv", "immersiveengineering:block/connector/relay_mv"),
				variantBased, BlockRenderLayer.SOLID);

		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), rl("block/connector/connector_hv.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), rl("block/connector/relay_hv.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.TRANSLUCENT);

		createConnector(Connectors.connectorStructural, rl("block/connector/connector_structural.obj.ie"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.connectorRedstone, rl("block/connector/connector_redstone.obj.ie"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.connectorProbe, rl("block/connector/connector_probe.obj.ie"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.CUTOUT, BlockRenderLayer.TRANSLUCENT);
		createConnector(Connectors.feedthrough, rl("feedthrough"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(MetalDevices.electricLantern, state -> rl("block/metal_device/e_lantern.obj"),
				state -> {
					if(state.get(IEProperties.ACTIVE)==Boolean.FALSE)
						return ImmutableMap.of();
					else
						return ImmutableMap.of(
								"#"+MODID+":block/metal_device/electric_lantern", MODID+":block/metal_device/electric_lantern_on"
						);
				},
				variantBased, ImmutableList.of(IEProperties.ACTIVE));

		createConnector(Connectors.redstoneBreaker, rl("block/connector/redstone_breaker.obj.ie"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.breakerswitch, map -> {
			if(map.get(IEProperties.ACTIVE)==Boolean.FALSE)
				return rl("block/connector/breaker_switch_off.obj.ie");
			else
				return rl("block/connector/breaker_switch_on.obj.ie");
		}, ImmutableMap.of(), variantBased, ImmutableList.of(IEProperties.ACTIVE), BlockRenderLayer.SOLID);
		createConnector(Connectors.transformer, map -> {
			if(map.get(IEProperties.MULTIBLOCKSLAVE)==Boolean.TRUE)
				return EMPTY_MODEL.name.getLocation();
			else if(map.get(IEProperties.MIRRORED)==Boolean.FALSE)
				return rl("block/connector/transformer_mv_left.obj");
			else
				return rl("block/connector/transformer_mv_right.obj");
		}, ImmutableMap.of(), variantBased, ImmutableList.of(
				IEProperties.MULTIBLOCKSLAVE,
				IEProperties.MIRRORED
		), BlockRenderLayer.SOLID);
		createConnector(Connectors.postTransformer, rl("block/connector/transformer_post.obj"),
				ImmutableMap.of(), variantBased, BlockRenderLayer.SOLID);
		createConnector(Connectors.transformerHV, map -> {
			if(map.get(IEProperties.MULTIBLOCKSLAVE)==Boolean.TRUE)
				return EMPTY_MODEL.name.getLocation();
			else if(map.get(IEProperties.MIRRORED)==Boolean.FALSE)
				return rl("block/connector/transformer_hv_left.obj");
			else
				return rl("block/connector/transformer_hv_right.obj");
		}, ImmutableMap.of(), variantBased, ImmutableList.of(
				IEProperties.MULTIBLOCKSLAVE,
				IEProperties.MIRRORED
		), BlockRenderLayer.SOLID);

		createConnector(Connectors.currentTransformer, map -> {
			if(map.get(IEProperties.MULTIBLOCKSLAVE)==Boolean.TRUE)
				return rl("block/connector/e_meter.obj");
			else
				return EMPTY_MODEL.name.getLocation();
		}, ImmutableMap.of(), variantBased, ImmutableList.of(IEProperties.MULTIBLOCKSLAVE), BlockRenderLayer.SOLID);
		createConnector(MetalDevices.razorWire, rl("block/razor_wire.obj.ie"), ImmutableMap.of(), variantBased);

		createRotatedBlock(StoneDecoration.coresample, map -> new ExistingModelFileIE(rl("block/coresample.obj")),
				IEProperties.FACING_HORIZONTAL, ImmutableList.of(), ImmutableMap.of(), variantBased);
		createBasicBlock(StoneDecoration.concreteSheet, models.sheetConcreteBlock, variantBased);
		createBasicBlock(StoneDecoration.concreteQuarter, models.quarterConcreteBlock, variantBased);
		createBasicBlock(StoneDecoration.concreteThreeQuarter, models.threeQuarterConcreteBlock, variantBased);
		createBasicBlock(StoneDecoration.concreteSprayed, new ExistingModelFileIE(rl("block/sprayed_concrete.obj")),
				variantBased);

		createBasicBlock(WoodenDevices.crate, models.crate, variantBased);
		createBasicBlock(WoodenDevices.reinforcedCrate, models.reinforcedCrate, variantBased);
		createMultistateSingleModel(WoodenDevices.gunpowderBarrel, new ConfiguredModel(models.gunpowderBarrel),
				variantBased);
		createBasicBlock(WoodenDevices.sorter, models.router, variantBased);
		createBasicBlock(WoodenDevices.fluidSorter, models.fluidRouter, variantBased);
		createBasicBlock(WoodenDevices.woodenBarrel,
				new UncheckedModelFile(rl("smartmodel/conf_sides_v_wooden_device/barrel")), variantBased);

		createConnector(Cloth.balloon, map -> rl("block/balloon.obj.ie"), ImmutableMap.of(), variantBased,
				ImmutableList.of(), BlockRenderLayer.SOLID);
		createRotatedBlock(Cloth.curtain,
				state -> new ExistingModelFileIE(rl(
						state.get(StripCurtainBlock.CEILING_ATTACHED)==Boolean.FALSE?
								"block/stripcurtain":
								"block/stripcurtain_middle"
				)), IEProperties.FACING_HORIZONTAL, ImmutableList.of(StripCurtainBlock.CEILING_ATTACHED),
				ImmutableMap.of(), variantBased);
		createBasicBlock(Cloth.cushion, models.cushion, variantBased);
		createMultistateSingleModel(Cloth.shaderBanner, EMPTY_MODEL, variantBased);

		createBasicBlock(MetalDevices.barrel,
				new UncheckedModelFile(rl("smartmodel/conf_sides_v_metal_device/barrel")), variantBased);
		for(Entry<Block, String> cap : ImmutableMap.of(
				MetalDevices.capacitorCreative, "creative",
				MetalDevices.capacitorLV, "lv",
				MetalDevices.capacitorMV, "mv",
				MetalDevices.capacitorHV, "hv"
		).entrySet())
			createBasicBlock(cap.getKey(),
					new UncheckedModelFile(rl("smartmodel/conf_sides_hud_metal_device/capacitor_"+cap.getValue())),
					variantBased);
		createMultiblock(MetalDevices.blastFurnacePreheater,
				new ExistingModelFileIE(rl("block/metal_device/blastfurnace_preheater.obj")),
				variantBased);
		createRotatedBlock(MetalDevices.furnaceHeater, props -> {
					if(props.get(IEProperties.ACTIVE)==Boolean.TRUE)
						return models.furnaceHeaterOn;
					else
						return models.furnaceHeaterOff;
				}, IEProperties.FACING_ALL, ImmutableList.of(IEProperties.ACTIVE),
				ImmutableMap.of(), variantBased);
		createPump(variantBased);
		createRotatedBlock(MetalDevices.dynamo, state -> models.kineticDynamo, IEProperties.FACING_HORIZONTAL,
				ImmutableList.of(), ImmutableMap.of(), variantBased);
		createBasicBlock(MetalDevices.thermoelectricGen, models.thermoelectricGen, variantBased);
		{
			ModelFile solid = new ExistingModelFileIE(rl("block/metal_device/charging_station.obj"));
			ModelFile translucent = new ExistingModelFileIE(rl("block/metal_device/charging_station_glass.obj"));
			ImmutableMap.Builder<String, Object> additional = ImmutableMap.builder();
			additional.put(BlockRenderLayer.SOLID.name(), ImmutableMap.of("model", solid.getLocation()));
			additional.put(BlockRenderLayer.TRANSLUCENT.name(), ImmutableMap.of("model", translucent.getLocation()));
			createRotatedBlock(MetalDevices.chargingStation,
					state -> new UncheckedModelFile(rl("multilayer")),
					IEProperties.FACING_HORIZONTAL,
					ImmutableList.of(),
					additional.build(),
					variantBased
			);
		}
		for(Block b : MetalDevices.CONVEYORS.values())
			createMultistateSingleModel(b, new ConfiguredModel(new UncheckedModelFile(rl("conveyor"))), variantBased);
		createHemp(variantBased);
		for(Block b : models.fluidModels.keySet())
			createMultistateSingleModel(b, new ConfiguredModel(models.fluidModels.get(b)), variantBased);
		createRotatedBlock(MetalDevices.toolbox, state -> new ExistingModelFileIE(rl("block/toolbox.obj")),
				IEProperties.FACING_HORIZONTAL, ImmutableList.of(),
				ImmutableMap.of(), variantBased);
		 */
		//TODO remove, this is a workaround for the broken missing model
		for (Block b : IEContent.registeredIEBlocks) {
			if (!registeredBlocks.containsKey(b)) {
				cubeAll(b, rl("block/metal_decoration/radiator"));
			}
		}
		loadedModels.backupModels();
	}
/*
	private void createPump(BiConsumer<Block, IVariantModelGenerator> variantBased)
	{
		Builder builder = new Builder(MetalDevices.fluidPump);
		builder.setForAllWithState(ImmutableMap.of(IEProperties.MULTIBLOCKSLAVE, true),
				new ConfiguredModel(new ExistingModelFileIE(rl("block/metal_device/fluid_pump.obj")),
						0, 0, false, ImmutableMap.of("flip-v", true)));
		builder.setForAllWithState(ImmutableMap.of(IEProperties.MULTIBLOCKSLAVE, false),
				new ConfiguredModel(new UncheckedModelFile(rl("smartmodel/conf_sides_hv_metal_device/fluid_pump"))));
		variantBased.accept(MetalDevices.fluidPump, builder.build());
	}

	private void createRotatedBlock(Block block, Function<Map<IProperty<?>, Object>, ModelFile> model, IProperty<Direction> facing,
									List<IProperty<?>> additionalProps, ImmutableMap<String, Object> additional,
									BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder builder = new Builder(block);
		forEachState(additionalProps, state -> {
			ImmutableMap.Builder<String, Object> additionalForState = ImmutableMap.builder();
			ModelFile modelLoc = model.apply(state);
			if(modelLoc.getLocation().getPath().contains(".obj"))
				additionalForState.put("flip-v", true);
			additionalForState.putAll(additional);
			Map<IProperty<?>, Object> baseState = new HashMap<>();
			for(Entry<IProperty<?>, Object> e : state.entrySet())
				baseState.put(e.getKey(), e.getValue());
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
						y = getAngle(d, 180);
						x = 0;
				}
				ConfiguredModel configuredModel = new ConfiguredModel(modelLoc, x, y, true,
						additionalForState.build());

				builder.setForAllWithState(with(baseState, facing, d), configuredModel);
			}
		});
		out.accept(block, builder.build());
	}

	private void createBasicBlock(Block block, ModelFile model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		ConfiguredModel configuredModel = new ConfiguredModel(model);
		createBasicBlock(block, configuredModel, out);
	}

	private void createBasicBlock(Block block, ConfiguredModel model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		IVariantModelGenerator gen = new Builder(block)
				.setModel(block.getDefaultState(), model)
				.build();
		out.accept(block, gen);
	}

	private void createMultistateSingleModel(Block block, ConfiguredModel model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		IVariantModelGenerator gen = new Builder(block)
				.setForAllMatching(state -> true, model)
				.build();
		out.accept(block, gen);
	}

	private void createSlabBlock(Block block, Map<SlabType, ModelFile> baseModels, EnumProperty<SlabType> typeProp, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder b = new Builder(block);
		for(SlabType type : SlabType.values())
		{
			Map<IProperty<?>, Object> partialState = ImmutableMap.<IProperty<?>, Object>builder()
					.put(typeProp, type)
					.build();
			b.setForAllWithState(partialState, new ConfiguredModel(baseModels.get(type)));
		}
		out.accept(block, b.build());
	}

	private void createStairsBlock(Block block, Map<BasicStairsShape, ModelFile> baseModels, EnumProperty<Direction> facingProp,
								   EnumProperty<Half> halfProp, EnumProperty<StairsShape> shapeProp, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder b = new Builder(block);
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			for(Half half : Half.values())
			{
				for(StairsShape shape : StairsShape.values())
				{
					Map<IProperty<?>, Object> partialState = ImmutableMap.<IProperty<?>, Object>builder()
							.put(facingProp, dir)
							.put(halfProp, half)
							.put(shapeProp, shape)
							.build();
					ModelFile base = baseModels.get(BasicStairsShape.toBasicShape(shape));
					int xRot = 0;
					if(half==Half.TOP)
						xRot = 180;
					int yRot = getAngle(dir, 90);
					if(shape==StairsShape.INNER_LEFT||shape==StairsShape.OUTER_LEFT)
						yRot = (yRot+270)%360;
					b.setForAllWithState(partialState, new ConfiguredModel(base, xRot, yRot, true, ImmutableMap.of()));
				}
			}
		}
		out.accept(block, b.build());
	}

	private void createFenceBlock(IEFenceBlock block, ModelFile post, ModelFile side, BiConsumer<Block, List<MultiPart>> out)
	{
		List<MultiPart> parts = new ArrayList<>();
		ConfiguredModel postModel = new ConfiguredModel(post, 0, 0, false, ImmutableMap.of());
		parts.add(new MultiPart(postModel, false));
		for(Direction dir : Direction.BY_HORIZONTAL_INDEX)
		{
			int angle = getAngle(dir, 180);
			ConfiguredModel sideModel = new ConfiguredModel(side, 0, angle, true, ImmutableMap.of());
			BooleanProperty sideActive = block.getFacingStateMap().get(dir);
			parts.add(new MultiPart(sideModel, false, new PropertyWithValues<>(sideActive, true)));
		}
		out.accept(block, parts);
	}
	*/

	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel, int rotationOffset)
	{
		createMultiblock(b, masterModel, mirroredModel, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, IEProperties.MIRRORED, rotationOffset);
	}

	private void createMultiblock(Block b, ModelFile masterModel, ModelFile mirroredModel)
	{
		createMultiblock(b, masterModel, mirroredModel, 180);
	}

	private void createMultiblock(Block b, ModelFile masterModel)
	{
		createMultiblock(b, masterModel, null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180);
	}

	private void createMultiblock(Block b, ModelFile masterModel, @Nullable ModelFile mirroredModel, IProperty<Boolean> isSlave,
								  EnumProperty<Direction> facing, @Nullable IProperty<Boolean> mirroredState, int rotationOffset)
	{
		Preconditions.checkArgument((mirroredModel==null)==(mirroredState==null));
		VariantBlockStateBuilder builder = getVariantBuilder(b);
		builder.partialState().with(isSlave, true).setModels(EMPTY_MODEL);
		boolean[] possibleMirrorStates;
		if(mirroredState!=null)
			possibleMirrorStates = new boolean[]{false, true};
		else
			possibleMirrorStates = new boolean[1];
		for(boolean mirrored : possibleMirrorStates)
			for(Direction dir : facing.getAllowedValues())
			{
				final int angleY;
				final int angleX;
				if(facing.getAllowedValues().contains(Direction.UP))
				{
					angleX = -90*dir.getYOffset();
					if(dir.getAxis()!=Axis.Y)
						angleY = getAngle(dir, rotationOffset);
					else
						angleY = 0;
				}
				else
				{
					angleY = getAngle(dir, rotationOffset);
					angleX = 0;
				}
				ModelFile model = mirrored?mirroredModel: masterModel;
				PartialBlockstate partialState = builder.partialState()
						.with(isSlave, false)
						.with(facing, dir);
				if(mirroredState!=null)
					partialState = partialState.with(mirroredState, mirrored);
				partialState.setModels(new ConfiguredModel(model, angleX, angleY, true));
			}
	}

	private int getAngle(Direction dir, int offset)
	{
		return (int)((dir.getHorizontalAngle()+offset)%360);
	}

	/*
	private void createDirectionalBlock(Block b, IProperty<Direction> prop, ModelFile model, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder builder = new Builder(b);
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			builder.setForAllWithState(ImmutableMap.of(prop, d), new ConfiguredModel(model, 0, getAngle(d, 180),
					true, ImmutableMap.of()));
		out.accept(b, builder.build());
	}

	private void createWallmount(Block b, ResourceLocation texture, BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder builder = new Builder(b);
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
		{
			int rotation = getAngle(d, 0);
			for(WallmountBlock.Orientation or : Orientation.values())
			{
				ResourceLocation model = rl("block/wooden_device/wallmount"+or.modelSuffix()+".obj");
				builder.setForAllWithState(
						ImmutableMap.of(IEProperties.FACING_HORIZONTAL, d, WallmountBlock.ORIENTATION, or),
						new ConfiguredModel(new ExistingModelFileIE(model), 0, rotation, true,
								ImmutableMap.of("flip-v", true),
								ImmutableMap.of("#immersiveengineering:block/wooden_device/wallmount", texture.toString())));
			}
		}
		out.accept(b, builder.build());
	}

	private void forEachState(List<IProperty<?>> props, Consumer<Map<IProperty<?>, Object>> out)
	{
		if(props.size() > 0)
		{
			List<IProperty<?>> remaining = props.subList(1, props.size());
			IProperty<?> main = props.get(0);
			for(Object value : main.getAllowedValues())
				forEachState(remaining, map -> {
					map.put(main, value);
					out.accept(map);
				});
		}
		else
			out.accept(new HashMap<>());
	}

	private void createConnector(Block b, Function<Map<IProperty<?>, Object>, ResourceLocation> model,
								 Function<Map<IProperty<?>, Object>, ImmutableMap<String, String>> textures,
								 BiConsumer<Block, IVariantModelGenerator> out,
								 List<IProperty<?>> additional, BlockRenderLayer... layers)
	{
		final ModelFile connFile = new UncheckedModelFile(rl("connector"));
		final IProperty<Direction> facingProp;
		final int xForHorizontal;
		if(b.getDefaultState().has(IEProperties.FACING_ALL))
		{
			facingProp = IEProperties.FACING_ALL;
			xForHorizontal = 90;
		}
		else if(b.getDefaultState().has(IEProperties.FACING_TOP_DOWN))
		{
			facingProp = IEProperties.FACING_TOP_DOWN;
			xForHorizontal = 90;
		}
		else if(b.getDefaultState().has(IEProperties.FACING_HORIZONTAL))
		{
			facingProp = IEProperties.FACING_HORIZONTAL;
			xForHorizontal = 0;
		}
		else
		{
			facingProp = null;
			xForHorizontal = 0;
		}
		Preconditions.checkState(facingProp==null||b.getDefaultState().has(facingProp),
				b+" does not have "+facingProp);
		Builder builder = new Builder(b);
		forEachState(additional, map -> {
			final ImmutableMap<String, Object> customData = ImmutableMap.of("flip-v", true,
					"base", model.apply(map),
					"layers", Arrays.stream(layers)
							.map(BlockRenderLayer::name)
							.collect(Collectors.toList())
			);
			if(facingProp!=null)
			{
				if(facingProp.getAllowedValues().contains(Direction.DOWN))
				{
					builder.setForAllWithState(with(map, facingProp, Direction.DOWN),
							new ConfiguredModel(connFile, xForHorizontal-90, 0, true, customData,
									textures.apply(map)));
					builder.setForAllWithState(with(map, facingProp, Direction.UP),
							new ConfiguredModel(connFile, xForHorizontal+90, 0, true, customData,
									textures.apply(map)));
				}
				for(Direction d : Direction.BY_HORIZONTAL_INDEX)
				{
					int rotation = getAngle(d, 0);
					builder.setForAllWithState(
							with(map, facingProp, d),
							new ConfiguredModel(connFile, xForHorizontal, rotation, true, customData, textures.apply(map)));
				}
			}
			else
				builder.setForAllWithState(map,
						new ConfiguredModel(connFile, 0, 0, true, customData, textures.apply(map)));
		});
		out.accept(b, builder.build());
	}


	private void createConnector(Block b, Function<Map<IProperty<?>, Object>, ResourceLocation> model,
								 ImmutableMap<String, String> textures, BiConsumer<Block, IVariantModelGenerator> out,
								 List<IProperty<?>> additional, BlockRenderLayer... layers)
	{
		createConnector(b, model, state -> textures, out, additional, layers);
	}

	private <K, V> Map<K, V> with(Map<K, V> old, K newKey, V newVal)
	{
		Map<K, V> ret = new HashMap<>(old);
		ret.put(newKey, newVal);
		return ret;
	}

	private void createConnector(Block b, ResourceLocation model, ImmutableMap<String, String> textures,
								 BiConsumer<Block, IVariantModelGenerator> out, BlockRenderLayer... layers)
	{
		createConnector(b, map -> model, textures, out, ImmutableList.of(), layers);
	}

	private void createHemp(BiConsumer<Block, IVariantModelGenerator> out)
	{
		Builder builder = new Builder(Misc.hempPlant);
		for(EnumHempGrowth g : EnumHempGrowth.values())
			builder.setModel(Misc.hempPlant.getDefaultState().with(HempBlock.GROWTH, g),
					new ConfiguredModel(models.hempGrowth.get(g)));
		out.accept(Misc.hempPlant, builder.build());
	}
 */
}
