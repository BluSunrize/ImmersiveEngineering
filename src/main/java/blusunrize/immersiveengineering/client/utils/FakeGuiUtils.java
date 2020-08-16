package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.ITextComponent;

import java.util.List;

public class FakeGuiUtils
{
	public static void drawHoveringText(MatrixStack transform, List<ITextComponent> tooltip, int mx, int my, int width, int height, int i, FontRenderer font) {
		//TODO this is temporary until Forge's GuiUtils.drawHoveringText is fixed
	}
}
