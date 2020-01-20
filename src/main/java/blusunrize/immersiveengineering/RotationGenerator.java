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
		transforms.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().translate(-.25, .375, .3125).rotate(-Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().translate(-.25, .375, -.625).rotate(-Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(-0.25, .125, .25).scale(.625f, .625f, .625f).rotate(-Math.PI*.375, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-0.5, .125, -.3125).scale(.625f, .625f, .625f).rotate(-Math.PI*.375, 0, 1, 0))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(-.5, .5, -.5).scale(1, 1, 1).rotate(Math.PI, 0, 1, 0))
				.setTransformations(TransformType.GUI, new Matrix4().translate(0, .5, 0).scale(1.125, 1.125, 1.125).rotate(-Math.PI*.25, 0, 1, 0))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.25, .25, .25).scale(.5, .5, .5));
		System.out.println(transforms.toJson());
	}
}
