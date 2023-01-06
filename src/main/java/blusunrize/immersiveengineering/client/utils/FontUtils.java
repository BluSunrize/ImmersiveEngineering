/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

public class FontUtils
{
	public static String hexColorString(int color)
	{
		StringBuilder hexCol = new StringBuilder(Integer.toHexString(color));
		while(hexCol.length() < 6)
			hexCol.insert(0, "0");
		return hexCol.toString();
	}

	public static MutableComponent withAppendColoredColour(MutableComponent base, int color) {
		String hexCol = hexColorString(color);
		MutableComponent coloredComponent = Component.literal("#"+hexCol);
		Style coloredStyle = coloredComponent.getStyle().withColor(TextColor.fromRgb(color));
		coloredComponent.setStyle(coloredStyle);
		return base.append(coloredComponent);
	}
}
