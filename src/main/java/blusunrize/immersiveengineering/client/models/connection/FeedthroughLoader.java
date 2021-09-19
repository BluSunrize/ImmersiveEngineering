/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader.FeedthroughModelRaw;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.client.model.IModelConfiguration;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

public class FeedthroughLoader implements IModelLoader<FeedthroughModelRaw>
{
	public static final ResourceLocation LOCATION = new ResourceLocation(ImmersiveEngineering.MODID, "feedthrough");

	@Override
	public void onResourceManagerReload(ResourceManager resourceManager)
	{
	}

	@Override
	public FeedthroughModelRaw read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
	{
		return new FeedthroughModelRaw();
	}

	public static class FeedthroughModelRaw implements IModelGeometry<FeedthroughModelRaw>
	{

		@Override
		public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation)
		{
			return new FeedthroughModel();
		}

		@Override
		public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors)
		{
			return ImmutableList.of();
		}
	}
}
