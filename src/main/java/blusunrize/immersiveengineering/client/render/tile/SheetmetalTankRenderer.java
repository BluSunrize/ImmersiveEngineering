/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.SheetmetalTankTileEntity;
import blusunrize.immersiveengineering.dummy.GlStateManager;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class SheetmetalTankRenderer extends TileEntityRenderer<SheetmetalTankTileEntity>
{
	public SheetmetalTankRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(SheetmetalTankTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		matrixStack.push();

		matrixStack.translate(.5, 0, .5);

		FluidStack fs = tile.tank.getFluid();
		matrixStack.translate(0, 3.5f, 0);
		float baseScale = .0625f;
		matrixStack.scale(baseScale, -baseScale, baseScale);

		float xx = -.5f;
		float zz = 1.5f-.004f;
		xx /= baseScale;
		zz /= baseScale;
		for(int i = 0; i < 4; i++)
		{
			matrixStack.translate(xx, 0, zz);

			GlStateManager.disableTexture();
			GlStateManager.enableBlend();
			GlStateManager.disableAlphaTest();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableLighting();

			BufferBuilder worldrenderer = ClientUtils.tes().getBuffer();
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos(-4, -4, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			worldrenderer.pos(-4, 20, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			worldrenderer.pos(20, 20, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			worldrenderer.pos(20, -4, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			ClientUtils.tes().draw();
			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.disableBlend();
			GlStateManager.enableAlphaTest();
			GlStateManager.enableTexture();

			if(!fs.isEmpty())
			{
				float h = fs.getAmount()/(float)tile.tank.getCapacity();
				GlStateManager.depthMask(false);
				matrixStack.translate(0, 0, .004f);
				ClientUtils.drawRepeatedFluidSprite(bufferIn, matrixStack, fs, 0, 0+(1-h)*16, 16, h*16);
				matrixStack.translate(0, 0, -.004f);
				GlStateManager.depthMask(true);
			}

			matrixStack.translate(-xx, 0, -zz);
			matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), 90, true));
			GlStateManager.enableAlphaTest();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		}
		matrixStack.pop();
	}

}