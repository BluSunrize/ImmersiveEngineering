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
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import com.mojang.math.Transformation;
import org.joml.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransform;
import net.minecraft.client.renderer.block.model.ItemTransform.Deserializer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.util.Mth;
import net.minecraftforge.common.util.TransformationHelper;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

// TODO this is a pile of hacks and should probably just go away
public class TransformationMap
{
	private final Map<ItemTransforms.TransformType, ItemTransform> transforms = new EnumMap<>(ItemTransforms.TransformType.class);

	public static Vector3f toXYZDegrees(Quaternionf q)
	{
		// Based on https://en.wikipedia.org/wiki/Conversion_between_quaternions_and_Euler_angles#Quaternion_to_Euler_angles_conversion
		// However some signs don't seem to be correct for MC coordinates, not sure if this is just a different
		// coordinate system or the article has errors
		// The vanilla function doesn't seem to work at all?
		float iSq = q.i()*q.i();
		float jSq = q.j()*q.j();
		float kSq = q.k()*q.k();
		float angleX = (float)Math.atan2(
				2*(q.r()*q.i()-q.j()*q.k()),
				1-2*(iSq+jSq)
		);
		float sinOfY = 2*(q.r()*q.j()+q.i()*q.k());
		float angleY;
		if(Math.abs(sinOfY) >= 0.999999)
			angleY = Math.copySign(Mth.HALF_PI, sinOfY);
		else
			angleY = (float)Math.asin(sinOfY);
		float angleZ = (float)Math.atan2(
				2*(q.r()*q.k()-q.j()*q.i()),
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
		Map<ItemTransforms.TransformType, Transformation> transforms = new EnumMap<>(TransformType.class);
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
		for(Entry<TransformType, Transformation> e : transforms.entrySet())
		{
			Transformation transform = composeForgeLike(e.getValue(), baseTransform);
			if(!transform.isIdentity())
			{
				var translation = transform.getTranslation().copy();
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
		for(Entry<ItemTransforms.TransformType, ItemTransform> entry : transforms.entrySet())
			add(ret, entry.getKey(), entry.getValue());
		return ret;
	}

	private void add(JsonObject main, ItemTransforms.TransformType type, ItemTransform trsr)
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
