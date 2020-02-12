/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.common.data.model.ModelHelper.TransformationMap;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;

/**
 * Utility to generate the TRSR JSONs we use now from the old transformation code
 */
public class RotationGenerator
{
	public static void main(String[] args)
	{
		TransformationMap transforms = new TransformationMap();
		transforms.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().translate(.2, .1, 0).rotate(-Math.PI/3, 1, 0, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().translate(.2, .1, 0).rotate(-Math.PI/3, 1, 0, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(0, .5, .1))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(0, .5, .1))
				.setTransformations(TransformType.FIXED, new Matrix4())
				.setTransformations(TransformType.GUI, new Matrix4().rotate(-Math.PI/4, 0, 0, 1).rotate(Math.PI/8, 0, 1, 0))
				.setTransformations(TransformType.GROUND, new Matrix4().scale(.5, .5, .5).translate(0, .5, 0));
		System.out.println(transforms.toJson());
	}
}
