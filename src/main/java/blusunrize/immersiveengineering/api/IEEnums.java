/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.util.IStringSerializable;

import java.util.Locale;

public class IEEnums
{
	public enum SideConfig implements IStringSerializable
	{
		NONE("none"),
		INPUT("in"),
		OUTPUT("out");

		public static final SideConfig[] VALUES = values();

		final String texture;

		SideConfig(String texture)
		{
			this.texture = texture;
		}

		@Override
		public String getName()
		{
			return this.toString().toLowerCase(Locale.ENGLISH);
		}

		public String getTextureName()
		{
			return texture;
		}

		public static SideConfig next(SideConfig current)
		{
			return current==INPUT?OUTPUT: current==OUTPUT?NONE: INPUT;
		}
	}
}