/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering;

import blusunrize.immersiveengineering.common.data.model.ModelHelper.TransformationMap;

/**
 * Utility to generate the TRSR JSONs we use now from the old transformation code
 */
public class RotationGenerator
{
	public static void main(String[] args)
	{
		TransformationMap transforms = new TransformationMap();
		transforms.addFromJson("{\n"+
				"        \"scale\": [ 0.5, 0.5, 0.5 ],\n"+
				"        \"firstperson_righthand\": { \"translation\": [ 0, 0.25, 0.125 ]},\n"+
				"        \"firstperson_lefthand\": { \"translation\": [ 0, 0.25, 0.125 ]},\n"+
				"        \"thirdperson_righthand\": { \"translation\": [ -0.0625, 0.125, 0.1875 ], \"rotation\": [{ \"x\": 70 }, { \"y\": 70 }]},\n"+
				"        \"thirdperson_lefthand\": { \"translation\": [ -0.0625, 0.125, 0.1875 ], \"rotation\": [{ \"x\": 70 }, { \"y\": 70 }]},\n"+
				"        \"fixed\": {\"scale\": [ 2,2,2 ], \"translation\": [ 0, 0, 0 ], \"rotation\": [{ \"y\": -90 }]},\n"+
				"        \"gui\": { \"translation\": [ 0, 0.125, 0 ], \"rotation\": [{ \"x\": 30 },{ \"y\": 135 },{ \"z\": 0 }], \"scale\": [ 1.5, 1.5, 1.5 ] }\n"+
				"      }");
		System.out.println(transforms.toJson());
	}


}
