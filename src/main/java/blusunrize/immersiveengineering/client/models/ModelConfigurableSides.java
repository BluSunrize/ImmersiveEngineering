/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.utils.client.CombinedModelData;
import blusunrize.immersiveengineering.api.utils.client.SinglePropertyModelData;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IConfigurableSides;
import blusunrize.immersiveengineering.common.util.DirectionUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.geometry.IModelGeometry;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraft.util.Direction.*;

public class ModelConfigurableSides extends BakedIEModel
{
	private static HashMap<String, ITextureNamer> TYPES = new HashMap<>();

	static
	{
		TYPES.put("side_top_bottom", new ITextureNamer()
		{//horizontal, up, down
			@Override
			public String nameFromSide(Direction side, IOSideConfig cfg)
			{
				return side.getAxis()==Axis.Y?side.getString(): "side";
			}
		});
		TYPES.put("side_vertical", new ITextureNamer()
		{//horizontal, vertical
			@Override
			public String nameFromSide(Direction side, IOSideConfig cfg)
			{
				return side.getAxis()==Axis.Y?"up": "side";
			}
		});
		TYPES.put("vertical", new ITextureNamer()
		{//vertical, sides not configureable
			@Override
			public String nameFromSide(Direction side, IOSideConfig cfg)
			{
				return side.getAxis()==Axis.Y?"up": "side";
			}

			@Override
			public String nameFromCfg(Direction side, IOSideConfig cfg)
			{
				return side.getAxis()==Axis.Y?cfg.getTextureName(): null;
			}
		});
		TYPES.put("all_same_texture", new ITextureNamer()
		{//all sides, same texture
			@Override
			public String nameFromSide(Direction side, IOSideConfig cfg)
			{
				return "side";
			}
		});
	}

	public static Cache<ModelKey, List<BakedQuad>> modelCache = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build();

	final String name;
	public Map<Direction, Map<IOSideConfig, TextureAtlasSprite>> textures;

	public ModelConfigurableSides(String name, Map<Direction, Map<IOSideConfig, TextureAtlasSprite>> textures)
	{
		this.name = name;
		this.textures = textures;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		Map<Direction, IOSideConfig> config;
		if(extraData.hasProperty(Model.SIDECONFIG))
			config = extraData.getData(Model.SIDECONFIG);
		else
		{
			config = new EnumMap<>(Direction.class);
			for(Direction d : DirectionUtils.VALUES)
				config.put(d, IOSideConfig.NONE);
		}
		assert (config!=null);
		ModelKey key = new ModelKey(name, config);
		try
		{
			return modelCache.get(key, () -> {
				Map<Direction, TextureAtlasSprite> tex = new EnumMap<>(Direction.class);
				for(Direction d : DirectionUtils.VALUES)
					tex.put(d, this.textures.get(d).get(config.get(d)));
				return bakeQuads(tex);
			});
		} catch(ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		List<IModelData> data = new ArrayList<>();
		data.add(tileData);
		data.add(super.getModelData(world, pos, state, tileData));
		TileEntity te = world.getTileEntity(pos);
		if(te instanceof IConfigurableSides)
		{
			IConfigurableSides confTE = (IConfigurableSides)te;
			Map<Direction, IOSideConfig> conf = new HashMap<>();
			for(Direction d : DirectionUtils.VALUES)
				conf.put(d, confTE.getSideConfig(d));
			data.add(new SinglePropertyModelData<>(conf, Model.SIDECONFIG));
		}
		return CombinedModelData.combine(data.toArray(new IModelData[0]));
	}

	private static List<BakedQuad> bakeQuads(Map<Direction, TextureAtlasSprite> sprites)
	{
		List<BakedQuad> quads = Lists.newArrayListWithExpectedSize(6);
		float[] colour = {1, 1, 1, 1};
		Vector3d[] vertices = {new Vector3d(0, 0, 0), new Vector3d(0, 0, 1), new Vector3d(1, 0, 1), new Vector3d(1, 0, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, vertices, DOWN, sprites.get(DOWN), new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vector3d[]{new Vector3d(0, 1, 0), new Vector3d(0, 1, 1), new Vector3d(1, 1, 1), new Vector3d(1, 1, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, vertices, UP, sprites.get(UP), new double[]{0, 0, 16, 16}, colour, false));

		vertices = new Vector3d[]{new Vector3d(1, 0, 0), new Vector3d(1, 1, 0), new Vector3d(0, 1, 0), new Vector3d(0, 0, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, vertices, NORTH, sprites.get(NORTH), new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vector3d[]{new Vector3d(1, 0, 1), new Vector3d(1, 1, 1), new Vector3d(0, 1, 1), new Vector3d(0, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, vertices, SOUTH, sprites.get(SOUTH), new double[]{16, 16, 0, 0}, colour, false));

		vertices = new Vector3d[]{new Vector3d(0, 0, 0), new Vector3d(0, 1, 0), new Vector3d(0, 1, 1), new Vector3d(0, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, vertices, WEST, sprites.get(WEST), new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vector3d[]{new Vector3d(1, 0, 0), new Vector3d(1, 1, 0), new Vector3d(1, 1, 1), new Vector3d(1, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.BLOCK, vertices, EAST, sprites.get(EAST), new double[]{16, 16, 0, 0}, colour, false));
		return quads;
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return true;
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.textures.get(DOWN).get(IOSideConfig.NONE);
	}

	static final ItemCameraTransforms defaultTransforms = new ItemCameraTransforms(
			new ItemTransformVec3f(new Vector3f(75, 45, 0), new Vector3f(0, .25f, 0), new Vector3f(0.375f, 0.375f, 0.375f)), //thirdperson left
			new ItemTransformVec3f(new Vector3f(75, 45, 0), new Vector3f(0, .15625f, 0), new Vector3f(0.375f, 0.375f, 0.375f)), //thirdperson left

			new ItemTransformVec3f(new Vector3f(0, 45, 0), new Vector3f(0, 0, 0), new Vector3f(.4f, .4f, .4f)), //firstperson left
			new ItemTransformVec3f(new Vector3f(0, 225, 0), new Vector3f(0, 0, 0), new Vector3f(.4f, .4f, .4f)), //firstperson right

			new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)), //head
			new ItemTransformVec3f(new Vector3f(30, 225, 0), new Vector3f(0, 0, 0), new Vector3f(.625f, .625f, .625f)), //gui
			new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0, .1875f, 0), new Vector3f(.25f, .25f, .25f)), //ground
			new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0), new Vector3f(.5f, .5f, .5f))); //fixed

	@Nonnull
	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return defaultTransforms;
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.EMPTY;
	}

