/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.common.data.model_old.ModelHelperOld.TransformationMap;
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
		transforms.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().rotate(Math.toRadians(90), 0, 1, 0).rotate(.1, 1, 0, 0).translate(.5, .125, .5))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().rotate(Math.toRadians(-90), 0, 1, 0).rotate(-.1, 1, 0, 0).translate(-.5, .125, .5))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(.59375, .375, .75))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().rotate(3.14159, 0, 1, 0).translate(-.59375, .375, .25))
				.setTransformations(TransformType.FIXED, new Matrix4().rotate(1.57079, 0, 1, 0).scale(.75f, .75f, .75f).translate(.375, .5, .5))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.375, .375, 0).scale(.75, .625, .75).rotate(-2.35619, 0, 1, 0).rotate(0.13089, 0, 0, 1))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.125, .125, .125).scale(.25, .25, .25));
		System.out.println(transforms.toJson());
	}
}
