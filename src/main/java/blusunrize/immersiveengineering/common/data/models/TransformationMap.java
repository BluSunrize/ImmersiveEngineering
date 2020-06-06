/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.data.models;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.gson.*;
import net.minecraft.client.renderer.model.ItemTransformVec3f;
import net.minecraftforge.client.model.ForgeBlockStateV1.TRSRDeserializer;
import net.minecraftforge.client.model.generators.ModelBuilder.Perspective;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

public class TransformationMap
{
	private static Field nameFromPerspective;

	static
	{
		try
		{
			//Who decided to make the one useful field in that enum private????
			nameFromPerspective = Perspective.class.getDeclaredField("name");
			nameFromPerspective.setAccessible(true);
		} catch(NoSuchFieldException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	private final Map<Perspective, TRSRTransformation> transforms = new TreeMap<>();

	public TransformationMap setTransformations(Perspective t, Matrix4 mat)
	{
		transforms.put(t, new TRSRTransformation(mat.toMatrix4f()));
		return this;
	}

	private static String getName(Perspective p)
	{
		try
		{
			return (String)nameFromPerspective.get(p);
		} catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	public void addFromJson(String json)
	{
		Gson GSON = new GsonBuilder()
				.registerTypeAdapter(TRSRTransformation.class, TRSRDeserializer.INSTANCE)
				.registerTypeAdapter(ItemTransformVec3f.class, new ItemTransformVec3f.Deserializer())
				.create();
		JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
		Map<Perspective, TRSRTransformation> transforms = new HashMap<>();
		Optional<String> type = Optional.ofNullable(obj.remove("type")).map(JsonElement::getAsString);
		boolean vanilla = type.map("vanilla"::equals).orElse(false);
		for(Perspective perspective : Perspective.values())
		{
			String key = getName(perspective);
			JsonObject forType = obj.getAsJsonObject(key);
			obj.remove(key);
			if(forType==null)
			{
				key = alternateName(perspective);
				forType = obj.getAsJsonObject(key);
				obj.remove(key);
			}
			TRSRTransformation transform;
			if(forType!=null)
			{
				if(vanilla)
				{
					ItemTransformVec3f vanillaTransform = GSON.fromJson(forType, ItemTransformVec3f.class);
					transform = TRSRTransformation.from(vanillaTransform);
				}
				else
					transform = GSON.fromJson(forType, TRSRTransformation.class);
			}
			else
				transform = TRSRTransformation.identity();
			if(type.map("no_corner_offset"::equals).orElse(false))
				transform = TRSRTransformation.blockCornerToCenter(transform);
			transforms.put(perspective, transform);
		}
		TRSRTransformation baseTransform;
		if(obj.size() > 0)
			baseTransform = GSON.fromJson(obj, TRSRTransformation.class);
		else
			baseTransform = TRSRTransformation.identity();
		for(Entry<Perspective, TRSRTransformation> e : transforms.entrySet())
			this.transforms.put(e.getKey(), e.getValue().compose(baseTransform));
	}

	private String alternateName(Perspective type)
	{
		return type.vanillaType.name()
				.toLowerCase();
	}

	public JsonObject toJson()
	{
		JsonObject ret = new JsonObject();
		for(Entry<Perspective, TRSRTransformation> entry : transforms.entrySet())
			add(ret, entry.getKey(), entry.getValue());
		return ret;
	}

	private void add(JsonObject main, Perspective type, TRSRTransformation trsr)
	{
		JsonObject result = new JsonObject();
		result.add("translation", toJson(trsr.getTranslation()));
		result.add("rotation", toJson(trsr.getLeftRot()));
		result.add("scale", toJson(trsr.getScale()));
		result.add("post-rotation", toJson(trsr.getRightRot()));
		main.add(getName(type), result);
	}

	private static JsonArray toJson(Quat4f v)
	{
		JsonArray ret = new JsonArray();
		ret.add(v.x);
		ret.add(v.y);
		ret.add(v.z);
		ret.add(v.w);
		return ret;
	}

	private static JsonArray toJson(Vector3f v)
	{
		JsonArray ret = new JsonArray();
		ret.add(v.x);
		ret.add(v.y);
		ret.add(v.z);
		return ret;
	}

	private static Vector3f fromJson(JsonElement ele)
	{
		JsonArray arr = ele.getAsJsonArray();
		return new Vector3f(arr.get(0).getAsFloat(), arr.get(1).getAsFloat(), arr.get(2).getAsFloat());
	}
}
