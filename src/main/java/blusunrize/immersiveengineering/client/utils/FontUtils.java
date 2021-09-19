package blusunrize.immersiveengineering.client.utils;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;

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
		MutableComponent coloredComponent = new TextComponent("#"+hexCol);
		Style coloredStyle = coloredComponent.getStyle().withColor(TextColor.fromRgb(color));
		coloredComponent.setStyle(coloredStyle);
		return base.append(coloredComponent);
	}
}
