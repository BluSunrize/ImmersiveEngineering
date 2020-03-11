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
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

public class MultiLayerModel implements IModelGeometry<MultiLayerModel>
{
	public static final Map<String, BlockRenderLayer> LAYERS_BY_NAME;

	static
	{
		ImmutableMap.Builder<String, BlockRenderLayer> builder = new Builder<>();
		for(BlockRenderLayer layer : BlockRenderLayer.values())
			builder.put(layer.name(), layer);
		LAYERS_BY_NAME = builder.build();
	}

	private final Map<BlockRenderLayer, IModelGeometry<?>> subModels;

	public MultiLayerModel(Map<BlockRenderLayer, IModelGeometry<?>> subModels)
	{
		this.subModels = subModels;
	}

	@Override
	public Collection<ResourceLocation> getTextureDependencies(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<String> missingTextureErrors)
	{
		List<ResourceLocation> ret = new ArrayList<>();
		for(IModelGeometry<?> geometry : subModels.values())
			ret.addAll(geometry.getTextureDependencies(owner, modelGetter, missingTextureErrors));
		return ret;
	}

	@Override
	public IBakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<ResourceLocation, TextureAtlasSprite> spriteGetter, ISprite sprite, VertexFormat format, ItemOverrideList overrides)
	{
		Map<BlockRenderLayer, IBakedModel> baked = new HashMap<>();
		for(Entry<BlockRenderLayer, IModelGeometry<?>> e : subModels.entrySet())
			//TODO sprite getters?
			baked.put(e.getKey(), e.getValue().bake(owner, bakery, spriteGetter, sprite, format, overrides));
		return new BakedMultiLayerModel(baked);
	}
}
