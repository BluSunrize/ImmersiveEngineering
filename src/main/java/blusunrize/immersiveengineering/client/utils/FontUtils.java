package blusunrize.immersiveengineering.client.utils;

import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class FontUtils
{
	public static String hexColorString(int color)
	{
		StringBuilder hexCol = new StringBuilder(Integer.toHexString(color));
		while(hexCol.length() < 6)
			hexCol.insert(0, "0");
		return hexCol.toString();
	}

	public static IFormattableTextComponent withAppendColoredColour(IFormattableTextComponent base, int color) {
		String hexCol = hexColorString(color);
		IFormattableTextComponent coloredComponent = new StringTextComponent("#"+hexCol);
		Style coloredStyle = coloredComponent.getStyle().setColor(Color.fromInt(color));
		coloredComponent.setStyle(coloredStyle);
		return base.appendSibling(coloredComponent);
	}
}