	public static class Loader implements IModelLoader<ConfigSidesModelBase>
	{
		public static ResourceLocation NAME = new ResourceLocation(ImmersiveEngineering.MODID, "conf_sides");

		@Override
		public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
		{
			modelCache.invalidateAll();
		}

		@Override
		public void onResourceManagerReload(@Nonnull IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
		{
			if(resourcePredicate.test(VanillaResourceType.MODELS)||resourcePredicate.test(VanillaResourceType.TEXTURES))
				onResourceManagerReload(resourceManager);
		}

		@Nonnull
		@Override
		public ConfigSidesModelBase read(@Nonnull JsonDeserializationContext deserializationContext, JsonObject modelContents)
		{
			final String name = modelContents.get("base_name").getAsString();
			final String type = modelContents.get("type").getAsString();
			ImmutableMap.Builder<String, RenderMaterial> builder = ImmutableMap.builder();
			ITextureNamer namer = TYPES.get(type);
			for(Direction f : DirectionUtils.VALUES)
				for(IOSideConfig cfg : IOSideConfig.values())
				{
					String key = f.getString()+"_"+cfg.getTextureName();
					String tex = name+"_"+namer.getTextureName(f, cfg);
					builder.put(key, new RenderMaterial(PlayerContainer.LOCATION_BLOCKS_TEXTURE, new ResourceLocation(tex)));
				}
			return new ConfigSidesModelBase(name, type, builder.build());
		}
	}

	private static class ConfigSidesModelBase implements IModelGeometry<ConfigSidesModelBase>
	{
		final String name;
		final String type;
		Map<String, RenderMaterial> textures;

		public ConfigSidesModelBase(String name, String type, Map<String, RenderMaterial> textures)
		{
			this.name = name;
			this.type = type;
			this.textures = textures;
		}

		@Override
		public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<RenderMaterial, TextureAtlasSprite> spriteGetter, IModelTransform modelTransform, ItemOverrideList overrides, ResourceLocation modelLocation)
		{
			Map<Direction, Map<IOSideConfig, TextureAtlasSprite>> tex = new EnumMap<>(Direction.class);
			for(Direction f : DirectionUtils.VALUES)
			{
				Map<IOSideConfig, TextureAtlasSprite> forSide = new EnumMap<>(IOSideConfig.class);
				for(IOSideConfig cfg : IOSideConfig.values())
				{
					RenderMaterial rl = textures.get(f.getString()+"_"+cfg.getTextureName());
					if(rl!=null)
						forSide.put(cfg, spriteGetter.apply(rl));
				}
				tex.put(f, forSide);
			}
			return new ModelConfigurableSides(name, tex);
		}

		@Override
		public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
		{
			return textures.values();
		}
	}

	interface ITextureNamer
	{
		default String getTextureName(Direction side, IOSideConfig cfg)
		{
			String s = nameFromSide(side, cfg);
			String c = nameFromCfg(side, cfg);
			if(s!=null&&c!=null)
				return s+"_"+c;
			else if(s!=null)
				return s;
			else if(c!=null)
				return c;
			return "";
		}

		default String nameFromSide(Direction side, IOSideConfig cfg)
		{
			return side.getString();
		}

		default String nameFromCfg(Direction side, IOSideConfig cfg)
		{
			return cfg.getTextureName();
		}
	}

	private static class ModelKey
	{
		@Nonnull
		final String name;
		@Nonnull
		final Map<Direction, IOSideConfig> config;

		private ModelKey(@Nonnull String name, @Nonnull Map<Direction, IOSideConfig> config)
		{
			this.name = name;
			this.config = config;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;
			ModelKey modelKey = (ModelKey)o;
			return name.equals(modelKey.name)&&
					config.equals(modelKey.config);
		}

		@Override
		public int hashCode()
		{
			return Objects.hash(name, config);
		}
	}
}
