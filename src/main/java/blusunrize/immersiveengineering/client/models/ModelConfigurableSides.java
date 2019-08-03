/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.client.ClientUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;

import static net.minecraft.util.Direction.*;

public class ModelConfigurableSides extends BakedIEModel
{
	private static final String MODEL_PREFIX = "conf_sides_";
	private static final String RESOURCE_LOCATION = "models/block/smartmodel/"+MODEL_PREFIX;
	//Holy shit, this type-chaining is messy. But I wanted to use lambdas!
	private static HashMap<String, ITextureNamer> TYPES = new HashMap<>();

	static
	{
		TYPES.put("all6_", new ITextureNamer()
		{
		});//every side seperately
		TYPES.put("s_", new ITextureNamer()
		{//all sides, same texture
			@Override
			public String nameFromSide(Direction side, SideConfig cfg)
			{
				return "side";
			}
		});
		TYPES.put("hud_", new ITextureNamer()
		{//horizontal, up, down
			@Override
			public String nameFromSide(Direction side, SideConfig cfg)
			{
				return side.ordinal() < 2?side.getName(): "side";
			}
		});
		TYPES.put("hv_", new ITextureNamer()
		{//horizontal, vertical
			@Override
			public String nameFromSide(Direction side, SideConfig cfg)
			{
				return side.ordinal() < 2?"up": "side";
			}
		});
		TYPES.put("ud_", new ITextureNamer()
		{//up, down, sides not configureable
			@Override
			public String nameFromSide(Direction side, SideConfig cfg)
			{
				return side.ordinal() < 2?side.getName(): "side";
			}

			@Override
			public String nameFromCfg(Direction side, SideConfig cfg)
			{
				return side.ordinal() < 2?cfg.getTextureName(): null;
			}
		});
		TYPES.put("v_", new ITextureNamer()
		{//vertical, sides not configureable
			@Override
			public String nameFromSide(Direction side, SideConfig cfg)
			{
				return side.ordinal() < 2?"up": "side";
			}

			@Override
			public String nameFromCfg(Direction side, SideConfig cfg)
			{
				return side.ordinal() < 2?cfg.getTextureName(): null;
			}
		});
	}

