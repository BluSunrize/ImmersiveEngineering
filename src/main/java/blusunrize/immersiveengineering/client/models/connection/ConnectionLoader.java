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
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
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
		model = ModelLoaderRegistry.getModel(subloader, deserializationContext, baseModelData);
		List<String> layers = ImmutableList.of(RenderType.getSolid().toString());
		if(modelContents.has("layers")&&modelContents.get("layers").isJsonArray())
		{
			JsonArray arr = modelContents.get("layers").getAsJsonArray();
			layers = new ArrayList<>(arr.size());
			for(JsonElement ele : arr)
			{
				if(ele.isJsonPrimitive()&&ele.getAsJsonPrimitive().isString())
					layers.add(ele.getAsString());
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
		private final List<String> layers;

		public ConnectorModel(@Nullable IModelGeometry<?> baseModel, @Nonnull List<String> layers)
		{
			this.baseModel = baseModel;
			this.layers = layers;
		}

		@Override
		public IBakedModel bake(
				IModelConfiguration owner,
				ModelBakery bakery,
				Function<RenderMaterial, TextureAtlasSprite> spriteGetter,
				IModelTransform modelTransform,
				ItemOverrideList overrides,
				ResourceLocation modelLocation)
		{
			IBakedModel base;
			if(baseModel==null)
				base = null;
			else
				base = baseModel.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
			return new BakedConnectionModel(base, layers);
		}

		@Override
		public Collection<RenderMaterial> getTextures(IModelConfiguration owner, Function<ResourceLocation, IUnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
		{
			Collection<RenderMaterial> ret;
			if(baseModel!=null)
				ret = new ArrayList<>(
						baseModel.getTextures(owner, modelGetter, missingTextureErrors));
			else
				ret = new ArrayList<>();
			ret.add(new RenderMaterial(PlayerContainer.LOCATION_BLOCKS_TEXTURE, WIRE_LOC));
			return ret;
		}
	}
}
