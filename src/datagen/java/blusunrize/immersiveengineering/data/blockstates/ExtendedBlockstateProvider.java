/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEStairsBlock;
import blusunrize.immersiveengineering.common.blocks.IEWallBlock;
import blusunrize.immersiveengineering.common.register.IEBlocks;
import blusunrize.immersiveengineering.data.DataGenUtils;
import blusunrize.immersiveengineering.data.models.*;
import blusunrize.immersiveengineering.data.models.NongeneratedModels.NongeneratedModel;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.properties.*;
import net.neoforged.neoforge.client.model.generators.*;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel.Builder;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder.PartBuilder;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder.PartialBlockstate;
import net.neoforged.neoforge.client.model.generators.loaders.ObjModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class ExtendedBlockstateProvider extends BlockStateProvider
{
	protected static final List<Vec3i> COLUMN_THREE = ImmutableList.of(BlockPos.ZERO, BlockPos.ZERO.above(), BlockPos.ZERO.above(2));

	protected static final Map<ResourceLocation, String> generatedParticleTextures = new HashMap<>();
	protected final ExistingFileHelper existingFileHelper;
	protected final NongeneratedModels innerModels;

	public ExtendedBlockstateProvider(PackOutput output, ExistingFileHelper exFileHelper)
	{
		super(output, Lib.MODID, exFileHelper);
		this.existingFileHelper = exFileHelper;
		this.innerModels = new NongeneratedModels(output, existingFileHelper);
	}

	protected String name(Supplier<? extends Block> b)
	{
		return name(b.get());
	}

	protected String name(Block b)
	{
		return BuiltInRegistries.BLOCK.getKey(b).getPath();
	}

	public void simpleBlockAndItem(Supplier<? extends Block> b, ModelFile model)
	{
		simpleBlockAndItem(b, new ConfiguredModel(model));
	}

	protected void simpleBlockAndItem(Supplier<? extends Block> b, ConfiguredModel model)
	{
		simpleBlock(b.get(), model);
		itemModel(b, model.model);
	}

	public void multiBlockAndItem(Supplier<? extends Block> b, ModelFile... models)
	{
		simpleBlock(b.get(), Stream.of(models).map(ConfiguredModel::new).toArray(ConfiguredModel[]::new));
		itemModel(b, new ConfiguredModel(models[0]).model);
	}

	protected void cubeSideVertical(Supplier<? extends Block> b, ResourceLocation side, ResourceLocation vertical)
	{
		simpleBlockAndItem(b, models().cubeBottomTop(name(b), side, vertical, vertical));
	}

	protected void cubeAll(Supplier<? extends Block> b, ResourceLocation texture)
	{
		cubeAll(b, texture, null);
	}

	protected void cubeAll(Supplier<? extends Block> b, ResourceLocation texture, @Nullable RenderType layer)
	{
		final BlockModelBuilder model = models().cubeAll(name(b), texture);
		setRenderType(layer, model);
		simpleBlockAndItem(b, model);
	}

	protected void multiCubeAll(Supplier<? extends Block> b, ResourceLocation... textures)
	{
		multiCubeAll(b, null, textures);
	}

	protected void multiCubeAll(Supplier<? extends Block> b, @Nullable RenderType layer, ResourceLocation... textures)
	{
		final BlockModelBuilder[] models = new BlockModelBuilder[textures.length];
		for(int i = 0; i < textures.length; i++)
		{
			models[i] = models().cubeAll(name(b)+i, textures[i]);
			setRenderType(layer, models[i]);
		}
		multiBlockAndItem(b, models);
	}

	protected void multiEightCubeAll(Supplier<? extends Block> b, ResourceLocation texture)
	{
		ResourceLocation[] textures = new ResourceLocation[8];
		for(int i = 0; i < 8; i++)
			textures[i] = texture.withSuffix(Integer.toString(i));
		multiCubeAll(b, textures);
	}

	protected void scaffold(Supplier<? extends Block> b, ResourceLocation others, ResourceLocation top)
	{
		simpleBlockAndItem(
				b,
				models().withExistingParent(name(b), modLoc("block/ie_scaffolding"))
						.texture("side", others)
						.texture("bottom", others)
						.texture("top", top)
						.renderType(ModelProviderUtils.getName(RenderType.cutout()))
		);
	}

	protected void slabFor(Supplier<? extends Block> b, ResourceLocation texture)
	{
		slabFor(b, texture, null);
	}

	protected void slabFor(Supplier<? extends Block> b, ResourceLocation texture, @Nullable RenderType layer)
	{
		slabFor(b, texture, texture, texture, layer);
	}

	protected void slabFor(Supplier<? extends Block> b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
	{
		slabFor(b, side, top, bottom, null);
	}

	protected void slabFor(
			Supplier<? extends Block> full,
			ResourceLocation side, ResourceLocation top, ResourceLocation bottom,
			@Nullable RenderType layer
	)
	{
		SlabBlock b = IEBlocks.TO_SLAB.get(BuiltInRegistries.BLOCK.getKey(full.get())).get();
		ModelBuilder<?> mainModel = models().slab(name(b)+"_bottom", side, bottom, top);
		ModelBuilder<?> topModel = models().slabTop(name(b)+"_top", side, bottom, top);
		ModelBuilder<?> doubleModel = models().cubeBottomTop(name(b)+"_double", side, bottom, top);
		setRenderType(layer, mainModel, topModel, doubleModel);
		slabBlock(b, mainModel, topModel, doubleModel);
		itemModel(() -> b, mainModel);
	}

	protected void slabForMultiEightAll(Supplier<? extends Block> b, ResourceLocation texture)
	{
		ResourceLocation[] textures = new ResourceLocation[8];
		for(int i = 0; i < 8; i++)
			textures[i] = texture.withSuffix(Integer.toString(i));
		slabForMultiAll(b, textures);
	}

	protected void slabForMultiAll(Supplier<? extends Block> b, ResourceLocation... textures)
	{
		slabForMultiAll(b, null, textures);
	}

	protected void slabForMultiAll(Supplier<? extends Block> full, @Nullable RenderType layer, ResourceLocation... textures)
	{
		SlabBlock b = IEBlocks.TO_SLAB.get(BuiltInRegistries.BLOCK.getKey(full.get())).get();

		final ModelBuilder<?>[] mainModels = new ModelBuilder<?>[textures.length];
		final ModelBuilder<?>[] topModels = new ModelBuilder<?>[textures.length];
		final ModelBuilder<?>[] doubleModels = new ModelBuilder<?>[textures.length];
		for(int i = 0; i < textures.length; i++)
		{
			mainModels[i] = models().slab(name(b)+i+"_bottom", textures[i], textures[i], textures[i]);
			topModels[i] = models().slabTop(name(b)+i+"_top", textures[i], textures[i], textures[i]);
			doubleModels[i] = models().cubeAll(name(b)+i+"_double", textures[i]);
			setRenderType(layer, mainModels[i], topModels[i], doubleModels[i]);
		}

		slabBlock(b, mainModels, topModels, doubleModels);
		itemModel(() -> b, mainModels[0]);
	}

	//Forge method does not allow random textures for slabs, instead creating a ConfiguredModel directly from an input file
	public void slabBlock(SlabBlock block, ModelFile[] bottom, ModelFile[] top, ModelFile[] doubleslab)
	{
		getVariantBuilder(block)
				.partialState().with(SlabBlock.TYPE, SlabType.BOTTOM).addModels(Stream.of(bottom).map(ConfiguredModel::new).toArray(ConfiguredModel[]::new))
				.partialState().with(SlabBlock.TYPE, SlabType.TOP).addModels(Stream.of(top).map(ConfiguredModel::new).toArray(ConfiguredModel[]::new))
				.partialState().with(SlabBlock.TYPE, SlabType.DOUBLE).addModels(Stream.of(doubleslab).map(ConfiguredModel::new).toArray(ConfiguredModel[]::new));
	}

	protected void stairsFor(Supplier<? extends Block> b, ResourceLocation texture)
	{
		stairsFor(b, texture, texture, texture, null);
	}

	protected void stairsFor(
			Supplier<? extends Block> full,
			ResourceLocation side, ResourceLocation top, ResourceLocation bottom,
			@Nullable RenderType layer
	)
	{
		final IEStairsBlock b = IEBlocks.TO_STAIRS.get(BuiltInRegistries.BLOCK.getKey(full.get())).get();
		String baseName = name(b);
		ModelBuilder<?> stairs = models().stairs(baseName, side, bottom, top);
		ModelBuilder<?> stairsInner = models().stairsInner(baseName+"_inner", side, bottom, top);
		ModelBuilder<?> stairsOuter = models().stairsOuter(baseName+"_outer", side, bottom, top);
		setRenderType(layer, stairs, stairsInner, stairsOuter);
		stairsBlock(b, stairs, stairsInner, stairsOuter);
		itemModel(() -> b, stairs);
	}

	protected void stairsForMultiEightAll(Supplier<? extends Block> b, ResourceLocation texture)
	{
		ResourceLocation[] textures = new ResourceLocation[8];
		for(int i = 0; i < 8; i++)
			textures[i] = texture.withSuffix(Integer.toString(i));
		stairsForMultiAll(b, textures);
	}

	protected void stairsForMultiAll(Supplier<? extends Block> b, ResourceLocation... textures)
	{
		stairsForMultiAll(b, null, textures);
	}

	protected void stairsForMultiAll(Supplier<? extends Block> full, @Nullable RenderType layer, ResourceLocation... textures)
	{
		final IEStairsBlock b = IEBlocks.TO_STAIRS.get(BuiltInRegistries.BLOCK.getKey(full.get())).get();

		final ModelBuilder<?>[] stairs = new ModelBuilder<?>[textures.length];
		final ModelBuilder<?>[] stairsInner = new ModelBuilder<?>[textures.length];
		final ModelBuilder<?>[] stairsOuter = new ModelBuilder<?>[textures.length];
		for(int i = 0; i < textures.length; i++)
		{
			stairs[i] = models().stairs(name(b)+i, textures[i], textures[i], textures[i]);
			stairsInner[i] = models().stairsInner(name(b)+i+"_inner", textures[i], textures[i], textures[i]);
			stairsOuter[i] = models().stairsOuter(name(b)+i+"_outer", textures[i], textures[i], textures[i]);
			setRenderType(layer, stairs[i], stairsInner[i], stairsOuter[i]);
		}

		stairsBlock(b, stairs, stairsInner, stairsOuter);
		itemModel(() -> b, stairs[0]);
	}

	//Forge method does not allow random textures for slabs, instead creating a ConfiguredModel directly from an input file
	public void stairsBlock(StairBlock block, ModelFile[] stairs, ModelFile[] stairsInner, ModelFile[] stairsOuter)
	{
		getVariantBuilder(block)
				.forAllStatesExcept(state -> {
					Direction facing = state.getValue(StairBlock.FACING);
					Half half = state.getValue(StairBlock.HALF);
					StairsShape shape = state.getValue(StairBlock.SHAPE);
					int yRot = (int)facing.getClockWise().toYRot(); // Stairs model is rotated 90 degrees clockwise for some reason
					if(shape==StairsShape.INNER_LEFT||shape==StairsShape.OUTER_LEFT)
					{
						yRot += 270; // Left facing stairs are rotated 90 degrees clockwise
					}
					if(shape!=StairsShape.STRAIGHT&&half==Half.TOP)
					{
						yRot += 90; // Top stairs are rotated 90 degrees clockwise
					}
					yRot %= 360;
					boolean uvlock = yRot!=0||half==Half.TOP; // Don't set uvlock for states that have no rotation
					//We need multiple textures, so no builder
					ModelFile[] files = (shape==StairsShape.STRAIGHT?stairs: shape==StairsShape.INNER_LEFT||shape==StairsShape.INNER_RIGHT?stairsInner: stairsOuter);
					ConfiguredModel[] models = new ConfiguredModel[stairs.length];
					for(int i = 0; i < stairs.length; i++)
						models[i] = new ConfiguredModel(files[i], half==Half.BOTTOM?0: 180, yRot, uvlock);
					return models;
				}, StairBlock.WATERLOGGED);
	}

	protected void wallForSingle(Supplier<? extends Block> full, ResourceLocation bottomTexture, ResourceLocation sideTexture, ResourceLocation topTexture)
	{
		final IEWallBlock b = IEBlocks.TO_WALL.get(BuiltInRegistries.BLOCK.getKey(full.get())).get();
		wallBlock(b,
				wallModelTopped(name(b)+"_post", "wall_post_topped", bottomTexture, sideTexture, topTexture),
				wallModelTopped(name(b)+"_side", "wall_side_topped", bottomTexture, sideTexture, topTexture),
				wallModelTopped(name(b)+"_side_tall", "wall_side_tall_topped", bottomTexture, sideTexture, topTexture)
		);
		itemModel(() -> b, wallModelToppedInventory(name(b), bottomTexture, sideTexture, topTexture));
	}

	protected void wallForMultiEight(Supplier<? extends Block> b, ResourceLocation bottomTexture, ResourceLocation sideTexture, ResourceLocation topTexture)
	{
		ResourceLocation[] bottomTextures = new ResourceLocation[8];
		ResourceLocation[] sideTextures = new ResourceLocation[8];
		ResourceLocation[] topTextures = new ResourceLocation[8];
		for(int i = 0; i < 8; i++)
		{
			bottomTextures[i] = bottomTexture.withSuffix(Integer.toString(i));
			sideTextures[i] = sideTexture.withSuffix(Integer.toString(i));
			topTextures[i] = topTexture.withSuffix(Integer.toString(i));
		}
		wallForMultiMany(b, bottomTextures, sideTextures, topTextures);
	}

	protected void wallForMultiMany(Supplier<? extends Block> b, ResourceLocation[] bottomTextures, ResourceLocation[] sideTextures, ResourceLocation[] topTextures)
	{
		wallForMultiMany(b, null, bottomTextures, sideTextures, topTextures);
	}

	protected void wallForMultiMany(Supplier<? extends Block> full, @Nullable RenderType layer, ResourceLocation[] bottomTextures, ResourceLocation[] sideTextures, ResourceLocation[] topTextures)
	{
		final IEWallBlock b = IEBlocks.TO_WALL.get(BuiltInRegistries.BLOCK.getKey(full.get())).get();

		final ModelBuilder<?>[] wallPost = new ModelBuilder<?>[bottomTextures.length];
		final ModelBuilder<?>[] wallSide = new ModelBuilder<?>[bottomTextures.length];
		final ModelBuilder<?>[] wallSideTall = new ModelBuilder<?>[bottomTextures.length];
		for(int i = 0; i < bottomTextures.length; i++)
		{
			wallPost[i] = wallModelTopped(name(b)+i+"_post", "wall_post_topped", bottomTextures[i], sideTextures[i], topTextures[i]);
			wallSide[i] = wallModelTopped(name(b)+i+"_side", "wall_side_topped", bottomTextures[i], sideTextures[i], topTextures[i]);
			wallSideTall[i] = wallModelTopped(name(b)+i+"_side_tall", "wall_side_tall_topped", bottomTextures[i], sideTextures[i], topTextures[i]);
			setRenderType(layer, wallPost[i], wallSide[i], wallSideTall[i]);
		}

		wallBlock(b, wallPost, wallSide, wallSideTall);
		itemModel(() -> b, wallModelToppedInventory(name(b), bottomTextures[0], sideTextures[0], topTextures[0]));
	}

	//Forge method does not allow random textures for walls, instead creating ConfiguredModel directly from input file
	private void wallBlock(WallBlock block, ModelFile[] posts, ModelFile[] sides, ModelFile[] sidesTall)
	{
		MultiPartBlockStateBuilder builder = getMultipartBuilder(block);
		Builder<PartBuilder> part = builder.part();
		for (int i = 0; i < posts.length; i++)
		{
			part = part.modelFile(posts[i]);
			if ((i + 1) < posts.length)
				part = part.nextModel();
		}
		part.addModel().condition(WallBlock.UP, true);
		WALL_PROPS.entrySet().stream()
				.filter(e -> e.getKey().getAxis().isHorizontal())
				.forEach(e -> {
					wallSidePart(builder, sides, e, WallSide.LOW);
					wallSidePart(builder, sidesTall, e, WallSide.TALL);
				});
	}

	//The equivalent method is private in BlockStateProvider & we need access to it
	private void wallSidePart(MultiPartBlockStateBuilder builder, ModelFile[] models, Entry<Direction, Property<WallSide>> entry, WallSide height)
	{
		Builder<MultiPartBlockStateBuilder.PartBuilder> part = builder.part();
		for (int i = 0; i < models.length; i++)
		{
			part = part.modelFile(models[i])
					.rotationY((((int)entry.getKey().toYRot())+180)%360)
					.uvLock(true);
			if ((i + 1) < models.length)
				part = part.nextModel();
		}
		part.addModel().condition(entry.getValue(), height);
	}

	protected void setRenderType(@Nullable RenderType type, ModelBuilder<?>... builders)
	{
		if(type!=null)
		{
			final String typeName = ModelProviderUtils.getName(type);
			for(final ModelBuilder<?> model : builders)
				model.renderType(typeName);
		}
	}

	protected ResourceLocation addModelsPrefix(ResourceLocation in)
	{
		return in.withPath("models/"+in.getPath());
	}

	protected void itemModel(Supplier<? extends Block> block, ModelFile model)
	{
		itemModels().getBuilder(name(block)).parent(model);
	}

	protected BlockModelBuilder wallModelTopped(String name, String type, ResourceLocation bottom, ResourceLocation side, ResourceLocation top)
	{
		return models().withExistingParent(name, ImmersiveEngineering.rl("block/"+type))
				.texture("wall_bottom", bottom)
				.texture("wall_side", side)
				.texture("wall_top", top);
	}

	protected BlockModelBuilder wallModelToppedInventory(String name, ResourceLocation bottom, ResourceLocation side, ResourceLocation top)
	{
		return models().withExistingParent(name, ImmersiveEngineering.rl("block/wall_inventory_topped"))
				.texture("wall_bottom", bottom)
				.texture("wall_side", side)
				.texture("wall_top", top);
	}

	protected NongeneratedModel innerObj(String loc, @Nullable RenderType layer)
	{
		Preconditions.checkArgument(loc.endsWith(".obj"));
		final var result = obj(loc.substring(0, loc.length()-4), modLoc(loc), innerModels);
		setRenderType(layer, result);
		return result;
	}

	protected NongeneratedModel innerObj(String loc)
	{
		return innerObj(loc, null);
	}

	protected BlockModelBuilder obj(String loc)
	{
		return obj(loc, (RenderType)null);
	}

	protected BlockModelBuilder obj(String loc, @Nullable RenderType layer)
	{
		final var model = obj(loc, models());
		setRenderType(layer, model);
		return model;
	}

	protected <T extends ModelBuilder<T>>
	T obj(String loc, ModelProvider<T> modelProvider)
	{
		Preconditions.checkArgument(loc.endsWith(".obj"));
		return obj(loc.substring(0, loc.length()-4), modLoc(loc), modelProvider);
	}

	protected <T extends ModelBuilder<T>>
	T obj(String name, ResourceLocation model, ModelProvider<T> provider)
	{
		return obj(name, model, ImmutableMap.of(), provider);
	}

	protected <T extends ModelBuilder<T>>
	T obj(String name, ResourceLocation model, Map<String, ResourceLocation> textures, ModelProvider<T> provider)
	{
		return obj(provider.withExistingParent(name, mcLoc("block")), model, textures);
	}

	protected <T extends ModelBuilder<T>>
	T obj(T base, ResourceLocation model, Map<String, ResourceLocation> textures)
	{
		assertModelExists(model);
		T ret = base
				.customLoader(ObjModelBuilder::begin)
				.automaticCulling(false)
				.modelLocation(addModelsPrefix(model))
				.flipV(true)
				.end();
		String particleTex = DataGenUtils.getTextureFromObj(model, existingFileHelper);
		if(particleTex.charAt(0)=='#')
			particleTex = textures.get(particleTex.substring(1)).toString();
		ret.texture("particle", particleTex);
		generatedParticleTextures.put(ret.getLocation(), particleTex);
		for(Entry<String, ResourceLocation> e : textures.entrySet())
			ret.texture(e.getKey(), e.getValue());
		return ret;
	}

	protected BlockModelBuilder splitModel(String name, NongeneratedModel model, List<Vec3i> parts, boolean dynamic)
	{
		BlockModelBuilder result = models().withExistingParent(name, mcLoc("block"))
				.customLoader(SplitModelBuilder::begin)
				.innerModel(model)
				.parts(parts)
				.dynamic(dynamic)
				.end();
		addParticleTextureFrom(result, model);
		return result;
	}

	protected ModelFile split(NongeneratedModel baseModel, List<Vec3i> parts, boolean dynamic)
	{
		return splitModel(baseModel.getLocation().getPath()+"_split", baseModel, parts, dynamic);
	}

	protected ModelFile split(NongeneratedModel baseModel, List<Vec3i> parts)
	{
		return split(baseModel, parts, false);
	}

	protected ModelFile splitDynamic(NongeneratedModel baseModel, List<Vec3i> parts)
	{
		return split(baseModel, parts, true);
	}

	protected void addParticleTextureFrom(BlockModelBuilder result, ModelFile model)
	{
		String particles = generatedParticleTextures.get(model.getLocation());
		if(particles!=null)
		{
			result.texture("particle", particles);
			generatedParticleTextures.put(result.getLocation(), particles);
		}
	}

	protected ConfiguredModel emptyWithParticles(String name, String particleTexture)
	{
		ModelFile model = models().withExistingParent(name, modLoc("block/ie_empty"))
				.texture("particle", particleTexture);
		generatedParticleTextures.put(modLoc(name), particleTexture);
		return new ConfiguredModel(model);
	}

	public void assertModelExists(ResourceLocation name)
	{
		String suffix = name.getPath().contains(".")?"": ".json";
		Preconditions.checkState(
				existingFileHelper.exists(name, PackType.CLIENT_RESOURCES, suffix, "models"),
				"Model \""+name+"\" does not exist");
	}

	protected IEOBJBuilder<BlockModelBuilder> ieObjBuilder(String loc)
	{
		return ieObjBuilder(getAutoNameIEOBJ(loc), modLoc(loc));
	}

	protected IEOBJBuilder<BlockModelBuilder> ieObjBuilder(String name, ResourceLocation model)
	{
		return ieObjBuilder(name, model, models());
	}

	protected <T extends ModelBuilder<T>>
	IEOBJBuilder<T> ieObjBuilder(String loc, ModelProvider<T> modelProvider)
	{
		return ieObjBuilder(getAutoNameIEOBJ(loc), modLoc(loc), modelProvider);
	}

	private static String getAutoNameIEOBJ(String loc)
	{
		Preconditions.checkArgument(loc.endsWith(".obj.ie"));
		return loc.substring(0, loc.length()-7);
	}

	protected <T extends ModelBuilder<T>>
	IEOBJBuilder<T> ieObjBuilder(String name, ResourceLocation model, ModelProvider<T> modelProvider)
	{
		final String particle = DataGenUtils.getTextureFromObj(model, existingFileHelper);
		generatedParticleTextures.put(modLoc(name), particle);
		return modelProvider.withExistingParent(name, mcLoc("block"))
				.texture("particle", particle)
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(addModelsPrefix(model));
	}

	protected <T extends ModelBuilder<T>> T mirror(NongeneratedModel inner, ModelProvider<T> provider)
	{
		return provider.getBuilder(inner.getLocation().getPath()+"_mirrored")
				.customLoader(MirroredModelBuilder::begin)
				.inner(inner)
				.end();
	}

	protected int getAngle(Direction dir, int offset)
	{
		return (int)((dir.toYRot()+offset)%360);
	}

	protected void createHorizontalRotatedBlock(Supplier<? extends Block> block, ModelFile model)
	{
		createHorizontalRotatedBlock(block, $ -> model, List.of());
	}

	protected void createHorizontalRotatedBlock(Supplier<? extends Block> block, ModelFile model, int offsetRotY)
	{
		createRotatedBlock(block, $ -> model, IEProperties.FACING_HORIZONTAL, List.of(), 0, offsetRotY);
	}

	protected void createHorizontalRotatedBlock(Supplier<? extends Block> block, Function<PartialBlockstate, ModelFile> model, List<Property<?>> additionalProps)
	{
		createRotatedBlock(block, model, IEProperties.FACING_HORIZONTAL, additionalProps, 0, 180);
	}

	protected void createAllRotatedBlock(Supplier<? extends Block> block, ModelFile model)
	{
		createAllRotatedBlock(block, $ -> model, List.of());
	}

	protected void createAllRotatedBlock(Supplier<? extends Block> block, Function<PartialBlockstate, ModelFile> model, List<Property<?>> additionalProps)
	{
		createRotatedBlock(block, model, IEProperties.FACING_ALL, additionalProps, 90, 0);
	}

	protected void createRotatedBlock(Supplier<? extends Block> block, ModelFile model, Property<Direction> facing,
									  List<Property<?>> additionalProps, int offsetRotX, int offsetRotY)
	{
		createRotatedBlock(block, $ -> model, facing, additionalProps, offsetRotX, offsetRotY);
	}

	protected void createRotatedBlock(Supplier<? extends Block> block, Function<PartialBlockstate, ModelFile> model, Property<Direction> facing,
									  List<Property<?>> additionalProps, int offsetRotX, int offsetRotY)
	{
		VariantBlockStateBuilder stateBuilder = getVariantBuilder(block.get());
		forEachState(stateBuilder.partialState(), additionalProps, state -> {
			ModelFile modelLoc = model.apply(state);
			for(Direction d : facing.getPossibleValues())
			{
				int x;
				int y;
				switch(d)
				{
					case UP ->
					{
						x = 90;
						y = 0;
					}
					case DOWN ->
					{
						x = -90;
						y = 0;
					}
					default ->
					{
						y = getAngle(d, offsetRotY);
						x = 0;
					}
				}
				state.with(facing, d).setModels(new ConfiguredModel(modelLoc, x+offsetRotX, y, false));
			}
		});
	}

	protected static String getName(RenderStateShard state)
	{
		//TODO clean up/speed up
		try
		{
			// Datagen should only ever run in a deobf environment, so no need to use unreadable SRG names here
			// This is a workaround for the fact that client-side Mixins are not applied in datagen
			Field f = RenderStateShard.class.getDeclaredField("name");
			f.setAccessible(true);
			return (String)f.get(state);
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}

	public static <T extends Comparable<T>> void forEach(PartialBlockstate base, Property<T> prop,
														 List<Property<?>> remaining, Consumer<PartialBlockstate> out)
	{
		for(T value : prop.getPossibleValues())
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
}
