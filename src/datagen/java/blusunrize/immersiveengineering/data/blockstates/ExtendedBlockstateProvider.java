package blusunrize.immersiveengineering.data.blockstates;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.blocks.IEBlocks;
import blusunrize.immersiveengineering.data.DataGenUtils;
import blusunrize.immersiveengineering.data.models.IEOBJBuilder;
import blusunrize.immersiveengineering.data.models.SplitModelBuilder;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.client.renderer.RenderState;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourcePackType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.client.model.generators.loaders.OBJLoaderBuilder;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class ExtendedBlockstateProvider extends BlockStateProvider
{
	protected static final List<Vector3i> COLUMN_THREE = ImmutableList.of(BlockPos.ZERO, BlockPos.ZERO.up(), BlockPos.ZERO.up(2));

	protected static final Map<ResourceLocation, String> generatedParticleTextures = new HashMap<>();
	protected final ExistingFileHelper existingFileHelper;

	public ExtendedBlockstateProvider(DataGenerator gen, ExistingFileHelper exFileHelper)
	{
		super(gen, Lib.MODID, exFileHelper);
		this.existingFileHelper = exFileHelper;
	}


	protected String name(Block b)
	{
		return b.getRegistryName().getPath();
	}

	public void simpleBlockItem(Block b, ModelFile model)
	{
		simpleBlockItem(b, new ConfiguredModel(model));
	}

	protected void simpleBlockItem(Block b, ConfiguredModel model)
	{
		simpleBlock(b, model);
		itemModel(b, model.model);
	}

	protected void cubeSideVertical(Block b, ResourceLocation side, ResourceLocation vertical)
	{
		simpleBlockItem(b, models().cubeBottomTop(name(b), side, vertical, vertical));
	}

	protected void cubeAll(Block b, ResourceLocation texture)
	{
		simpleBlockItem(b, models().cubeAll(name(b), texture));
	}

	protected void scaffold(Block b, ResourceLocation others, ResourceLocation top)
	{
		simpleBlockItem(
				b,
				models().withExistingParent(name(b), modLoc("block/ie_scaffolding"))
						.texture("side", others)
						.texture("bottom", others)
						.texture("top", top)
		);
	}

	protected void slabFor(Block b, ResourceLocation texture)
	{
		slabFor(b, texture, texture, texture);
	}

	protected void slabFor(Block b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
	{
		slab(IEBlocks.toSlab.get(b), side, top, bottom);
	}

	protected void slab(SlabBlock b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
	{
		ModelFile mainModel = models().slab(name(b)+"_bottom", side, bottom, top);
		slabBlock(
				b, mainModel,
				models().slabTop(name(b)+"_top", side, bottom, top),
				models().cubeBottomTop(name(b)+"_double", side, bottom, top)
		);
		itemModel(b, mainModel);
	}

	protected void stairs(StairsBlock b, ResourceLocation texture)
	{
		stairs(b, texture, texture, texture);
	}

	protected void stairs(StairsBlock b, ResourceLocation side, ResourceLocation top, ResourceLocation bottom)
	{
		String baseName = name(b);
		ModelFile stairs = models().stairs(baseName, side, bottom, top);
		ModelFile stairsInner = models().stairsInner(baseName+"_inner", side, bottom, top);
		ModelFile stairsOuter = models().stairsOuter(baseName+"_outer", side, bottom, top);
		stairsBlock(b, stairs, stairsInner, stairsOuter);
		itemModel(b, stairs);
	}

	protected ResourceLocation forgeLoc(String path)
	{
		return new ResourceLocation("forge", path);
	}

	protected ResourceLocation addModelsPrefix(ResourceLocation in)
	{
		return new ResourceLocation(in.getNamespace(), "models/"+in.getPath());
	}

	protected void itemModel(Block block, ModelFile model)
	{
		itemModels().getBuilder(name(block)).parent(model);
	}

	protected BlockModelBuilder obj(String loc)
	{
		Preconditions.checkArgument(loc.endsWith(".obj"));
		return obj(loc.substring(0, loc.length()-4), modLoc(loc));
	}

	protected BlockModelBuilder obj(String name, ResourceLocation model)
	{
		return obj(name, model, ImmutableMap.of());
	}

	protected BlockModelBuilder obj(String name, ResourceLocation model, Map<String, ResourceLocation> textures)
	{
		assertModelExists(model);
		BlockModelBuilder ret = models().withExistingParent(name, mcLoc("block"))
				.customLoader(OBJLoaderBuilder::begin)
				.detectCullableFaces(false)
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

	protected BlockModelBuilder splitModel(String name, ModelFile model, List<Vector3i> parts, boolean dynamic)
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

	protected ModelFile split(ModelFile baseModel, List<Vector3i> parts, boolean dynamic)
	{
		return splitModel(baseModel.getLocation().getPath()+"_split", baseModel, parts, dynamic);
	}

	protected ModelFile split(ModelFile baseModel, List<Vector3i> parts)
	{
		return split(baseModel, parts, false);
	}

	protected ModelFile splitDynamic(ModelFile baseModel, List<Vector3i> parts)
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
				existingFileHelper.exists(name, ResourcePackType.CLIENT_RESOURCES, suffix, "models"),
				"Model \""+name+"\" does not exist");
	}

	protected BlockModelBuilder ieObj(String loc)
	{
		Preconditions.checkArgument(loc.endsWith(".obj.ie"));
		return ieObj(loc.substring(0, loc.length()-7), modLoc(loc));
	}

	protected BlockModelBuilder ieObj(String name, ResourceLocation model)
	{
		final String particle = DataGenUtils.getTextureFromObj(model, existingFileHelper);
		generatedParticleTextures.put(modLoc(name), particle);
		return models().withExistingParent(name, mcLoc("block"))
				.customLoader(IEOBJBuilder::begin)
				.modelLocation(addModelsPrefix(model))
				.flipV(true)
				.end()
				.texture("particle", particle);
	}

	protected int getAngle(Direction dir, int offset)
	{
		return (int)((dir.getHorizontalAngle()+offset)%360);
	}

	protected static String getName(RenderState state)
	{
		//TODO clean up/speed up
		try
		{
			// Datagen should only ever run in a deobf environment, so no need to use unreadable SRG names here
			// This is a workaround for the fact that client-side Mixins are not applied in datagen
			Field f = RenderState.class.getDeclaredField("name");
			f.setAccessible(true);
			return (String)f.get(state);
		} catch(Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
