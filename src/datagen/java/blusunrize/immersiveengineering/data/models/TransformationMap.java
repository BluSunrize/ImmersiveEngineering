/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import com.google.gson.*;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraftforge.common.util.TransformationHelper;

import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

// TODO Remove and replace by a simple JSON passthrough
// TODO or use parent models to set transforms?
public class TransformationMap
{
	private final Map<ItemTransforms.TransformType, ItemTransform> transforms = new EnumMap<>(ItemTransforms.TransformType.class);

	public void addFromJson(String json)
	{
		Gson GSON = new GsonBuilder()
				.registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer())
				.registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
				.registerTypeAdapter(ItemTransforms.class, new ItemTransforms.Deserializer())
				.create();
		JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
		Optional<String> type = Optional.ofNullable(obj.remove("type")).map(JsonElement::getAsString);
		if(!type.map("vanilla"::equals).orElse(false))
			return;
		ItemTransforms vanillaTransforms = GSON.fromJson(obj, ItemTransforms.class);
		Vector3f extraScale;
		// TODO also support extra rotation, not sure how to compose. See alloysmelter.json
		if(obj.has("scale"))
		{
			JsonArray scaleArr = obj.remove("scale").getAsJsonArray();
			extraScale = new Vector3f(
					scaleArr.get(0).getAsFloat(), scaleArr.get(1).getAsFloat(), scaleArr.get(2).getAsFloat()
			);
		}
		else
			extraScale = new Vector3f(1, 1, 1);
		for(ItemTransforms.TransformType perspective : ItemTransforms.TransformType.values())
		{
			ItemTransform vanillaValue = vanillaTransforms.getTransform(perspective);
			Vector3f newScale = vanillaValue.scale.copy();
			newScale.mul(extraScale.x(), extraScale.y(), extraScale.z());
			this.transforms.put(perspective, new ItemTransform(
					vanillaValue.rotation,
					vanillaValue.translation,
					newScale,
					vanillaValue.rightRotation
			));
		}
	}

	public JsonObject toJson()
	{
		JsonObject ret = new JsonObject();
		for(Entry<ItemTransforms.TransformType, ItemTransform> entry : transforms.entrySet())
			add(ret, entry.getKey(), entry.getValue());
		return ret;
	}

	private void add(JsonObject main, ItemTransforms.TransformType type, ItemTransform trsr)
	{
		JsonObject result = new JsonObject();
		if(!trsr.translation.equals(ItemTransform.NO_TRANSFORM.translation))
			result.add("translation", toJson(trsr.translation));
		if(!trsr.rotation.equals(ItemTransform.NO_TRANSFORM.rotation))
			result.add("rotation", toJson(trsr.rotation));
		if(!trsr.scale.equals(ItemTransform.NO_TRANSFORM.scale))
			result.add("scale", toJson(trsr.scale));
		if(!trsr.rightRotation.equals(ItemTransform.NO_TRANSFORM.rightRotation))
			result.add("right_rotation", toJson(trsr.rightRotation));
		if(!result.keySet().isEmpty())
			main.add(type.getSerializeName(), result);
	}

	private static JsonArray toJson(Vector3f v)
	{
		JsonArray ret = new JsonArray();
		ret.add(v.x());
		ret.add(v.y());
		ret.add(v.z());
		return ret;
	}
}
