/*
 * BluSunrize
 * Copyright (c) 2019
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import blusunrize.immersiveengineering.client.utils.ModelUtils;
import com.google.common.base.Preconditions;
import com.google.gson.*;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransform.Deserializer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.common.util.TransformationHelper;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

// TODO this is a pile of hacks and should probably just go away
public class TransformationMap
{
	private final Map<ItemDisplayContext, ItemTransform> transforms = new EnumMap<>(ItemDisplayContext.class);

	public static Vector3f toXYZDegrees(Quaternionf q)
	{
		// Based on https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles#Quaternion_to_Euler_angles_conversion
		// However some signs don't seem to be correct for MC coordinates, not sure if this is just a different
		// coordinate system or the article has errors
		// The vanilla function doesn't seem to work at all?
		float iSq = q.x*q.x;
		float jSq = q.y*q.y;
		float kSq = q.z*q.z;
		float angleX = (float)Math.atan2(
				2*(q.w*q.x-q.y*q.z),
				1-2*(iSq+jSq)
		);
		float sinOfY = 2*(q.w*q.y+q.x*q.z);
		float angleY;
		if(Math.abs(sinOfY) >= 0.999999)
			angleY = Math.copySign(Mth.HALF_PI, sinOfY);
		else
			angleY = (float)Math.asin(sinOfY);
		float angleZ = (float)Math.atan2(
				2*(q.w*q.z-q.y*q.x),
				1-2*(jSq+kSq)
		);
		Preconditions.checkState(Float.isFinite(angleX), q);
		Preconditions.checkState(Float.isFinite(angleY), q);
		Preconditions.checkState(Float.isFinite(angleZ), q);
		Vector3f result = new Vector3f(angleX, angleY, angleZ);
		result.mul(180/Mth.PI);
		return result;
	}

	public void addFromJson(String json)
	{
		Gson GSON = new GsonBuilder()
				.registerTypeAdapter(Transformation.class, new TransformationHelper.Deserializer())
				.registerTypeAdapter(ItemTransform.class, new ItemTransform.Deserializer())
				.create();
		JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
		Optional<String> type = Optional.ofNullable(obj.remove("type")).map(JsonElement::getAsString);
		boolean vanilla = type.map("vanilla"::equals).orElse(false);
		Map<ItemDisplayContext, Transformation> transforms = new EnumMap<>(ItemDisplayContext.class);
		for(ItemDisplayContext perspective : ItemDisplayContext.values())
		{
			String key = perspective.getSerializedName();
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
					transform = ModelUtils.fromItemTransform(vanillaTransform, false);
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
		for(Entry<ItemDisplayContext, Transformation> e : transforms.entrySet())
		{
			Transformation transform = composeForgeLike(e.getValue(), baseTransform);
			if(!transform.isIdentity())
			{
				var translation = new Vector3f(transform.getTranslation());
				translation.mul(16);
				this.transforms.put(e.getKey(), new ItemTransform(
						toXYZDegrees(transform.getLeftRotation()),
						translation,
						transform.getScale(),
						toXYZDegrees(transform.getRightRotation())
				));
			}
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
		m.mul(b.getMatrix());
		return new Transformation(m);
	}

	private Transformation readMatrix(JsonObject json, Gson GSON)
	{
		if(!json.has("origin"))
			json.addProperty("origin", "corner");
		return GSON.fromJson(json, Transformation.class);
	}

	private String alternateName(ItemDisplayContext type)
	{
		return type.name().toLowerCase(Locale.US);
	}

	public JsonObject toJson()
	{
		JsonObject ret = new JsonObject();
		for(Entry<ItemDisplayContext, ItemTransform> entry : transforms.entrySet())
			add(ret, entry.getKey(), entry.getValue());
		return ret;
	}

	private void add(JsonObject main, ItemDisplayContext type, ItemTransform trsr)
	{
		JsonObject result = new JsonObject();
		if(!trsr.translation.equals(Deserializer.DEFAULT_TRANSLATION))
			result.add("translation", toJson(trsr.translation));
		if(!trsr.rotation.equals(Deserializer.DEFAULT_ROTATION))
			result.add("rotation", toJson(trsr.rotation));
		if(!trsr.scale.equals(Deserializer.DEFAULT_SCALE))
			result.add("scale", toJson(trsr.scale));
		if(!trsr.rightRotation.equals(Deserializer.DEFAULT_ROTATION))
			result.add("right_rotation", toJson(trsr.rightRotation));
		main.add(type.getSerializedName(), result);
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
