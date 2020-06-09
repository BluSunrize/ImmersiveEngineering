/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.models.ModelConfigurableSides.Loader;
import blusunrize.immersiveengineering.client.models.ModelConveyor.ConveyorLoader;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader;
import blusunrize.immersiveengineering.client.models.multilayer.MultiLayerLoader;
import blusunrize.immersiveengineering.common.blocks.EnumMetals;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.*;
import blusunrize.immersiveengineering.common.blocks.cloth.StripCurtainBlock;
import blusunrize.immersiveengineering.common.blocks.generic.PostBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock;
import blusunrize.immersiveengineering.common.blocks.generic.WallmountBlock.Orientation;
import blusunrize.immersiveengineering.common.blocks.metal.MetalLadderBlock.CoverType;
import blusunrize.immersiveengineering.common.blocks.metal.MetalScaffoldingType;
import blusunrize.immersiveengineering.common.blocks.plant.EnumHempGrowth;
import blusunrize.immersiveengineering.common.blocks.plant.HempBlock;
import blusunrize.immersiveengineering.common.blocks.wooden.TreatedWoodStyles;
import blusunrize.immersiveengineering.common.data.models.LoadedModelBuilder;
import blusunrize.immersiveengineering.common.util.fluids.IEFluid;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.FenceBlock;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.*;
import net.minecraftforge.client.model.generators.ModelFile.ExistingModelFile;
import net.minecraftforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;

