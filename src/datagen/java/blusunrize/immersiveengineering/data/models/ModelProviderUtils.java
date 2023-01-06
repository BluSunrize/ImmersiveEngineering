/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.models;

import net.minecraft.client.renderer.RenderType;

public class ModelProviderUtils
{
	public static String getName(RenderType type)
	{
		if(type==RenderType.solid())
			return "solid";
		else if(type==RenderType.translucent())
			return "translucent";
		else if(type==RenderType.cutout())
			return "cutout";
		else if(type==RenderType.cutoutMipped())
			return "cutout_mipped";
		else
			throw new RuntimeException("Unknown render type: "+type);
	}
}
