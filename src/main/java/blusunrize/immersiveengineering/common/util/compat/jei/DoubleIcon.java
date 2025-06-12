/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.jei;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.gui.GuiGraphics;

/**
 * Heavily inspired by Create, which is published under MIT license.
 * Thanks folks!
 */
public record DoubleIcon(IDrawable main, IDrawable secondary, float scale) implements IDrawable
{
	@Override
	public int getWidth()
	{
		return 18;
	}

	@Override
	public int getHeight()
	{
		return 18;
	}

	@Override
	public void draw(GuiGraphics graphics, int xOffset, int yOffset)
	{
		PoseStack matrixStack = graphics.pose();

		RenderSystem.enableDepthTest();
		matrixStack.pushPose();
		matrixStack.translate(xOffset, yOffset, 0);
		main.draw(graphics, 1, 1);

		matrixStack.pushPose();
		matrixStack.translate(18-scale*secondary.getWidth(), 18-scale*secondary.getHeight(), 100);
		matrixStack.scale(scale, scale, scale);
		secondary.draw(graphics, 0, 0);
		matrixStack.popPose();

		matrixStack.popPose();
	}

}
