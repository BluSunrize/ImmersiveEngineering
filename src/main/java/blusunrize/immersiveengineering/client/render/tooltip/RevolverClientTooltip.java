/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.render.tooltip;

import blusunrize.immersiveengineering.client.gui.RevolverScreen;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;

public record RevolverClientTooltip(RevolverServerTooltip data) implements ClientTooltipComponent
{
	@Override
	public int getHeight()
	{
		return 40;
	}

	@Override
	public int getWidth(Font pFont)
	{
		return 40;
	}

	@Override
	public void renderImage(Font pFont, int pMouseX, int pMouseY, PoseStack pPoseStack, ItemRenderer pItemRenderer, int pBlitOffset, TextureManager pTextureManager)
	{
		pPoseStack.pushPose();
		pPoseStack.translate(pMouseX, pMouseY, pBlitOffset);
		pPoseStack.scale(.5f, .5f, 1);
		RevolverScreen.drawExternalGUI(data.bullets(), data.bulletCount(), pPoseStack);
		pPoseStack.popPose();
	}
}
