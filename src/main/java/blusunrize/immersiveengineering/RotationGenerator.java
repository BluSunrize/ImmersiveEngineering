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
		transforms.setTransformations(TransformType.FIRST_PERSON_RIGHT_HAND, new Matrix4().scale(.375, .375, .375).translate(-.75, 1.25, .3125).rotate(-Math.PI*.75, 0, 1, 0))
				.setTransformations(TransformType.FIRST_PERSON_LEFT_HAND, new Matrix4().scale(.375, .375, .375).translate(-.125, 1.25, .9375).rotate(Math.PI*.25, 0, 1, 0))
				.setTransformations(TransformType.THIRD_PERSON_RIGHT_HAND, new Matrix4().translate(-.25, .1875, .3125).scale(.625, .625, .625).rotate(Math.PI, 0, 1, 0).rotate(-Math.PI*.5, 1, 0, 0))
				.setTransformations(TransformType.THIRD_PERSON_LEFT_HAND, new Matrix4().translate(-.25, -.4375, .3125).scale(.625, .625, .625).rotate(Math.PI*.5, 1, 0, 0))
				.setTransformations(TransformType.FIXED, new Matrix4().translate(.5, .875, -.5).scale(1, 1, 1).rotate(Math.PI*.5, 0, 1, 0))
				.setTransformations(TransformType.GUI, new Matrix4().translate(-.625, .75, 0).scale(.875, .875, .875).rotate(-Math.PI*.6875, 0, 1, 0))
				.setTransformations(TransformType.GROUND, new Matrix4().translate(.25, .5, .25).scale(.5, .5, .5));
		System.out.println(transforms.toJson());
	}


}
