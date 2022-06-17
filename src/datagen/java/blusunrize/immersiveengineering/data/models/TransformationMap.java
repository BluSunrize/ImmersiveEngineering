/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import com.google.gson.*;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraftforge.common.model.TransformationHelper;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class TransformationMap
{
	private final Map<ItemTransforms.TransformType, Transformation> transforms = new EnumMap<>(ItemTransforms.TransformType.class);

	public void addFromJson(String json)
	{
		Gson GSON = new GsonBuilder()
				.registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer())
				.registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
				.create();
		JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
		Map<ItemTransforms.TransformType, Transformation> transforms = new EnumMap<>(TransformType.class);
		Optional<String> type = Optional.ofNullable(obj.remove("type")).map(JsonElement::getAsString);
		boolean vanilla = type.map("vanilla"::equals).orElse(false);
		for(ItemTransforms.TransformType perspective : ItemTransforms.TransformType.values())
		{
			String key = perspective.getSerializeName();
			JsonObject forType = obj.getAsJsonObject(key);
			obj.remove(key);
			if(forType==null)
			{
				key = alternateName(perspective);
				forType = obj.getAsJsonObject(key);
				obj.remove(key);
			}
			Transformation transform;
			if(forType!=null)
			{
				if(vanilla)
				{
					ItemTransform vanillaTransform = GSON.fromJson(forType, ItemTransform.class);
					transform = TransformationHelper.toTransformation(vanillaTransform);
				}
				else
				{
					transform = readMatrix(forType, GSON);
					if(type.map("no_corner_offset"::equals).orElse(false))
						transform = transform.blockCornerToCenter();
				}
			}
			else
				transform = Transformation.identity();
			transforms.put(perspective, transform);
		}
		Transformation baseTransform;
		if(obj.size() > 0)
			baseTransform = readMatrix(obj, GSON);
		else
			baseTransform = Transformation.identity();
		for(Entry<ItemTransforms.TransformType, Transformation> e : transforms.entrySet())
		{
			Transformation transform = composeForgeLike(e.getValue(), baseTransform);
			this.transforms.put(e.getKey(), transform);
		}
	}

	/**
	 * Composes two matrices, with special cases for one being the identity. There's a method for that in Forge in
	 * principle, but calling it is rather inconsistent due to a naming conflict with official names and Forge making
	 * an absolute mess of mappings and patches.
	 */
	private static Transformation composeForgeLike(Transformation a, Transformation b)
	{
		if(a.isIdentity()) return b;
		if(b.isIdentity()) return a;
		Matrix4f m = a.getMatrix();
		m.multiply(b.getMatrix());
		return new Transformation(m);
	}

	private Transformation readMatrix(JsonObject json, Gson GSON)
	{
		if(!json.has("origin"))
			json.addProperty("origin", "center");
		return GSON.fromJson(json, Transformation.class);
	}

	private String alternateName(ItemTransforms.TransformType type)
	{
		return type.name().toLowerCase(Locale.US);
	}

	public JsonObject toJson()
	{
		JsonObject ret = new JsonObject();
		for(Entry<ItemTransforms.TransformType, Transformation> entry : transforms.entrySet())
			add(ret, entry.getKey(), entry.getValue());
		return ret;
	}

	private void add(JsonObject main, ItemTransforms.TransformType type, Transformation trsr)
	{
		JsonObject result = new JsonObject();
		result.add("translation", toJson(trsr.getTranslation()));
		result.add("rotation", toJson(trsr.getLeftRotation()));
		result.add("scale", toJson(trsr.getScale()));
		result.add("post-rotation", toJson(trsr.getRightRotation()));
		result.addProperty("origin", "corner");
		main.add(type.getSerializeName(), result);
	}

	private static JsonArray toJson(Quaternion v)
	{
		JsonArray ret = new JsonArray();
		ret.add(v.i());
		ret.add(v.j());
		ret.add(v.k());
		ret.add(v.r());
		return ret;
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
