/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEEnums;
import blusunrize.immersiveengineering.api.IEEnums.SideConfig;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.property.IExtendedBlockState;
import org.lwjgl.util.vector.Vector3f;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

public class ModelConfigurableSides implements IBakedModel
{
	private static final String MODEL_PREFIX = "conf_sides_";
	private static final String RESOURCE_LOCATION = "models/block/smartmodel/"+MODEL_PREFIX;
	//Holy shit, this type-chaining is messy. But I wanted to use lambdas!
	private static HashMap<String, ITextureNamer> TYPES = new HashMap();

	static
	{
		TYPES.put("all6_", new ITextureNamer()
		{
		});//every side seperately
		TYPES.put("s_", new ITextureNamer()
		{//all sides, same texture
			@Override
			public String nameFromSide(EnumFacing side, SideConfig cfg)
			{
				return "side";
			}
		});
		TYPES.put("hud_", new ITextureNamer()
		{//horizontal, up, down
			@Override
			public String nameFromSide(EnumFacing side, SideConfig cfg)
			{
				return side.ordinal() < 2?side.getName(): "side";
			}
		});
		TYPES.put("hv_", new ITextureNamer()
		{//horizontal, vertical
			@Override
			public String nameFromSide(EnumFacing side, SideConfig cfg)
			{
				return side.ordinal() < 2?"up": "side";
			}
		});
		TYPES.put("ud_", new ITextureNamer()
		{//up, down, sides not configureable
			@Override
			public String nameFromSide(EnumFacing side, SideConfig cfg)
			{
				return side.ordinal() < 2?side.getName(): "side";
			}

			@Override
			public String nameFromCfg(EnumFacing side, SideConfig cfg)
			{
				return side.ordinal() < 2?cfg.getTextureName(): null;
			}
		});
		TYPES.put("v_", new ITextureNamer()
		{//vertical, sides not configureable
			@Override
			public String nameFromSide(EnumFacing side, SideConfig cfg)
			{
				return side.ordinal() < 2?"up": "side";
			}

			@Override
			public String nameFromCfg(EnumFacing side, SideConfig cfg)
			{
				return side.ordinal() < 2?cfg.getTextureName(): null;
			}
		});
	}

	public static HashMap<String, List<BakedQuad>> modelCache = new HashMap<>();

	final String name;
	public TextureAtlasSprite[][] textures;

	public ModelConfigurableSides(String name, TextureAtlasSprite[][] textures)
	{
		this.name = name;
		this.textures = textures;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		TextureAtlasSprite[] tex = new TextureAtlasSprite[6];
		for(int i = 0; i < tex.length; i++)
			tex[i] = this.textures[i][0];
		char[] keyArray = "000000".toCharArray();
		if(state instanceof IExtendedBlockState)
		{
			IExtendedBlockState extended = (IExtendedBlockState)state;
			for(int i = 0; i < IEProperties.SIDECONFIG.length; i++)
				if(extended.getUnlistedNames().contains(IEProperties.SIDECONFIG[i]))
				{
					IEEnums.SideConfig config = extended.getValue(IEProperties.SIDECONFIG[i]);
					if(config!=null)
					{
						int c = config.ordinal();
						tex[i] = this.textures[i][c];
						keyArray[i] = Character.forDigit(c, 10);
					}
				}
		}
		String key = name+String.copyValueOf(keyArray);
		if(!modelCache.containsKey(key))
			modelCache.put(key, bakeQuads(tex));
		return modelCache.get(key);
	}

	private static List<BakedQuad> bakeQuads(TextureAtlasSprite[] sprites)
	{
		List<BakedQuad> quads = Lists.newArrayListWithExpectedSize(6);
		float[] colour = {1, 1, 1, 1};
		Vector3f[] vertices = {new Vector3f(0, 0, 0), new Vector3f(0, 0, 1), new Vector3f(1, 0, 1), new Vector3f(1, 0, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.DOWN, sprites[0], new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vector3f[]{new Vector3f(0, 1, 0), new Vector3f(0, 1, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.UP, sprites[1], new double[]{0, 0, 16, 16}, colour, false));

		vertices = new Vector3f[]{new Vector3f(1, 0, 0), new Vector3f(1, 1, 0), new Vector3f(0, 1, 0), new Vector3f(0, 0, 0)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.NORTH, sprites[2], new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vector3f[]{new Vector3f(1, 0, 1), new Vector3f(1, 1, 1), new Vector3f(0, 1, 1), new Vector3f(0, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.SOUTH, sprites[3], new double[]{16, 16, 0, 0}, colour, false));

		vertices = new Vector3f[]{new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), new Vector3f(0, 1, 1), new Vector3f(0, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.WEST, sprites[4], new double[]{0, 16, 16, 0}, colour, true));
		vertices = new Vector3f[]{new Vector3f(1, 0, 0), new Vector3f(1, 1, 0), new Vector3f(1, 1, 1), new Vector3f(1, 0, 1)};
		quads.add(ClientUtils.createBakedQuad(DefaultVertexFormats.ITEM, vertices, EnumFacing.EAST, sprites[5], new double[]{16, 16, 0, 0}, colour, false));
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
		return this.textures[0][0];
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
		return ItemOverrideList.NONE;
	}

	public static class Loader implements ICustomModelLoader
	{
		@Override
		public void onResourceManagerReload(IResourceManager resourceManager)
		{
			modelCache.clear();
		}

		@Override
		public boolean accepts(ResourceLocation modelLocation)
		{
			return modelLocation.getPath().contains(RESOURCE_LOCATION);
		}

		@Override
		public IModel loadModel(ResourceLocation modelLocation)
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
						for(EnumFacing f : EnumFacing.VALUES)
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

	private static class ConfigSidesModelBase implements IModel
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

		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			return ImmutableList.of();
		}

		@Override
		public Collection<ResourceLocation> getTextures()
		{
			return textures.values();
		}

		@Override
		public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
		{
			TextureAtlasSprite[][] tex = new TextureAtlasSprite[6][3];
			for(EnumFacing f : EnumFacing.VALUES)
				for(SideConfig cfg : SideConfig.values())
				{
					ResourceLocation rl = textures.get(f.getName()+"_"+cfg.getTextureName());
					if(rl!=null)
						tex[f.ordinal()][cfg.ordinal()] = ApiUtils.getRegisterSprite(ClientUtils.mc().getTextureMapBlocks(), rl);
				}
			return new ModelConfigurableSides(name, tex);
		}

		@Override
		public IModel retexture(ImmutableMap<String, String> textures)
		{
			String newName = this.name;
			ImmutableMap.Builder<String, ResourceLocation> builder = ImmutableMap.builder();
			for(EnumFacing f : EnumFacing.VALUES)
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
		default String getTextureName(EnumFacing side, SideConfig cfg)
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

		default String nameFromSide(EnumFacing side, SideConfig cfg)
		{
			return side.getName();
		}

		default String nameFromCfg(EnumFacing side, SideConfig cfg)
		{
			return cfg.getTextureName();
		}
	}
}
