/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.SiloTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.item.ItemStack;

public class SiloRenderer extends TileEntityRenderer<SiloTileEntity>
{
	@Override
	public void render(SiloTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!tile.formed||tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos()))
			return;
		GlStateManager.pushMatrix();

		GlStateManager.translated(x+.5, y, z+.5);

		if(!tile.identStack.isEmpty())
		{
			GlStateManager.translated(0, 5, 0);
			float baseScale = .0625f;
			float itemScale = .75f;
			float flatScale = .001f;
			baseScale *= itemScale;
			float textScale = .375f;
			GlStateManager.scalef(baseScale, -baseScale, baseScale);
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
				GlStateManager.translated(xx, 0, zz);
				GlStateManager.scalef(1, 1, flatScale);
				ClientUtils.mc().getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
				GlStateManager.scalef(1, 1, 1/flatScale);

				GlStateManager.disableLighting();
				GlStateManager.depthMask(false);
				GlStateManager.translated(8-w/2, 17, .001f);
				GlStateManager.scalef(textScale, textScale, 1);
				ClientUtils.font().drawStringWithShadow(""+stack.getCount(), 0, 0, 0x888888);
				GlStateManager.scalef(1/textScale, 1/textScale, 1);
				GlStateManager.translated(-(8-w/2), -17, -.001f);
				GlStateManager.depthMask(true);
				GlStateManager.enableLighting();

				GlStateManager.translated(-xx, 0, -zz);
				GlStateManager.popMatrix();
				GlStateManager.rotatef(90, 0, 1, 0);

				GlStateManager.enableAlphaTest();
				GlStateManager.alphaFunc(516, 0.1F);
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			}
		}
		GlStateManager.popMatrix();
	}

}