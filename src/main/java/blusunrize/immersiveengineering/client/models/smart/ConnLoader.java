/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.smart;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class ConnLoader implements ICustomModelLoader
{
	public static final String RESOURCE_LOCATION = "models/block/smartmodel/conn_";
	public static final ResourceLocation DATA_BASED_LOC = new ResourceLocation(ImmersiveEngineering.MODID, "models/block/smartmodel/connector");
	//Do not manually write to these in IE 0.12-77+. Use blusunrize.immersiveengineering.api.energy.wires.WireApi.registerConnectorForRender()
	public static Map<String, ImmutableMap<String, String>> textureReplacements = new HashMap<>();
	public static Map<String, ResourceLocation> baseModels = new HashMap<>();

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
		ConnModelReal.cache.invalidateAll();
	}

	@Override
	public boolean accepts(@Nonnull ResourceLocation modelLocation)
	{
		return modelLocation.equals(DATA_BASED_LOC)||modelLocation.getResourcePath().contains(RESOURCE_LOCATION);
	}

	@Nonnull
	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation)
	{
		if(modelLocation.equals(DATA_BASED_LOC))
			return new ConnModelBase(null);
		String resourcePath = modelLocation.getResourcePath();
		int pos = resourcePath.indexOf("conn_");
		if(pos >= 0)
		{
			pos += 5;// length of "conn_"
			String name = resourcePath.substring(pos);
			ResourceLocation r = baseModels.get(name);
			if(r!=null)
			{
				if(textureReplacements.containsKey(name))
					return new ConnModelBase(r, textureReplacements.get(name), ImmutableMap.of());
				else
					return new ConnModelBase(r);
			}
		}
		return ModelLoaderRegistry.getMissingModel();
	}

	private static class ConnModelBase implements IModel
	{
		private static final ResourceLocation WIRE_LOC = new ResourceLocation(ImmersiveEngineering.MODID.toLowerCase(Locale.ENGLISH)+":blocks/wire");
		@Nullable
		final ResourceLocation base;
		@Nonnull
		final ImmutableMap<String, String> texReplace;
		@Nonnull
		final ImmutableMap<String, String> customBase;
		@Nullable
		private IModel baseModel;

		public ConnModelBase(@Nullable ResourceLocation b, @Nonnull ImmutableMap<String, String> t,
							 @Nonnull ImmutableMap<String, String> customBase)
		{
			base = b;
			texReplace = t;
			this.customBase = customBase;
		}

		public ConnModelBase(@Nullable ResourceLocation b)
		{
			this(b, ImmutableMap.of(), ImmutableMap.of());
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			if(base==null)
				return ImmutableList.of();
			attemptToLoadBase(false);
			if(baseModel!=null)
			{
				List<ResourceLocation> ret = new ArrayList<>(baseModel.getDependencies());
				ret.add(0, base);
				return ret;
			}
			else
				return ImmutableList.of(base);
		}

		private void attemptToLoadBase(boolean throwError)
		{
			try
			{
				assert base!=null;
				baseModel = ModelLoaderRegistry.getModel(base);
				baseModel = baseModel.retexture(texReplace).process(customBase);
			} catch(Exception e)
			{
				if(throwError)
					throw new RuntimeException(e);
			}
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getTextures()
		{
			if(base==null)
				return ImmutableList.of();
			attemptToLoadBase(false);
			if(baseModel!=null)
			{
				List<ResourceLocation> ret = new ArrayList<>(baseModel.getTextures());
				ret.add(WIRE_LOC);
				return ret;
			}
			else
				return ImmutableList.of(WIRE_LOC);
		}

		@Nonnull
		@Override
		public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
		{
			attemptToLoadBase(true);
			assert baseModel!=null;
			return new ConnModelReal(baseModel.bake(state, format, bakedTextureGetter));
		}

		private static final ImmutableSet<String> ownKeys = ImmutableSet.of("base", "custom", "textures");

		@Nonnull
		@Override
		public IModel process(ImmutableMap<String, String> customData)
		{
			String baseLocStr = customData.get("base");
			ResourceLocation baseLoc = this.base;
			if(baseLocStr!=null)
				baseLoc = new ResourceLocation(asString(baseLocStr));
			ImmutableMap<String, String> customBase = ImmutableMap.of();
			ImmutableMap<String, String> texReplacements = ImmutableMap.of();
			if(customData.containsKey("custom"))
			{
				JsonObject obj = new JsonParser().parse(customData.get("custom")).getAsJsonObject();
				customBase = asMap(obj);
			}
			ImmutableMap.Builder<String, String> leftoverKeys = ImmutableMap.builder();
			for(Entry<String, String> e : customData.entrySet())
				if(!ownKeys.contains(e.getKey()))
					leftoverKeys.put(e);
			leftoverKeys.putAll(customBase);
			customBase = leftoverKeys.build();
			if(customData.containsKey("textures"))
			{
				JsonObject obj = new JsonParser().parse(customData.get("textures")).getAsJsonObject();
				texReplacements = asMap(obj);
			}
			if(!(baseLoc==base||baseLoc.equals(base))||!customBase.equals(this.customBase)
					||!texReplacements.equals(this.texReplace))
				return new ConnModelBase(baseLoc, texReplacements, customBase);
			return this;
		}

		private ImmutableMap<String, String> asMap(JsonObject obj)
		{
			ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
			for(Entry<String, JsonElement> e : obj.entrySet())
			{
				JsonElement val = e.getValue();
				if(val.isJsonPrimitive()&&val.getAsJsonPrimitive().isString())
					b.put(e.getKey(), val.getAsString());
				else
					b.put(e.getKey(), val.toString());
			}
			return b.build();
		}

		private String asString(String json)
		{
			return new JsonParser().parse(json).getAsString();
		}
	}
}
