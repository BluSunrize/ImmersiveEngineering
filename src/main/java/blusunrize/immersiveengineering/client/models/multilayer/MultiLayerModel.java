/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.multilayer;

import blusunrize.immersiveengineering.client.models.ModelData;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class MultiLayerModel implements IModel
{
	public static MultiLayerModel INSTANCE = new MultiLayerModel();

	public static final Map<String, BlockRenderLayer> LAYERS_BY_NAME;

	static
	{
		ImmutableMap.Builder<String, BlockRenderLayer> builder = new Builder<>();
		for(BlockRenderLayer layer : BlockRenderLayer.values())
			builder.put(layer.name(), layer);
		LAYERS_BY_NAME = builder.build();
	}

	private final Map<BlockRenderLayer, List<ModelData>> subModels;

	public MultiLayerModel(Map<BlockRenderLayer, List<ModelData>> subModels)
	{
		this.subModels = subModels;
	}

	public MultiLayerModel()
	{
		this.subModels = ImmutableMap.of();
	}

	@Nonnull
	@Override
	public Collection<ResourceLocation> getDependencies()
	{
		return subModels.values().stream().flatMap(List::stream).map(modelData -> {
			modelData.attemptToLoad(false);
			if(modelData.getModel()!=null)
			{
				List<ResourceLocation> ret = new ArrayList<>(modelData.getModel().getDependencies());
				ret.add(modelData.location);
				return ret;
			}
			else
				return ImmutableList.of(modelData.location);
		}).flatMap(List::stream).collect(ImmutableList.toImmutableList());
	}

	@Nonnull
	@Override
	public Collection<ResourceLocation> getTextures()
	{
		return subModels.values().stream().flatMap(List::stream).map(modelData -> {
			modelData.attemptToLoad(false);
			if(modelData.getModel()!=null)
			{
				return modelData.getModel().getTextures();
			}
			else
				return ImmutableList.<ResourceLocation>of();
		}).flatMap(Collection::stream).collect(ImmutableList.toImmutableList());
	}

	@Nonnull
	@Override
	public IBakedModel bake(@Nonnull IModelState state, @Nonnull VertexFormat format, @Nonnull Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
	{
		Map<BlockRenderLayer, List<IBakedModel>> baked = new HashMap<>();
		for(BlockRenderLayer layer : subModels.keySet())
		{
			baked.put(layer, subModels.get(layer).stream().map(modelData -> {
				modelData.attemptToLoad(false);
				assert modelData.getModel()!=null;
				return modelData.getModel().bake(state, format, bakedTextureGetter);
			}).collect(ImmutableList.toImmutableList()));
		}
		return new BakedMultiLayerModel(baked);
	}
	@Nonnull
	@Override
	public IModel process(ImmutableMap<String, String> customData)
	{
		Map<BlockRenderLayer, List<ModelData>> newSubs = new HashMap<>();
		JsonParser parser = new JsonParser();
		Map<String, String> unused = new HashMap<>();
		for(String layerStr : customData.keySet())
			if(LAYERS_BY_NAME.containsKey(layerStr))
			{

				BlockRenderLayer layer = LAYERS_BY_NAME.get(layerStr);
				JsonElement ele = parser.parse(customData.get(layerStr));
				if(ele.isJsonObject())
				{
					ModelData data = ModelData.fromJson(ele.getAsJsonObject(), ImmutableList.of(), ImmutableMap.of());
					newSubs.put(layer, ImmutableList.of(data));
				}
				else if(ele.isJsonArray())
				{
					JsonArray array = ele.getAsJsonArray();
					List<ModelData> models = new ArrayList<>();
					for(JsonElement subEle : array)
						if(subEle.isJsonObject())
							models.add(ModelData.fromJson(ele.getAsJsonObject(), ImmutableList.of(), ImmutableMap.of()));
					newSubs.put(layer, models);
				}
			}
			else
				unused.put(layerStr, customData.get(layerStr));
		JsonObject unusedJson = ModelData.asJsonObject(unused);
		for(Entry<BlockRenderLayer, List<ModelData>> entry : newSubs.entrySet())
			for(ModelData d : entry.getValue())
				for(Entry<String, JsonElement> entryJ : unusedJson.entrySet())
					if(!d.data.has(entryJ.getKey()))
						d.data.add(entryJ.getKey(), entryJ.getValue());
		if(!newSubs.equals(subModels))
			return new MultiLayerModel(newSubs);
		return this;
	}
}
