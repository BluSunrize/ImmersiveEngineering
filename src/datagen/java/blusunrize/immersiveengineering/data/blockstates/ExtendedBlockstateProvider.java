/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEStairsBlock;
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
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.*;
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

	protected void setRenderType(@Nullable RenderType type, ModelBuilder<?>... builders)
	{
		if(type!=null)
		{
			final String typeName = ModelProviderUtils.getName(type);
			for(final ModelBuilder<?> model : builders)
				model.renderType(typeName);
		}
	}

	protected ResourceLocation forgeLoc(String path)
	{
		return new ResourceLocation("forge", path);
	}

	protected ResourceLocation addModelsPrefix(ResourceLocation in)
	{
		return new ResourceLocation(in.getNamespace(), "models/"+in.getPath());
	}

	protected void itemModel(Supplier<? extends Block> block, ModelFile model)
	{
		itemModels().getBuilder(name(block)).parent(model);
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

	protected void createHorizontalRotatedBlock(Supplier<? extends Block> block, ModelFile model, int offsetRotY) {
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
					case UP -> {
						x = 90;
						y = 0;
					}
					case DOWN -> {
						x = -90;
						y = 0;
					}
					default -> {
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
