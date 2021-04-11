package blusunrize.immersiveengineering.api.client;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

public class TextUtils
{
	public static IFormattableTextComponent applyFormat(ITextComponent component, TextFormatting... color)
	{
		Style style = component.getStyle();
		for(TextFormatting format : color)
			style = style.applyFormatting(format);
		return component.deepCopy().setStyle(style);
	}
}
