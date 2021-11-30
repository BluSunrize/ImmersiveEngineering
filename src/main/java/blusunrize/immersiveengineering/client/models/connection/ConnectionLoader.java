/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.client.models.UnbakedModelGeometry;
import blusunrize.immersiveengineering.client.models.connection.ConnectionLoader.ConnectorModel;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.model.ForgeModelBakery;
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
	public void onResourceManagerReload(@Nonnull ResourceManager resourceManager)
	{
		BakedConnectionModel.cache.invalidateAll();
	}

	@Nonnull
	@Override
	public ConnectorModel read(@Nonnull JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		IModelGeometry<?> model;
		JsonElement baseModel = modelContents.get("base_model");
		if(baseModel.isJsonObject())
		{
			JsonObject baseModelData = modelContents.getAsJsonObject("base_model");
			ResourceLocation subloader;
			if(baseModelData.has("loader"))
				subloader = new ResourceLocation(baseModelData.get("loader").getAsString());
			else
				subloader = new ResourceLocation("minecraft", "elements");
			model = ModelLoaderRegistry.getModel(subloader, deserializationContext, baseModelData);
		}
		else
			model = new UnbakedModelGeometry(ForgeModelBakery.defaultModelGetter().apply(
					new ResourceLocation(baseModel.getAsString())
			));
		List<String> layers = ImmutableList.of(RenderType.solid().toString());
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
		public BakedModel bake(
				IModelConfiguration owner,
				ModelBakery bakery,
				Function<Material, TextureAtlasSprite> spriteGetter,
				ModelState modelTransform,
				ItemOverrides overrides,
				ResourceLocation modelLocation)
		{
			BakedModel base;
			if(baseModel==null)
				base = null;
			else
				base = baseModel.bake(owner, bakery, spriteGetter, modelTransform, overrides, modelLocation);
			if(base instanceof ICacheKeyProvider<?>)
				return new BakedConnectionModel<>(base, layers, (ICacheKeyProvider<?>)base);
			else
				return new BakedConnectionModel<>(base, layers, Unit.INSTANCE);
		}

		@Override
		public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
		{
			Collection<Material> ret;
			if(baseModel!=null)
				ret = new ArrayList<>(
						baseModel.getTextures(owner, modelGetter, missingTextureErrors));
			else
				ret = new ArrayList<>();
			ret.add(new Material(InventoryMenu.BLOCK_ATLAS, WIRE_LOC));
			return ret;
		}
	}
}
