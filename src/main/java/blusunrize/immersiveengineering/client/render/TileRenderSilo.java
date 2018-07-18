/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySilo;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;

public class TileRenderSilo extends TileEntitySpecialRenderer<TileEntitySilo>
{
	@Override
	public void render(TileEntitySilo tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(!tile.formed||tile.pos!=4||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		GlStateManager.pushMatrix();

		GlStateManager.translate(x+.5, y, z+.5);

		if(!tile.identStack.isEmpty())
		{
			GlStateManager.translate(0, 5, 0);
			float baseScale = .0625f;
			float itemScale = .75f;
			float flatScale = .001f;
			baseScale *= itemScale;
			float textScale = .375f;
			GlStateManager.scale(baseScale, -baseScale, baseScale);
			ItemStack stack = Utils.copyStackWithAmount(tile.identStack, tile.storageAmount);
			String s = ""+stack.getCount();
			float w = this.getFontRenderer().getStringWidth(s);

			float xx = -.5f*itemScale;
			float zz = 1.501f;
			xx /= baseScale;
			zz /= baseScale;
			w *= textScale;
			for(int i = 0; i < 4; i++)
			{
				GlStateManager.pushMatrix();
				GlStateManager.translate(xx, 0, zz);
				GlStateManager.scale(1, 1, flatScale);
				ClientUtils.mc().getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
				GlStateManager.scale(1, 1, 1/flatScale);

				GlStateManager.disableLighting();
				GlStateManager.depthMask(false);
				GlStateManager.translate(8-w/2, 17, .001f);
				GlStateManager.scale(textScale, textScale, 1);
				ClientUtils.font().drawString(""+stack.getCount(), 0, 0, 0x888888, true);
				GlStateManager.scale(1/textScale, 1/textScale, 1);
				GlStateManager.translate(-(8-w/2), -17, -.001f);
				GlStateManager.depthMask(true);
				GlStateManager.enableLighting();

				GlStateManager.translate(-xx, 0, -zz);
				GlStateManager.popMatrix();
				GlStateManager.rotate(90, 0, 1, 0);

				GlStateManager.enableAlpha();
				GlStateManager.alphaFunc(516, 0.1F);
				GlStateManager.enableBlend();
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			}
		}
		GlStateManager.popMatrix();
	}

}