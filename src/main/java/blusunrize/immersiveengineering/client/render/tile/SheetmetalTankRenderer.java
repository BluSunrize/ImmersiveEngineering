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
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

public class SheetmetalTankRenderer extends TileEntityRenderer<SheetmetalTankTileEntity>
{
	@Override
	public void render(SheetmetalTankTileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!tile.formed||tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		GlStateManager.pushMatrix();

		GlStateManager.translated(x+.5, y, z+.5);

		FluidStack fs = tile.tank.getFluid();
		GlStateManager.translated(0, 3.5f, 0);
		float baseScale = .0625f;
		GlStateManager.scalef(baseScale, -baseScale, baseScale);

		float xx = -.5f;
		float zz = 1.5f-.004f;
		xx /= baseScale;
		zz /= baseScale;
		for(int i = 0; i < 4; i++)
		{
			GlStateManager.translated(xx, 0, zz);

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
				GlStateManager.translated(0, 0, .004f);
				ClientUtils.drawRepeatedFluidSprite(fs, 0, 0+(1-h)*16, 16, h*16);
				GlStateManager.translated(0, 0, -.004f);
				GlStateManager.depthMask(true);
			}

			GlStateManager.translated(-xx, 0, -zz);
			GlStateManager.rotatef(90, 0, 1, 0);
			GlStateManager.enableAlphaTest();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			GlStateManager.blendFuncSeparate(770, 771, 1, 0);
		}
		GlStateManager.popMatrix();
	}

}