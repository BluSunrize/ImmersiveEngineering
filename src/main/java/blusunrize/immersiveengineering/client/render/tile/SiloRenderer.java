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
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;

public class SiloRenderer extends TileEntityRenderer<SiloTileEntity>
{
	public SiloRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(SiloTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		matrixStack.push();

		matrixStack.translate(.5, 0, .5);

		if(!tile.identStack.isEmpty())
		{
			matrixStack.translate(0, 5, 0);
			float baseScale = .0625f;
			float itemScale = .75f;
			float flatScale = .001f;
			baseScale *= itemScale;
			float textScale = .375f;
			matrixStack.scale(baseScale, -baseScale, baseScale);
			ItemStack stack = Utils.copyStackWithAmount(tile.identStack, tile.storageAmount);
			String s = ""+stack.getCount();
			float w = ClientUtils.mc().fontRenderer.getStringWidth(s);

			float xx = -.5f*itemScale;
			float zz = 1.501f;
			xx /= baseScale;
			zz /= baseScale;
			w *= textScale;
			for(int i = 0; i < 4; i++)
			{
				matrixStack.push();
				matrixStack.translate(xx, 0, zz);
				matrixStack.scale(1, 1, flatScale);
				ClientUtils.mc().getItemRenderer().renderItemAndEffectIntoGUI(stack, 0, 0);
				matrixStack.scale(1, 1, 1/flatScale);

				GlStateManager.disableLighting();
				GlStateManager.depthMask(false);
				matrixStack.translate(8-w/2, 17, .001f);
				matrixStack.scale(textScale, textScale, 1);
				ClientUtils.font().drawStringWithShadow(""+stack.getCount(), 0, 0, 0x888888);
				matrixStack.scale(1/textScale, 1/textScale, 1);
				matrixStack.translate(-(8-w/2), -17, -.001f);
				GlStateManager.depthMask(true);
				GlStateManager.enableLighting();

				matrixStack.translate(-xx, 0, -zz);
				matrixStack.pop();
				matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), 90, true));

				GlStateManager.enableAlphaTest();
				GlStateManager.alphaFunc(516, 0.1F);
				GlStateManager.enableBlend();
				GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			}
		}
		matrixStack.pop();
	}

}