import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
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
	private final ConfiguredModel EMPTY_MODEL;
	private final LoadedModels loadedModels;
	final Map<Block, ModelFile> itemModels = new HashMap<>();
	private final ExistingFileHelper existingFileHelper;

	public ModelFile blastFurnaceOff;
	public ModelFile blastFurnaceOn;
	public ModelFile cokeOvenOff;
	public ModelFile cokeOvenOn;
	public ModelFile alloySmelterOff;
	public ModelFile alloySmelterOn;

	public BlockStates(DataGenerator gen, ExistingFileHelper exHelper, LoadedModels loaded)
	{
		super(gen, MODID, exHelper);
		loadedModels = loaded;
		this.existingFileHelper = exHelper;
		EMPTY_MODEL = new ConfiguredModel(
				new ExistingModelFile(modLoc("block/ie_empty"), existingFileHelper)
		);
	}

	private String name(Block b)
	{
		return b.getRegistryName().getPath();
	}

	public void simpleBlockItem(Block b, ModelFile model)
	{
		simpleBlockItem(b, new ConfiguredModel(model));
	}

	private void simpleBlockItem(Block b, ConfiguredModel model)
	{
		simpleBlock(b, model);
		itemModels.put(b, model.model);
	}

	private void cubeSideVertical(Block b, ResourceLocation side, ResourceLocation vertical)
	{
		simpleBlockItem(b, models().cubeBottomTop(name(b), side, vertical, vertical));
	}

	private void cubeAll(Block b, ResourceLocation texture)
	{
		simpleBlockItem(b, models().cubeAll(name(b), texture));
	}

	private void scaffold(Block b, ResourceLocation others, ResourceLocation top)
	{
		simpleBlockItem(
				b,
				models().withExistingParent(name(b), modLoc("block/ie_scaffolding"))
						.texture("side", others)
						.texture("bottom", others)
						.texture("top", top)
		);
	}

	private void slabFor(Block b, ResourceLocation texture)
	{
		slabFor(b, texture, texture, texture);
	}

	private void slabFor(Block b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
	{
		slab(IEBlocks.toSlab.get(b), side, top, bottom);
	}

	private void slab(SlabBlock b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
	{
		ModelFile mainModel = models().slab(name(b)+"_bottom", side, bottom, top);
		slabBlock(
				b,
				mainModel,
				models().slabTop(name(b)+"_top", side, bottom, top),
				models().cubeBottomTop(name(b)+"_double", side, bottom, top)
		);
		itemModels.put(b, mainModel);
	}

	private void stairs(StairsBlock b, ResourceLocation texture)
	{
		stairs(b, texture, texture, texture);
	}

	private void stairs(StairsBlock b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
	{
		String baseName = name(b);
		ModelFile stairs = models().stairs(baseName, side, bottom, top);
		ModelFile stairsInner = models().stairsInner(baseName+"_inner", side, bottom, top);
		ModelFile stairsOuter = models().stairsOuter(baseName+"_outer", side, bottom, top);
		stairsBlock(b, stairs, stairsInner, stairsOuter);
		itemModels.put(b, stairs);
	}

	private ResourceLocation forgeLoc(String path)
	{
		return new ResourceLocation("forge", path);
	}

	private ResourceLocation addModelsPrefix(ResourceLocation in)
	{
		return new ResourceLocation(in.getNamespace(), "models/"+in.getPath());
	}

	private void postBlock(Block b, ResourceLocation texture)
	{
		ResourceLocation model = rl("block/wooden_device/wooden_post.obj.ie");
		assertModelExists(model);
		LoadedModelBuilder modelFile = loadedModels.withExistingParent(name(b), mcLoc("block"))
				.loader(modLoc("ie_obj"))
				.additional("model", addModelsPrefix(model))
				.additional("flip-v", true)
				.texture("texture", texture)
				.texture("particle", texture);
		getVariantBuilder(b)
				.partialState()
				.with(PostBlock.POST_SLAVE, 0)
				.setModels(new ConfiguredModel(modelFile));
		for(int i = 1; i <= 3; ++i)
			getVariantBuilder(b)
					.partialState()
					.with(PostBlock.POST_SLAVE, i)
					.setModels(new ConfiguredModel(
							models().withExistingParent("empty_"+b.getRegistryName().getPath(), EMPTY_MODEL.model.getLocation())
									.texture("particle", texture)
					));
	}

	private ModelFile cubeTwo(String name, ResourceLocation top, ResourceLocation bottom,
							  ResourceLocation side, ResourceLocation front)
	{
		return models().withExistingParent(name, modLoc("ie_two_cubed"))
				.texture("north", front)
				.texture("top", top)
				.texture("bottom", bottom)
				.texture("east", side)
				.texture("west", side)
				.texture("south", side);
	}

	private ModelFile cubeThree(String name, ResourceLocation def, ResourceLocation front)
	{
		return models().withExistingParent(name, modLoc("ie_three_cubed"))
				.texture("north", front)
				.texture("top", def)
				.texture("bottom", def)
				.texture("east", def)
				.texture("west", def)
				.texture("south", def);
	}

	private ModelFile obj(String loc)
	{
		Preconditions.checkArgument(loc.endsWith(".obj"));
		return obj(loc.substring(0, loc.length()-4), modLoc(loc));
	}

	private ModelFile obj(String name, ResourceLocation model)
	{
		return obj(name, model, ImmutableMap.of());
	}

	private ModelFile obj(String name, ResourceLocation model, Map<String, ResourceLocation> textures)
	{
		assertModelExists(model);
		LoadedModelBuilder ret = loadedModels.withExistingParent(name, mcLoc("block"))
				.loader(forgeLoc("obj"))
				.additional("detectCullableFaces", false)
				.additional("model", addModelsPrefix(model))
				.additional("flip-v", true);
		String particleTex = DataGenUtils.getTextureFromObj(model, existingFileHelper);
		if(particleTex.charAt(0)=='#')
			particleTex = textures.get(particleTex.substring(1)).toString();
		ret.texture("particle", particleTex);
		for(Entry<String, ResourceLocation> e : textures.entrySet())
			ret.texture(e.getKey(), e.getValue());
		return ret;
	}

	private LoadedModelBuilder ieObj(String loc)
	{
		Preconditions.checkArgument(loc.endsWith(".obj.ie"));
		return ieObj(loc.substring(0, loc.length()-7), modLoc(loc));
	}

	private LoadedModelBuilder ieObj(String name, ResourceLocation model)
	{
		return loadedModels.withExistingParent(name, mcLoc("block"))
				.loader(modLoc("ie_obj"))
				.additional("model", addModelsPrefix(model))
				.additional("flip-v", true)
				.texture("particle", DataGenUtils.getTextureFromObj(model, existingFileHelper));
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
		createStoneMultiblocks();
		createMetalMultiblocks();
		createConnectors();

		simpleBlock(Multiblocks.bucketWheel, EMPTY_MODEL);
		simpleBlock(MetalDevices.fluidPipe, ieObj("block/metal_device/fluid_pipe.obj.ie"));

		createMultiblock(
				MetalDevices.cloche,
				ieObj("block/metal_device/cloche.obj.ie")
		);
		createMultiblock(
				MetalDevices.turretChem,
				ieObj("block/metal_device/chem_turret.obj.ie")
		);
		createMultiblock(
				MetalDevices.turretGun,
				ieObj("block/metal_device/gun_turret.obj.ie")
		);
		createMultiblock(MetalDevices.teslaCoil, obj("block/metal_device/teslacoil.obj"),
				null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_ALL, null,
				180);
		for(Entry<EnumMetals, Block> chute : MetalDevices.chutes.entrySet())
		{
			ModelFile model = ieObj("block/metal_device/chute_"+chute.getKey().tagName(), rl("block/metal_device/chute.obj.ie"))
					.texture("texture", rl("block/metal/sheetmetal_"+chute.getKey().tagName()))
					.texture("particle", rl("block/metal/sheetmetal_"+chute.getKey().tagName()));
			simpleBlock(chute.getValue(), model);
		}

		simpleBlockItem(Misc.fakeLight, EMPTY_MODEL);

		createMultistateSingleModel(WoodenDevices.windmill, EMPTY_MODEL);
		createMultistateSingleModel(WoodenDevices.watermill, EMPTY_MODEL);
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
			itemModels.put(none, noneModel);
			itemModels.put(alu, aluModel);
			itemModels.put(steel, steelModel);
		}

		createWallmount(WoodenDevices.treatedWallmount, rl("block/wooden_device/wallmount"));
		{
			ModelFile turntableModel = models().cubeBottomTop("turntable",
					modLoc("block/wooden_device/turntable"),
					modLoc("block/wooden_device/turntable_bottom"),
					modLoc("block/wooden_device/turntable_top")
			);
			createRotatedBlock(WoodenDevices.turntable, s -> turntableModel, IEProperties.FACING_ALL, ImmutableList.of(), -90, 0);
			itemModels.put(WoodenDevices.turntable, turntableModel);
		}
		createWallmount(MetalDecoration.aluWallmount, rl("block/metal_decoration/aluminum_wallmount"));
		createWallmount(MetalDecoration.steelWallmount, rl("block/metal_decoration/steel_wallmount"));
		{
			ModelFile steelModel = ieObj("block/slope.obj.ie")
					.texture("texture", modLoc("block/metal_decoration/steel_scaffolding"))
					.texture("particle", modLoc("block/metal_decoration/steel_scaffolding"))
					.transforms(modLoc("item/block"));
			ModelFile aluModel = ieObj("slope_alu", modLoc("block/slope.obj.ie"))
					.texture("texture", modLoc("block/metal_decoration/aluminum_scaffolding"))
					.texture("particle", modLoc("block/metal_decoration/aluminum_scaffolding"))
					.transforms(modLoc("item/block"));
			createMultistateSingleModel(MetalDecoration.slopeSteel, new ConfiguredModel(steelModel));
			itemModels.put(MetalDecoration.slopeSteel, steelModel);
			createMultistateSingleModel(MetalDecoration.slopeAlu, new ConfiguredModel(aluModel));
			itemModels.put(MetalDecoration.slopeAlu, aluModel);
		}

		createRotatedBlock(StoneDecoration.coresample, map -> obj("block/coresample.obj"),
				IEProperties.FACING_HORIZONTAL, ImmutableList.of());
		ResourceLocation concreteTexture = rl("block/stone_decoration/concrete");
		simpleBlockItem(StoneDecoration.concreteSheet, models().carpet("concrete_sheet", concreteTexture));
		simpleBlockItem(StoneDecoration.concreteQuarter, quarter("concrete_quarter", concreteTexture));
		simpleBlockItem(StoneDecoration.concreteThreeQuarter, threeQuarter("concrete_three_quarter", concreteTexture));
		simpleBlock(StoneDecoration.concreteSprayed, obj("block/sprayed_concrete.obj"));

		createRotatedBlock(WoodenDevices.craftingTable, state -> obj("block/wooden_device/craftingtable.obj"),
				IEProperties.FACING_HORIZONTAL, ImmutableList.of());
		cubeAll(WoodenDevices.crate, modLoc("block/wooden_device/crate"));
		cubeAll(WoodenDevices.reinforcedCrate, modLoc("block/wooden_device/reinforced_crate"));
		{
			ModelFile gunpowderModel = models().cubeBottomTop(
					"gunpowder_barrel", rl("block/wooden_device/gunpowder_barrel"),
					rl("block/wooden_device/barrel_up_none"), rl("block/wooden_device/gunpowder_barrel_top")
			);
			createMultistateSingleModel(WoodenDevices.gunpowderBarrel, new ConfiguredModel(gunpowderModel));
			itemModels.put(WoodenDevices.gunpowderBarrel, gunpowderModel);
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
			itemModels.put(WoodenDevices.itemBatcher, batcherModel);
		}
		simpleBlockItem(WoodenDevices.fluidSorter, createRouterModel(rl("block/wooden_device/fluid_sorter"),
				"fluid_router"));
		simpleBlockItem(WoodenDevices.woodenBarrel,
				loadedModels.getBuilder("wooden_devices/barrel")
						.loader(Loader.NAME)
						.additional("type", "vertical")
						.additional("base_name", modLoc("block/wooden_device/barrel"))
		);

		createRotatedBlock(Cloth.curtain,
				state -> new ExistingModelFile(rl(
						state.getSetStates().get(StripCurtainBlock.CEILING_ATTACHED)==Boolean.FALSE?
								"block/stripcurtain":
								"block/stripcurtain_middle"
				), existingFileHelper), IEProperties.FACING_HORIZONTAL,
				ImmutableList.of(StripCurtainBlock.CEILING_ATTACHED));
		cubeAll(Cloth.cushion, modLoc("block/cushion"));
		createMultistateSingleModel(Cloth.shaderBanner, EMPTY_MODEL);
		createMultistateSingleModel(Cloth.shaderBannerWall, EMPTY_MODEL);

		simpleBlockItem(MetalDevices.barrel,
				loadedModels.getBuilder("metal_devices/barrel")
						.loader(Loader.NAME)
						.additional("type", "vertical")
						.additional("base_name", modLoc("block/metal_device/barrel")));

		for(Entry<Block, String> cap : ImmutableMap.of(
				MetalDevices.capacitorCreative, "creative",
				MetalDevices.capacitorLV, "lv",
				MetalDevices.capacitorMV, "mv",
				MetalDevices.capacitorHV, "hv"
		).entrySet())
		{
			ModelFile model = loadedModels.getBuilder("block/metal_device/capacitor_"+cap.getValue())
					.loader(Loader.NAME)
					.additional("type", "side_top_bottom")
					.additional("base_name", modLoc("block/metal_device/capacitor_"+cap.getValue()));
			simpleBlockItem(cap.getKey(), model);
		}
		{
			ModelFile model = loadedModels.getBuilder("block/metal_device/fluid_placer")
					.loader(Loader.NAME)
					.additional("type", "all_same_texture")
					.additional("base_name", modLoc("block/metal_device/fluid_placer"));
			simpleBlockItem(MetalDevices.fluidPlacer, model);
		}
		createMultiblock(MetalDevices.blastFurnacePreheater,
				obj("block/metal_device/blastfurnace_preheater.obj"));
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
			itemModels.put(MetalDevices.furnaceHeater, furnaceHeaterOff);
		}
		createPump();
		{
			ModelFile kineticDynamo = models().withExistingParent("kinetic_dynamo", mcLoc("block/cube"))
					.texture("down", modLoc("block/metal_device/dynamo_bottom"))
					.texture("south", modLoc("block/metal_device/dynamo_bottom"))
					.texture("up", modLoc("block/metal_device/dynamo_top"))
					.texture("north", modLoc("block/metal_device/dynamo_front"))
					.texture("west", modLoc("block/metal_device/dynamo_side"))
					.texture("east", modLoc("block/metal_device/dynamo_side"));
			createRotatedBlock(MetalDevices.dynamo, state -> kineticDynamo, IEProperties.FACING_HORIZONTAL,
					ImmutableList.of());
			itemModels.put(MetalDevices.dynamo, kineticDynamo);
		}
		simpleBlockItem(MetalDevices.thermoelectricGen, new ConfiguredModel(models().cubeBottomTop(
				"thermoelectric_generator",
				modLoc("block/metal_device/thermoelectric_gen_side"),
				modLoc("block/metal_device/thermoelectric_gen_bottom"),
				modLoc("block/metal_device/thermoelectric_gen_top")
		)));
		{
			JsonObject solid = new JsonObject();
			solid.addProperty("loader", forgeLoc("obj").toString());
			solid.addProperty("detectCullableFaces", false);
			solid.addProperty("flip-v", true);
			solid.addProperty("model", modLoc("models/block/metal_device/charging_station.obj").toString());
			JsonObject translucent = new JsonObject();
			translucent.addProperty("loader", forgeLoc("obj").toString());
			translucent.addProperty("detectCullableFaces", false);
			translucent.addProperty("flip-v", true);
			translucent.addProperty("model", modLoc("models/block/metal_device/charging_station_glass.obj").toString());
			ModelFile full = loadedModels.getBuilder("metal_device/charging_station")
					.loader(MultiLayerLoader.LOCATION)
					.additional("solid", solid)
					.additional("translucent", translucent)
					.transforms(modLoc("item/block"))
					.texture("particle", DataGenUtils.getTextureFromObj(
							modLoc("block/metal_device/charging_station.obj"),
							existingFileHelper
					));
			createRotatedBlock(MetalDevices.chargingStation,
					state -> full,
					IEProperties.FACING_HORIZONTAL,
					ImmutableList.of()
			);
			itemModels.put(MetalDevices.chargingStation, full);
		}
		for(Block b : MetalDevices.CONVEYORS.values())
			createMultistateSingleModel(b, new ConfiguredModel(
					loadedModels.getBuilder("metal_device/conveyor")
							.loader(ConveyorLoader.LOCATION)
			));
		createHemp();
		for(IEFluid f : IEFluid.IE_FLUIDS)
		{
			ResourceLocation stillTexture = f.getAttributes().getStillTexture();
			ModelFile model = models().getBuilder("block/fluid/"+f.getRegistryName().getPath())
					.texture("particle", stillTexture);
			getVariantBuilder(f.block).partialState().setModels(new ConfiguredModel(model));
		}
		createRotatedBlock(MetalDevices.toolbox, state -> obj("block/toolbox.obj"),
				IEProperties.FACING_HORIZONTAL, ImmutableList.of());

		loadedModels.backupModels();
	}

	public void fenceBlock(FenceBlock b, ResourceLocation texture)
	{
		super.fenceBlock(b, texture);
		itemModels.put(b,
				models().withExistingParent(b.getRegistryName().getPath()+"_inventory", mcLoc("block/fence_inventory"))
						.texture("texture", texture));
	}

	private ModelFile retexture(String name, ResourceLocation baseModel, ImmutableMap<String, ResourceLocation> textures)
	{
		LoadedModelBuilder ret = loadedModels.getBuilder(name)
				.loader(guessLoader(baseModel).get());
		for(Entry<String, ResourceLocation> e : textures.entrySet())
			ret.texture(e.getKey(), e.getValue());
		return ret;
	}

	private void createConnectors()
	{
		createConnector(
				MetalDevices.floodlight,
				rl("block/metal_device/floodlight.obj.ie"),
				ImmutableMap.of(),
				RenderType.getTranslucent(), RenderType.getSolid()
		);
		createConnector(Connectors.getEnergyConnector(WireType.LV_CATEGORY, false), rl("block/connector/connector_lv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/connector_lv")), RenderType.getSolid());
		createConnector(Connectors.getEnergyConnector(WireType.LV_CATEGORY, true), rl("block/connector/connector_lv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/relay_lv")),
				RenderType.getSolid());

		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, false), rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/connector_mv")), RenderType.getSolid());
		createConnector(Connectors.getEnergyConnector(WireType.MV_CATEGORY, true), rl("block/connector/connector_mv.obj"),
				ImmutableMap.of("texture", modLoc("block/connector/relay_mv")),
				RenderType.getSolid());

		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, false), rl("block/connector/connector_hv.obj"),
				ImmutableMap.of(), RenderType.getSolid());
		createConnector(Connectors.getEnergyConnector(WireType.HV_CATEGORY, true), rl("block/connector/relay_hv.obj"),
				ImmutableMap.of(), RenderType.getTranslucent());

		createConnector(Connectors.connectorStructural, rl("block/connector/connector_structural.obj.ie"),
				ImmutableMap.of(), RenderType.getSolid());
		createConnector(Connectors.connectorRedstone, rl("block/connector/connector_redstone.obj.ie"),
				ImmutableMap.of(), RenderType.getSolid());
		createConnector(Connectors.connectorProbe, rl("block/connector/connector_probe.obj.ie"),
				ImmutableMap.of(), RenderType.getCutout(), RenderType.getTranslucent());
		createConnector(Connectors.connectorBundled, rl("block/connector/connector_bundled.obj"),
				ImmutableMap.of(), RenderType.getCutout());
		//TODO all layers
		createConnector(Connectors.feedthrough, FeedthroughLoader.LOCATION, ImmutableMap.of(),
				RenderType.getBlockRenderTypes().toArray(new RenderType[0]));
		createConnector(MetalDevices.electricLantern, state -> rl("block/metal_device/e_lantern.obj"),
				state -> {
					if(state.getSetStates().get(IEProperties.ACTIVE)==Boolean.FALSE)
						return ImmutableMap.of("texture", modLoc("block/metal_device/electric_lantern"));
					else
						return ImmutableMap.of("texture", modLoc("block/metal_device/electric_lantern_on"));
				},
				ImmutableList.of(IEProperties.ACTIVE), RenderType.getSolid());

		createConnector(Connectors.redstoneBreaker, rl("block/connector/redstone_breaker.obj.ie"),
				ImmutableMap.of(), RenderType.getSolid());
		createConnector(Connectors.breakerswitch, map -> {
			if(map.getSetStates().get(IEProperties.ACTIVE)==Boolean.FALSE)
				return rl("block/connector/breaker_switch_off.obj.ie");
			else
				return rl("block/connector/breaker_switch_on.obj.ie");
		}, ImmutableMap.of(), ImmutableList.of(IEProperties.ACTIVE), RenderType.getSolid());
		{
			ResourceLocation leftModel = rl("block/connector/transformer_mv_left.obj");
			createConnector(Connectors.transformer, map -> {
				if(map.getSetStates().get(IEProperties.MULTIBLOCKSLAVE)==Boolean.TRUE)
					return EMPTY_MODEL.model.getLocation();
				else if(map.getSetStates().get(IEProperties.MIRRORED)==Boolean.FALSE)
					return leftModel;
				else
					return rl("block/connector/transformer_mv_right.obj");
			}, ImmutableMap.of(
					"particle", new ResourceLocation(DataGenUtils.getTextureFromObj(leftModel, existingFileHelper))
			), ImmutableList.of(
					IEProperties.MULTIBLOCKSLAVE,
					IEProperties.MIRRORED
			), RenderType.getSolid());
		}
		createConnector(Connectors.postTransformer, rl("block/connector/transformer_post.obj"),
				ImmutableMap.of(), RenderType.getSolid());
		{
			ResourceLocation leftModel = rl("block/connector/transformer_hv_left.obj");
			createConnector(Connectors.transformerHV, map -> {
				if(map.getSetStates().get(IEProperties.MULTIBLOCKSLAVE)==Boolean.TRUE)
					return EMPTY_MODEL.model.getLocation();
				else if(map.getSetStates().get(IEProperties.MIRRORED)==Boolean.FALSE)
					return leftModel;
				else
					return rl("block/connector/transformer_hv_right.obj");
			}, ImmutableMap.of(
					"particle", new ResourceLocation(DataGenUtils.getTextureFromObj(leftModel, existingFileHelper))
			), ImmutableList.of(
					IEProperties.MULTIBLOCKSLAVE,
					IEProperties.MIRRORED
			), RenderType.getSolid());
		}

		ResourceLocation ctModel = rl("block/connector/e_meter.obj");
		createConnector(Connectors.currentTransformer, map -> {
			if(map.getSetStates().get(IEProperties.MULTIBLOCKSLAVE)==Boolean.TRUE)
				return ctModel;
			else
				return EMPTY_MODEL.model.getLocation();
		}, ImmutableMap.of(
				"particle", new ResourceLocation(DataGenUtils.getTextureFromObj(ctModel, existingFileHelper))
		), ImmutableList.of(IEProperties.MULTIBLOCKSLAVE), RenderType.getSolid());
		createConnector(MetalDevices.razorWire, rl("block/razor_wire.obj.ie"), ImmutableMap.of(),
				RenderType.getSolid());
		createConnector(Cloth.balloon, map -> rl("block/balloon.obj.ie"), ImmutableMap.of(),
				ImmutableList.of(), RenderType.getTranslucent());
	}

	private void createMetalMultiblocks()
	{
		createMultiblock(Multiblocks.excavator, obj("block/metal_multiblock/excavator.obj"),
				obj("block/metal_multiblock/excavator_mirrored.obj"));
		createMultiblock(Multiblocks.crusher, obj("block/metal_multiblock/crusher_mirrored.obj"),
				obj("block/metal_multiblock/crusher.obj"));
		createMultiblock(Multiblocks.metalPress, obj("block/metal_multiblock/metal_press.obj"));
		createMultiblock(Multiblocks.assembler, obj("block/metal_multiblock/assembler.obj"));
		createMultiblock(Multiblocks.arcFurnace, obj("block/metal_multiblock/arc_furnace.obj"),
				obj("block/metal_multiblock/arc_furnace_mirrored.obj"));

		createMultiblock(Multiblocks.blastFurnaceAdv, obj("block/blastfurnace_advanced.obj"));
		createMultiblock(Multiblocks.silo, obj("block/metal_multiblock/silo.obj"));
		createMultiblock(Multiblocks.tank, obj("block/metal_multiblock/tank.obj"));
		createMultiblock(Multiblocks.bottlingMachine,
				obj("block/metal_multiblock/bottling_machine.obj"),
				obj("block/metal_multiblock/bottling_machine_mirrored.obj"));
		createMultiblock(Multiblocks.fermenter,
				obj("block/metal_multiblock/fermenter.obj"),
				obj("block/metal_multiblock/fermenter_mirrored.obj"));
		createMultiblock(Multiblocks.squeezer,
				obj("block/metal_multiblock/squeezer.obj"),
				obj("block/metal_multiblock/squeezer_mirrored.obj"));
		createMultiblock(Multiblocks.mixer,
				obj("block/metal_multiblock/mixer.obj"),
				obj("block/metal_multiblock/mixer_mirrored.obj"));
		createMultiblock(Multiblocks.refinery,
				obj("block/metal_multiblock/refinery.obj"),
				obj("block/metal_multiblock/refinery_mirrored.obj"));
		createMultiblock(Multiblocks.dieselGenerator,
				obj("block/metal_multiblock/diesel_generator.obj"),
				obj("block/metal_multiblock/diesel_generator_mirrored.obj"));
		createMultiblock(Multiblocks.lightningrod,
				obj("block/metal_multiblock/lightningrod.obj"));
		createMultiblock(WoodenDevices.workbench,
				ieObj("block/wooden_device/workbench.obj.ie"),
				null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180);
		createMultiblock(MetalDevices.sampleDrill,
				obj("block/metal_device/core_drill.obj"),
				null, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL, null, 180);
		createMultiblock(Multiblocks.autoWorkbench,
				obj("block/metal_multiblock/auto_workbench.obj"),
				obj("block/metal_multiblock/auto_workbench_mirrored.obj"),
				IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL,
				IEProperties.MIRRORED, 180);
	}

	private void createStoneMultiblocks()
	{
		blastFurnaceOff = cubeThree("blast_furnace_off",
				modLoc("block/multiblocks/blast_furnace"),
				modLoc("block/multiblocks/blast_furnace_off")
		);
		blastFurnaceOn = cubeThree("blast_furnace_on",
				modLoc("block/multiblocks/blast_furnace"),
				modLoc("block/multiblocks/blast_furnace_on")
		);
		cokeOvenOff = cubeThree("coke_oven_off",
				modLoc("block/multiblocks/coke_oven"),
				modLoc("block/multiblocks/coke_oven_off")
		);
		cokeOvenOn = cubeThree("coke_oven_on",
				modLoc("block/multiblocks/coke_oven"),
				modLoc("block/multiblocks/coke_oven_on")
		);
		alloySmelterOff = cubeTwo("alloy_smelter_off",
				modLoc("block/multiblocks/alloy_smelter_top"),
				modLoc("block/multiblocks/alloy_smelter_bottom"),
				modLoc("block/multiblocks/alloy_smelter_side"),
				modLoc("block/multiblocks/alloy_smelter_off")
		);
		alloySmelterOn = cubeTwo("alloy_smelter_on",
				modLoc("block/multiblocks/alloy_smelter_top"),
				modLoc("block/multiblocks/alloy_smelter_bottom"),
				modLoc("block/multiblocks/alloy_smelter_side"),
				modLoc("block/multiblocks/alloy_smelter_on")
		);
		createMultiblock(Multiblocks.cokeOven, cokeOvenOff, cokeOvenOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180,
				modLoc("block/multiblocks/coke_oven"));
		createMultiblock(Multiblocks.alloySmelter, alloySmelterOff, alloySmelterOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180,
				modLoc("block/multiblocks/alloy_smelter_side"));
		createMultiblock(Multiblocks.blastFurnace, blastFurnaceOff, blastFurnaceOn, IEProperties.MULTIBLOCKSLAVE,
				IEProperties.FACING_HORIZONTAL, IEProperties.ACTIVE, 180,
				modLoc("block/multiblocks/blast_furnace"));
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
						loadedModels.getBuilder("metal_device/pump_bottom")
								.loader(Loader.NAME)
								.additional("type", "side_vertical")
								.additional("base_name", modLoc("block/metal_device/fluid_pump"))
				));
	}

	private void createRotatedBlock(Block block, Function<PartialBlockstate, ModelFile> model, IProperty<Direction> facing,
									List<IProperty<?>> additionalProps)
	{
		createRotatedBlock(block, model, facing, additionalProps, 0, 180);
	}

	private void createRotatedBlock(Block block, Function<PartialBlockstate, ModelFile> model, IProperty<Direction> facing,
									List<IProperty<?>> additionalProps, int offsetRotX, int offsetRotY)
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
		String objLoc = ((ModelBuilder<?>)masterModel).toJson().get("model").getAsString();
		objLoc = objLoc.substring(0, objLoc.indexOf(':')+1)+objLoc.substring(objLoc.indexOf('/')+1);
		createMultiblock(b, masterModel, mirroredModel, isSlave, facing, mirroredState, rotationOffset,
				new ResourceLocation(DataGenUtils.getTextureFromObj(
						new ResourceLocation(objLoc),
						existingFileHelper
				)));
	}

	private void createMultiblock(Block b, ModelFile masterModel, @Nullable ModelFile mirroredModel, IProperty<Boolean> isSlave,
								  EnumProperty<Direction> facing, @Nullable IProperty<Boolean> mirroredState, int rotationOffset,
								  ResourceLocation particleTex)
	{
		Preconditions.checkArgument((mirroredModel==null)==(mirroredState==null));
		VariantBlockStateBuilder builder = getVariantBuilder(b);
		builder.partialState()
				.with(isSlave, true)
				.setModels(new ConfiguredModel(
						models().withExistingParent(b.getRegistryName().getPath()+"_empty", EMPTY_MODEL.model.getLocation())
								.texture("particle", particleTex)
				));
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
		textures.put("ladder", new ResourceLocation(ImmersiveEngineering.MODID, "block/metal_decoration/metal_ladder"));
		LoadedModelBuilder ret = loadedModels.withExistingParent(name, parent);
		if(bottomTop!=null)
			ret.transforms(rl("item/block"));
		for(Entry<String, ResourceLocation> e : textures.entrySet())
			ret.texture(e.getKey(), e.getValue());
		return ret;
	}

	private void createDirectionalBlock(Block b, IProperty<Direction> prop, ModelFile model)
	{
		VariantBlockStateBuilder builder = getVariantBuilder(b);
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
			builder.partialState()
					.with(prop, d)
					.setModels(new ConfiguredModel(model, 0, getAngle(d, 180), true));
	}

	private void createWallmount(Block b, ResourceLocation texture)
	{
		VariantBlockStateBuilder stateBuilder = getVariantBuilder(b);
		for(Direction d : Direction.BY_HORIZONTAL_INDEX)
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

	private <T extends Comparable<T>> void forEach(PartialBlockstate base, IProperty<T> prop,
												   List<IProperty<?>> remaining, Consumer<PartialBlockstate> out)
	{
		for(T value : prop.getAllowedValues())
			forEachState(base, remaining, map -> {
				map = map.with(prop, value);
				out.accept(map);
			});
	}

	private void forEachState(PartialBlockstate base, List<IProperty<?>> props, Consumer<PartialBlockstate> out)
	{
		if(props.size() > 0)
		{
			List<IProperty<?>> remaining = props.subList(1, props.size());
			IProperty<?> main = props.get(0);
			forEach(base, main, remaining, out);
		}
		else
			out.accept(base);
	}

	private void createConnector(Block b, Function<PartialBlockstate, ResourceLocation> model,
								 Function<PartialBlockstate, ImmutableMap<String, ResourceLocation>> textures,
								 List<IProperty<?>> additional, RenderType... layers)
	{
		Preconditions.checkArgument(layers.length > 0);
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
		VariantBlockStateBuilder builder = getVariantBuilder(b);
		forEachState(builder.partialState(), additional, map -> {
			final List<String> layersList = Arrays.stream(layers)
					.map(RenderType::toString) // toString is implemented as getName
					.collect(Collectors.toList());
			if(facingProp!=null)
			{
				for(Direction d : facingProp.getAllowedValues())
					if(d==Direction.DOWN)
					{
						PartialBlockstate downState = map.with(facingProp, Direction.DOWN);
						ModelFile downModel = forConnectorModel(downState, model, layersList, textures);
						builder.setModels(downState,
								new ConfiguredModel(downModel, xForHorizontal-90, 0, true));
					}
					else if(d==Direction.UP)
					{
						PartialBlockstate upState = map.with(facingProp, Direction.UP);
						ModelFile upModel = forConnectorModel(upState, model, layersList, textures);
						builder.setModels(upState,
								new ConfiguredModel(upModel, xForHorizontal+90, 0, true));
					}
					else
					{
						int rotation = getAngle(d, 0);
						PartialBlockstate dState = map.with(facingProp, d);
						ModelFile connFile = forConnectorModel(dState, model, layersList, textures);
						builder.setModels(dState, new ConfiguredModel(connFile, xForHorizontal, rotation, true));
					}
			}
			else
			{
				ModelFile connFile = forConnectorModel(map, model, layersList, textures);
				builder.setModels(map,
						new ConfiguredModel(connFile, 0, 0, true));
			}
		});
	}

	private ModelFile forConnectorModel(PartialBlockstate state, Function<PartialBlockstate, ResourceLocation> model,
										List<String> layers,
										Function<PartialBlockstate, ImmutableMap<String, ResourceLocation>> textures)
	{
		JsonObject baseJson = new JsonObject();
		ResourceLocation modelLoc = model.apply(state);
		Optional<ResourceLocation> loader = guessLoader(modelLoc);
		if(!loader.isPresent())
			baseJson.addProperty("parent", EMPTY_MODEL.model.getLocation().toString());
		else
		{
			baseJson.addProperty("loader", loader.get().toString());
			if(!FeedthroughLoader.LOCATION.equals(loader.get()))
			{
				baseJson.addProperty("model", addModelsPrefix(modelLoc).toString());
				baseJson.addProperty("flip-v", true);
			}
		}
		ImmutableMap<String, ResourceLocation> texForState = textures.apply(state);
		LoadedModelBuilder ret = loadedModels.getBuilder(
				nameFor(state.getOwner(), modelLoc, texForState)
		)
				.loader(ConnectionLoader.LOADER_NAME)
				.additional("base_model", baseJson)
				.additional("layers", layers);
		for(Entry<String, ResourceLocation> e : texForState.entrySet())
			ret.texture(e.getKey(), e.getValue());
		if(!texForState.containsKey("particle")&&loader.isPresent()&&loader.get().getPath().contains("obj"))
		{
			String particleTex = DataGenUtils.getTextureFromObj(modelLoc, existingFileHelper);
			if(particleTex.charAt(0)=='#')
				particleTex = texForState.get(particleTex.substring(1)).toString();
			ret.texture("particle", particleTex);
			ret.additional("detectCullableFaces", false);
		}
		return ret;
	}

	private Optional<ResourceLocation> guessLoader(ResourceLocation modelLoc)
	{
		if(modelLoc.getPath().endsWith(".obj"))
			return Optional.of(forgeLoc("obj"));
		else if(modelLoc.getPath().endsWith(".obj.ie"))
			return Optional.of(modLoc("ie_obj"));
		else if(modelLoc.equals(EMPTY_MODEL.model.getLocation()))
			return Optional.empty();
		else if(modelLoc.equals(FeedthroughLoader.LOCATION))
			return Optional.of(FeedthroughLoader.LOCATION);
		else
			throw new RuntimeException("Failed to guess loader for "+modelLoc);
	}

	Map<String, Map<Map<String, ResourceLocation>, Integer>> nameCache = new HashMap<>();

	private String nameFor(Block b, ResourceLocation model, ImmutableMap<String, ResourceLocation> texReplacement)
	{
		String modelPath = model.getPath();
		String blockName = b.getRegistryName().getPath();
		String base;
		if(modelPath.endsWith(".obj"))
			base = modelPath.substring(0, modelPath.length()-4);
		else if(modelPath.endsWith(".obj.ie"))
			base = modelPath.substring(0, modelPath.length()-7);
		else if(FeedthroughLoader.LOCATION.equals(model))
			base = "feedthrough";
		else if(EMPTY_MODEL.model.getLocation().equals(model))
			base = blockName+"_empty";
		else
			throw new RuntimeException("Unknown model type: "+model);
		if(!nameCache.containsKey(base))
			nameCache.put(base, new HashMap<>());
		Map<Map<String, ResourceLocation>, Integer> namesForModel = nameCache.get(base);
		int index;
		if(namesForModel.containsKey(texReplacement))
			index = namesForModel.get(texReplacement);
		else
		{
			index = namesForModel.size();
			namesForModel.put(texReplacement, index);
		}
		if(index==0)
			return base;
		else
			return base+"_"+index;
	}

	private void createConnector(Block b, Function<PartialBlockstate, ResourceLocation> model,
								 ImmutableMap<String, ResourceLocation> textures,
								 List<IProperty<?>> additional, RenderType... layers)
	{
		createConnector(b, model, state -> textures, additional, layers);
	}

	private void createConnector(Block b, ResourceLocation model, ImmutableMap<String, ResourceLocation> textures,
								 RenderType... layers)
	{
		createConnector(b, map -> model, textures, ImmutableList.of(), layers);
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
			ModelFile model = models().withExistingParent("block/hemp/"+g.getName(),
					new ResourceLocation("block/crop"))
					.texture("crop", g.getTextureName());
			builder.partialState()
					.with(HempBlock.GROWTH, g)
					.setModels(new ConfiguredModel(model));
		}
	}

	private ModelFile createRouterModel(ResourceLocation baseTexName, String outName)
	{
		BlockModelBuilder builder = models().withExistingParent(outName, modLoc("block/ie_six_sides"));
		for(Direction d : Direction.VALUES)
			builder.texture(d.getName(), new ResourceLocation(baseTexName.getNamespace(),
					baseTexName.getPath()+"_"+d.ordinal()));
		builder.texture("particle", new ResourceLocation(baseTexName.getNamespace(),
				baseTexName.getPath()+"_0"));
		return builder;
	}

	public void assertModelExists(ResourceLocation name)
	{
		String suffix = name.getPath().contains(".")?"": ".json";
		Preconditions.checkState(
				existingFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, suffix, "models"),
				"Model \""+name+"\" does not exist");
	}

}
