/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.models.connection.FeedthroughLoader.FeedthroughModelRaw;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class FeedthroughLoader implements IGeometryLoader<FeedthroughModelRaw>
{
	public static final ResourceLocation LOCATION = IEApi.ieLoc("feedthrough");

	@Override
	public FeedthroughModelRaw read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException
	{
		return new FeedthroughModelRaw();
	}

	public static class FeedthroughModelRaw implements IUnbakedGeometry<FeedthroughModelRaw>
	{
		@Override
		public BakedModel bake(IGeometryBakingContext context, ModelBaker bakery, Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState, ItemOverrides overrides)
		{
			return new FeedthroughModel();
		}
	}
}
