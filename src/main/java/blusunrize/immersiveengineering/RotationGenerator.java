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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
		transforms
				.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.125, .125, .125).translate(-.5, 1.5, .5).rotate(Math.PI*.46875, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.125, .125, .125).translate(-1.75, 1.625, .875).rotate(-Math.PI*.46875, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(.0625, .5, -.3125).scale(.1875, .1875, .1875).rotate(Math.PI*.53125, 0, 1, 0).rotate(Math.PI*.25, 0, 0, 1))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-.1875, .5, -.3125).scale(.1875, .1875, .1875).rotate(-Math.PI*.46875, 0, 1, 0).rotate(-Math.PI*.25, 0, 0, 1))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(.1875, .0625, .0625).scale(.125, .125, .125).rotate(-Math.PI*.25, 0, 0, 1))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.1875, 0, 0).scale(.1875, .1875, .1875).rotate(-Math.PI*.6875, 0, 1, 0).rotate(-Math.PI*.1875, 0, 0, 1))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.125, .125, .0625).scale(.125, .125, .125));
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
	}
}
