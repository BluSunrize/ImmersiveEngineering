/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.google.common.base.Preconditions;
import com.google.gson.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraftforge.client.model.ForgeBlockStateV1.TRSRDeserializer;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Utility to generate the TRSR JSONs we use now from the old transformation code
 */
public class RotationGenerator
{
	public static void main(String[] args)
	{
		Transformation transforms = new Transformation();
		transforms.addFromJson("{ \"scale\": [ 0.09375, 0.09375, 0.09375 ], \"firstperson_righthand\": { \"translation\": [ 0.25, 0, 0 ], \"rotation\": [{ \"y\": -90 }], \"scale\": [ 2, 2, 2 ] }, \"firstperson_lefthand\": { \"translation\": [ 0.25, 0, 0 ], \"rotation\": [{ \"y\": -90 }], \"scale\": [ 2, 2, 2 ] }, \"thirdperson_righthand\": { \"translation\": [ 0, 0.09375, -0.171875 ], \"rotation\": [{ \"x\": 60 },{ \"y\": -142.5 }], \"scale\": [ 0.75, 0.75, 0.75 ] }, \"thirdperson_lefthand\": { \"translation\": [ 0, 0.09375, -0.171875 ], \"rotation\": [{ \"x\": 60 },{ \"y\": -142.5 }], \"scale\": [ 0.75, 0.75, 0.75 ] }, \"fixed\": { \"scale\": [ 1.5, 1.5, 1.5 ], \"rotation\": [{ \"y\": 180 }] }, \"gui\": { \"scale\": [ 1.25, 1.25, 1.25 ], \"rotation\": [{ \"y\": 35 }] } }");
		System.out.println(transforms.toJson());
	}

	private static class Transformation
	{
		private final Map<TransformType, Matrix4> transforms = new HashMap<>();

		public Transformation setTransformations(TransformType t, Matrix4 mat)
		{
			transforms.put(t, mat);
			return this;
		}

		public void addFromJson(String json)
		{
			Gson GSON = new GsonBuilder().registerTypeAdapter(TRSRTransformation.class, TRSRDeserializer.INSTANCE).create();
			JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
			Vector3f baseScale;
			if(obj.has("scale"))
				baseScale = fromJson(obj.get("scale"));
			else
				baseScale = new Vector3f(1, 1, 1);
			for(TransformType type : TransformType.values())
			{
				String key = type.name().toLowerCase();
				JsonObject forType = obj.getAsJsonObject(key);
				if(forType==null)
				{
					key = key.replace("_person", "person").replace("_hand", "hand");
					forType = obj.getAsJsonObject(key);
				}
				TRSRTransformation transform;
				if(forType!=null)
				{
					transform = GSON.fromJson(forType, TRSRTransformation.class);
					Vector3f oldScale = transform.getScale();
					Vector3f newScale = new Vector3f(
							oldScale.x*baseScale.x,
							oldScale.y*baseScale.y,
							oldScale.z*baseScale.z
					);
					transform = new TRSRTransformation(transform.getTranslation(), transform.getLeftRot(), newScale, transform.getRightRot());
				}
				else
					transform = new TRSRTransformation(null, null, baseScale, null);
				transforms.put(type, new Matrix4(transform.getMatrixVec()));
			}
		}

		public JsonObject toJson()
		{
			JsonObject ret = new JsonObject();
			for(Entry<TransformType, Matrix4> entry : transforms.entrySet())
			{
				add(ret, entry.getKey(), entry.getValue());
			}
			return ret;
		}

		private void add(JsonObject main, TransformType type, Matrix4 mat)
		{
			TRSRTransformation trsr = TRSRTransformation.blockCenterToCorner(new TRSRTransformation(mat.toMatrix4f()));
			JsonObject result = new JsonObject();
			result.add("translation", toJson(trsr.getTranslation()));
			result.add("rotation", toJson(trsr.getLeftRot()));
			result.add("scale", toJson(trsr.getScale()));
			result.add("post-rotation", toJson(trsr.getRightRot()));
			Gson tmp = new GsonBuilder().registerTypeAdapter(TRSRTransformation.class, TRSRDeserializer.INSTANCE).create();
			TRSRTransformation check = tmp.fromJson(result, TRSRTransformation.class);
			check = TRSRTransformation.blockCornerToCenter(check);
			for(int i = 0; i < 4; ++i)
			{
				for(int j = 0; j < 4; ++j)
				{
					float checkVal = check.getMatrixVec().getElement(i, j);
					double origVal = mat.getElement(i, j);
					Preconditions.checkState(Math.abs(checkVal-origVal) < 1e-3);
				}

			}
			main.add(type.name().toLowerCase(), result);
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
}
