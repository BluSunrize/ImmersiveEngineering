/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

public class ModelData
{
	@Nonnull
	public final ResourceLocation location;
	@Nonnull
	public final JsonObject data;
	@Nonnull
	public final ImmutableMap<String, String> textures;
	@Nullable
	private IModel model;

	public ModelData(@Nonnull ResourceLocation location, @Nonnull JsonObject data, @Nonnull ImmutableMap<String, String> textures)
	{
		this.location = location;
		this.data = data;
		this.textures = textures;
	}

	public void attemptToLoad(boolean throwError)
	{
		try
		{
			model = ModelLoaderRegistry.getModel(location);
			model = model.retexture(textures).process(asMap(data, false));
		} catch(Exception e)
		{
			if(throwError)
				throw new RuntimeException(e);
		}
	}

	@Nullable
	public IModel getModel()
	{
		return model;
	}

	public static ModelData fromMap(ImmutableMap<String, String> customData, ImmutableSet<String> ownKeys, String base,
									ImmutableMap<String, String> texReplacements)
	{
		return fromJson(asJsonObject(customData), ownKeys, base, texReplacements);
	}

	public static ModelData fromJson(JsonObject customData, Collection<String> ownKeys, ImmutableMap<String, String> texReplacements)
	{
		return fromJson(customData, ownKeys, "model", texReplacements);
	}

	public static ModelData fromJson(JsonObject customData, Collection<String> knownKeys, String modelKey,
									 ImmutableMap<String, String> texReplacements)
	{
		String baseLocStr = customData.get(modelKey).getAsString();
		ResourceLocation baseLoc = new ResourceLocation(baseLocStr);
		JsonObject customBase = new JsonObject();
		if(customData.has("custom"))
			customBase = customData.get("custom").getAsJsonObject();
		for(Entry<String, JsonElement> e : customData.entrySet())
			if(!knownKeys.contains(e.getKey())&&!customBase.has(e.getKey()))
				customBase.add(e.getKey(), e.getValue());
		if(customData.has("textures"))
		{
			JsonObject obj = customData.get("textures").getAsJsonObject();
			ImmutableMap.Builder<String, String> b = ImmutableMap.builder();
			b.putAll(texReplacements);
			b.putAll(asMap(obj, true));
			texReplacements = b.build();
		}
		return new ModelData(baseLoc, customBase, texReplacements);
	}

	public static JsonObject asJsonObject(Map<String, String> map)
	{
		JsonObject ret = new JsonObject();
		JsonParser parser = new JsonParser();
		for(Entry<String, String> e : map.entrySet())
		{
			ret.add(e.getKey(), parser.parse(e.getValue()));
		}
		return ret;
	}

	public static ImmutableMap<String, String> asMap(JsonObject obj, boolean cleanStrings)
	{
		ImmutableMap.Builder<String, String> ret = new Builder<>();
		for(Entry<String, JsonElement> entry : obj.entrySet())
			if(cleanStrings&&entry.getValue().isJsonPrimitive()&&entry.getValue().getAsJsonPrimitive().isString())
				ret.put(entry.getKey(), entry.getValue().getAsString());
			else
				ret.put(entry.getKey(), entry.getValue().toString());
		return ret.build();
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;

		ModelData modelData = (ModelData)o;

		if(!location.equals(modelData.location)) return false;
		if(!data.equals(modelData.data)) return false;
		return textures.equals(modelData.textures);
	}

	@Override
	public int hashCode()
	{
		int result = location.hashCode();
		result = 31*result+data.hashCode();
		result = 31*result+textures.hashCode();
		return result;
	}
}
