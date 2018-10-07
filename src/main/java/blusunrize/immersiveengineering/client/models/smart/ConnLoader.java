/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.smart;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.ModelData;
import blusunrize.immersiveengineering.client.models.multilayer.MultiLayerModel;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class ConnLoader implements ICustomModelLoader
{
	public static final String RESOURCE_LOCATION = "models/block/smartmodel/conn_";
	public static final ResourceLocation DATA_BASED_LOC = new ResourceLocation(ImmersiveEngineering.MODID, "models/block/smartmodel/connector");
	public static final ImmutableSet<BlockRenderLayer> ALL_LAYERS = ImmutableSet.copyOf(BlockRenderLayer.values());
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
		return modelLocation.equals(DATA_BASED_LOC)||modelLocation.getPath().contains(RESOURCE_LOCATION);
	}

	@Nonnull
	@Override
	public IModel loadModel(@Nonnull ResourceLocation modelLocation)
	{
		if(modelLocation.equals(DATA_BASED_LOC))
			return new ConnModelBase();
		String resourcePath = modelLocation.getPath();
		int pos = resourcePath.indexOf("conn_");
		if(pos >= 0)
		{
			pos += 5;// length of "conn_"
			String name = resourcePath.substring(pos);
			ResourceLocation r = baseModels.get(name);
			if(r!=null)
			{
				ImmutableMap<String, String> texRepl = ImmutableMap.of();
				if(textureReplacements.containsKey(name))
					texRepl = textureReplacements.get(name);
				return new ConnModelBase(r, texRepl, ImmutableMap.of("flip-v", "true"), ALL_LAYERS);
			}
		}
		return ModelLoaderRegistry.getMissingModel();
	}

	private static class ConnModelBase implements IModel
	{
		private static final ResourceLocation WIRE_LOC = new ResourceLocation(ImmersiveEngineering.MODID.toLowerCase(Locale.ENGLISH)+":blocks/wire");
		@Nullable
		private final ModelData baseData;
		@Nonnull
		private final ImmutableSet<BlockRenderLayer> layers;
		@Nonnull
		private final ImmutableMap<String, String> externalTextures;

		public ConnModelBase(@Nonnull ResourceLocation b, @Nonnull ImmutableMap<String, String> t,
							 @Nonnull ImmutableMap<String, String> customBase, @Nonnull ImmutableSet<BlockRenderLayer> layers)
		{
			this(new ModelData(b, ModelData.asJsonObject(customBase), t), layers, ImmutableMap.of());
		}

		public ConnModelBase(@Nonnull ResourceLocation b)
		{
			this(b, ImmutableMap.of(), ImmutableMap.of(), ALL_LAYERS);
		}

		public ConnModelBase()
		{
			baseData = null;
			layers = ALL_LAYERS;
			externalTextures = ImmutableMap.of();
		}

		public ConnModelBase(@Nullable ModelData newData, @Nonnull ImmutableSet<BlockRenderLayer> layers,
							 @Nonnull ImmutableMap<String, String> externalTextures)
		{
			this.baseData = newData;
			this.layers = layers;
			this.externalTextures = externalTextures;
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getDependencies()
		{
			if(baseData==null)
				return ImmutableList.of();
			baseData.attemptToLoad(false);
			if(baseData.getModel()!=null)
			{
				List<ResourceLocation> ret = new ArrayList<>(baseData.getModel().getDependencies());
				ret.add(0, baseData.location);
				return ret;
			}
			else
				return ImmutableList.of(baseData.location);
		}

		@Nonnull
		@Override
		public Collection<ResourceLocation> getTextures()
		{
			if(baseData==null)
				return ImmutableList.of();
			baseData.attemptToLoad(false);
			if(baseData.getModel()!=null)
			{
				List<ResourceLocation> ret = new ArrayList<>(baseData.getModel().getTextures());
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
			assert baseData!=null;
			baseData.attemptToLoad(true);
			assert baseData.getModel()!=null;
			return new ConnModelReal(baseData.getModel().bake(state, format, bakedTextureGetter), layers);
		}

		private static final ImmutableSet<String> ownKeys = ImmutableSet.of("base", "custom", "textures", "layers");

		@Nonnull
		@Override
		public IModel process(ImmutableMap<String, String> customData)
		{
			if(customData==null||customData.isEmpty()||!customData.containsKey("base"))
				return this;
			JsonObject obj = ModelData.asJsonObject(customData);
			ModelData newData = ModelData.fromJson(obj, ownKeys, "base", externalTextures);
			Collection<BlockRenderLayer> layers = ALL_LAYERS;
			if(obj.has("layers")&&obj.get("layers").isJsonArray())
			{
				JsonArray arr = obj.get("layers").getAsJsonArray();
				layers = new ArrayList<>(arr.size());
				for(JsonElement ele : arr)
				{
					if(ele.isJsonPrimitive()&&ele.getAsJsonPrimitive().isString())
					{
						String layerAsStr = ele.getAsString();
						if(MultiLayerModel.LAYERS_BY_NAME.containsKey(layerAsStr))
							layers.add(MultiLayerModel.LAYERS_BY_NAME.get(layerAsStr));
						else
							throw new RuntimeException("Invalid layer \""+layerAsStr+"\" in wire model");
					}
					else
						throw new RuntimeException("Layers in wire models must be strings! Invalid value: "+ele.toString());
				}
			}
			layers = ImmutableSet.copyOf(layers);
			if(!newData.equals(baseData)||!layers.equals(this.layers))
				return new ConnModelBase(newData, (ImmutableSet<BlockRenderLayer>)layers, externalTextures);
			return this;
		}

		@Nonnull
		@Override
		public IModel retexture(ImmutableMap<String, String> textures)
		{
			if(baseData!=null)
			{
				if(!textures.equals(baseData.textures)&&!(textures.isEmpty()&&!baseData.textures.isEmpty()))
					return new ConnModelBase(new ModelData(baseData.location, baseData.data, textures), layers, textures);
			}
			else if(!externalTextures.equals(textures))
				return new ConnModelBase(null, layers, textures);
			return this;
		}
	}
}
