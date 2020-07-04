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
		coloredComponent.getStyle().func_240718_a_(Color.func_240743_a_(color));
		Style coloredStyle = coloredComponent.getStyle().func_240718_a_(Color.func_240743_a_(color));
		coloredComponent.func_230530_a_(coloredStyle);
		return base.func_230529_a_(coloredComponent);
	}
}
