/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common;

import net.minecraft.util.ResourceLocation;

public class IERecipes
{
	//TODO move these helpers somewhere else
	public static ResourceLocation getCrystal(String type)
	{
		//TODO dos anyone use this?
		return new ResourceLocation("forge", "crystal/"+type);
	}

	public static ResourceLocation getGem(String type)
	{
		return new ResourceLocation("forge", "gems/"+type);
	}

	public static ResourceLocation getDust(String type)
	{
		return new ResourceLocation("forge", "dusts/"+type);
	}

	public static ResourceLocation getIngot(String type)
	{
		return new ResourceLocation("forge", "ingots/"+type);
	}

	public static ResourceLocation getPlate(String type)
	{
		return new ResourceLocation("forge", "plates/"+type);
	}

	public static ResourceLocation getStick(String type)
	{
		return new ResourceLocation("forge", "sticks/"+type);
	}

	public static ResourceLocation getRod(String type)
	{
		return new ResourceLocation("forge", "rods/"+type);
	}

	public static ResourceLocation getOre(String type)
	{
		return new ResourceLocation("forge", "ores/"+type);
	}

	public static ResourceLocation getStorageBlock(String type)
	{
		return new ResourceLocation("forge", "storage_blocks/"+type);
	}

	public static ResourceLocation getSheetmetalBlock(String type)
	{
		return new ResourceLocation("forge", "sheetmetal/"+type);
	}

	public static ResourceLocation getNugget(String type)
	{
		return new ResourceLocation("forge", "nuggets/"+type);
	}
}
