/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.plant;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum EnumHempGrowth implements StringRepresentable
{
	BOTTOM0,
	BOTTOM1,
	BOTTOM2,
	BOTTOM3,
	BOTTOM4,
	TOP0;

	@Override
	public String getSerializedName()
	{
		return name().toLowerCase(Locale.ENGLISH);
	}

	public ResourceLocation getTextureName()
	{
		return new ResourceLocation(ImmersiveEngineering.MODID, "block/hemp/"+getSerializedName());
	}

	@Override
	public String toString()
	{
		return getSerializedName();
	}

	public EnumHempGrowth next()
	{
		return switch(this)
				{
					case BOTTOM0 -> BOTTOM1;
					case BOTTOM1 -> BOTTOM2;
					case BOTTOM2 -> BOTTOM3;
					case BOTTOM3 -> BOTTOM4;
					case BOTTOM4, TOP0 -> this;
				};
	}

	public EnumHempGrowth getMin()
	{
		return TOP0==this?TOP0: BOTTOM0;
	}

	public EnumHempGrowth getMax()
	{
		return TOP0==this?TOP0: BOTTOM4;
	}
}