	public static Cache<ModelKey, List<BakedQuad>> modelCache = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.SECONDS).build();

	final String name;
	public Map<Direction, Map<SideConfig, TextureAtlasSprite>> textures;

	public ModelConfigurableSides(String name, Map<Direction, Map<SideConfig, TextureAtlasSprite>> textures)
	{
		this.name = name;
		this.textures = textures;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		Map<Direction, SideConfig> config;
		if(extraData.hasProperty(Model.SIDECONFIG))
			config = extraData.getData(Model.SIDECONFIG);
		else
		{
			config = new EnumMap<>(Direction.class);
			for(Direction d : Direction.VALUES)
				config.put(d, SideConfig.NONE);
		}
		assert (config!=null);
		ModelKey key = new ModelKey(name, config);
		try
		{
			return modelCache.get(key, () -> {
				Map<Direction, TextureAtlasSprite> tex = new EnumMap<>(Direction.class);
				for(Direction d : Direction.VALUES)
					tex.put(d, this.textures.get(d).get(config.get(d)));
				return bakeQuads(tex);
			});
		} catch(ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static List<BakedQuad> bakeQuads(Map<Direction, TextureAtlasSprite> sprites)
	{
		List<BakedQuad> quads = Lists.newArrayListWithExpectedSize(6);
		float[] colour = {1, 1, 1, 1};
		Vec3d[] vertices = {new Vec3d(0, 0, 0), new Vec3d(0, 0, 1), new Vec3d(1, 0, 1), new Vec3d(1, 0, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, DOWN, sprites.get(DOWN), new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vec3d[]{new Vec3d(0, 1, 0), new Vec3d(0, 1, 1), new Vec3d(1, 1, 1), new Vec3d(1, 1, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, UP, sprites.get(UP), new double[]{0, 0, 16, 16}, colour, false));

		vertices = new Vec3d[]{new Vec3d(1, 0, 0), new Vec3d(1, 1, 0), new Vec3d(0, 1, 0), new Vec3d(0, 0, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, NORTH, sprites.get(NORTH), new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vec3d[]{new Vec3d(1, 0, 1), new Vec3d(1, 1, 1), new Vec3d(0, 1, 1), new Vec3d(0, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, SOUTH, sprites.get(SOUTH), new double[]{16, 16, 0, 0}, colour, false));

		vertices = new Vec3d[]{new Vec3d(0, 0, 0), new Vec3d(0, 1, 0), new Vec3d(0, 1, 1), new Vec3d(0, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, WEST, sprites.get(WEST), new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vec3d[]{new Vec3d(1, 0, 0), new Vec3d(1, 1, 0), new Vec3d(1, 1, 1), new Vec3d(1, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, EAST, sprites.get(EAST), new double[]{16, 16, 0, 0}, colour, false));
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

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return this.textures.get(DOWN).get(SideConfig.NONE);
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

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return defaultTransforms;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.EMPTY;
	}

	public static class Loader implements ICustomModelLoader
	{

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager)
		{
			modelCache.invalidateAll();
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate)
		{
			if(resourcePredicate.test(VanillaResourceType.MODELS)||resourcePredicate.test(VanillaResourceType.TEXTURES))
				onResourceManagerReload(resourceManager);
		}

		@Override
		public boolean accepts(ResourceLocation modelLocation)
		{
			return modelLocation.getPath().contains(RESOURCE_LOCATION);
		}

		@Override
		public IUnbakedModel loadModel(ResourceLocation modelLocation)
		{
			String resourcePath = modelLocation.getPath();
			int pos = resourcePath.indexOf(MODEL_PREFIX);
			if(pos >= 0)
			{
				pos += MODEL_PREFIX.length();
				String sub = resourcePath.substring(pos);
				String name = sub;
				String type = null;
				ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builder();
				for(Entry<String, ITextureNamer> e : TYPES.entrySet())
					if(sub.startsWith(e.getKey()))
					{
						type = e.getKey();
						name = sub.substring(type.length());
						for(Direction f : Direction.VALUES)
							for(SideConfig cfg : SideConfig.values())
							{
								String key = f.getName()+"_"+cfg.getTextureName();
								String tex = name+"_"+e.getValue().getTextureName(f, cfg);
								builder.put(key, new ResourceLocation(ImmersiveEngineering.MODID, "blocks/"+tex));
							}
					}
				return new ConfigSidesModelBase(name, type, builder.build());
			}
			return ModelLoaderRegistry.getMissingModel();
		}
	}

	private static class ConfigSidesModelBase implements IUnbakedModel
	{
		final String name;
		final String type;
		ImmutableMap<String, ResourceLocation> textures;

		public ConfigSidesModelBase(String name, String type, ImmutableMap<String, ResourceLocation> textures)
		{
			this.name = name;
			this.type = type;
			this.textures = textures;
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			return ImmutableList.of();
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getTextures(@Nonnull Function<ResourceLocation, IUnbakedModel> modelGetter,
														@Nonnull Set<String> missingTextureErrors)
		{
			return textures.values();
		}

		@Nullable
		@Override
		public IBakedModel bake(ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format)
		{
			Map<Direction, Map<SideConfig, TextureAtlasSprite>> tex = new EnumMap<>(Direction.class);
			for(Direction f : Direction.VALUES)
			{
				Map<SideConfig, TextureAtlasSprite> forSide = new EnumMap<>(SideConfig.class);
				for(SideConfig cfg : SideConfig.values())
				{
					ResourceLocation rl = textures.get(f.getName()+"_"+cfg.getTextureName());
					if(rl!=null)
						forSide.put(cfg, spriteGetter.apply(rl));
				}
				tex.put(f, forSide);
			}
			return new ModelConfigurableSides(name, tex);
		}

		@Override
		public IUnbakedModel retexture(ImmutableMap<String, String> textures)
		{
			String newName = this.name;
			ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builder();
			for(Direction f : Direction.VALUES)
				for(SideConfig cfg : SideConfig.values())
				{
					String key = f.getName()+"_"+cfg.getTextureName();
					ResourceLocation rl = this.textures.get(key);
					if(textures.containsKey(key))
						rl = new ResourceLocation(textures.get(key));
					else if(textures.containsKey(f.getName()))
					{
						ITextureNamer namer = TYPES.get(type);
						rl = new ResourceLocation(textures.get(f.getName()));
						if(namer!=null)
						{
							String c = namer.nameFromCfg(f, cfg);
							if(c!=null)
								rl = new ResourceLocation(textures.get(f.getName())+"_"+c);
						}
					}
					else if(textures.containsKey("name"))
					{
						ITextureNamer namer = TYPES.get(type);
						newName = textures.get("name");
						if(namer!=null)
							rl = new ResourceLocation(newName+"_"+namer.getTextureName(f, cfg));
					}
					builder.put(key, rl);
				}
			return new ConfigSidesModelBase(newName, type, builder.build());
		}
	}

	interface ITextureNamer
	{
		default String getTextureName(Direction side, SideConfig cfg)
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

		default String nameFromSide(Direction side, SideConfig cfg)
		{
			return side.getName();
		}

		default String nameFromCfg(Direction side, SideConfig cfg)
		{
			return cfg.getTextureName();
		}
	}

	private static class ModelKey
	{
		@Nonnull
		final String name;
		@Nonnull
		final Map<Direction, SideConfig> config;

		private ModelKey(String name, Map<Direction, SideConfig> config)
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
