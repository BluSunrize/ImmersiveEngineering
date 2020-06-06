/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader.ConnectorModel;
import blusunrize.immersiveengineering.client.models.multilayer.MultiLayerModel;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.ISprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry2;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class ConnectionLoader implements IModelLoader<ConnectorModel>
{
	public static final ResourceLocation LOADER_NAME = new ResourceLocation(ImmersiveEngineering.MODID, "connector");

	@Override
	public void onResourceManagerReload(@Nonnull IResourceManager resourceManager)
	{
		BakedConnectionModel.cache.invalidateAll();
	}

	@Nonnull
	@Override
	public ConnectorModel read(@Nonnull JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		JsonObject baseModelData = modelContents.getAsJsonObject("base_model");
		IModelGeometry<?> model;
		ResourceLocation subloader;
		if(baseModelData.has("loader"))
			subloader = new ResourceLocation(baseModelData.get("loader").getAsString());
		else
			subloader = new ResourceLocation("minecraft", "elements");
		model = ModelLoaderRegistry2.getModel(subloader, deserializationContext, baseModelData);
		List<BlockRenderLayer> layers = ImmutableList.of(BlockRenderLayer.SOLID);
		if(modelContents.has("layers")&&modelContents.get("layers").isJsonArray())
		{
			JsonArray arr = modelContents.get("layers").getAsJsonArray();
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
		return new ConnectorModel(model, layers);
	}

	public static class ConnectorModel implements IModelGeometry<ConnectorModel>
	{
		private static final ResourceLocation WIRE_LOC = new ResourceLocation(ImmersiveEngineering.MODID.toLowerCase(Locale.ENGLISH)+":block/wire");
		@Nullable
		private final IModelGeometry<?> baseModel;
		@Nonnull
		private final List<BlockRenderLayer> layers;

		public ConnectorModel(@Nullable IModelGeometry<?> baseModel, @Nonnull List<BlockRenderLayer> layers)
		{
			this.baseModel = baseModel;
			this.layers = layers;
		}

		@Override
		public IBakedModel bake(
				IModelConfiguration owner,
				ModelBakery bakery,
				Function<ResourceLocation, TextureAtlasSprite> spriteGetter,
				ISprite sprite,
				VertexFormat format,
				ItemOverrideList overrides)
		{
			IBakedModel base;
			if(baseModel==null)
				base = null;
			else
				base = baseModel.bake(owner, bakery, spriteGetter, sprite, format, overrides);
			return new BakedConnectionModel(base, layers);
		}

		@Override
		public Collection<ResourceLocation> getTextureDependencies(
				IModelConfiguration owner,
				Function<ResourceLocation, IUnbakedModel> modelGetter,
				Set<String> missingTextureErrors)
		{
			Collection<ResourceLocation> ret;
			if(baseModel!=null)
				ret = new ArrayList<>(
						baseModel.getTextureDependencies(owner, modelGetter, missingTextureErrors));
			else
				ret = new ArrayList<>();
			ret.add(WIRE_LOC);
			return ret;
		}
	}
}
