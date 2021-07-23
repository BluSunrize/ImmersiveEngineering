/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public class IEEnums
{
	public enum IOSideConfig implements StringRepresentable
	{
		NONE("none"),
		INPUT("in"),
		OUTPUT("out");

		public static final IOSideConfig[] VALUES = values();

		final String texture;

		IOSideConfig(String texture)
		{
			this.texture = texture;
		}

		@Override
		public String getSerializedName()
		{
			return this.toString().toLowerCase(Locale.ENGLISH);
		}

		public String getTextureName()
		{
			return texture;
		}

		public Component getTextComponent()
		{
			return new TranslatableComponent(Lib.DESC_INFO+"blockSide.io."+getSerializedName());
		}

		public static IOSideConfig next(IOSideConfig current)
		{
			return current==INPUT?OUTPUT: current==OUTPUT?NONE: INPUT;
		}
	}
}