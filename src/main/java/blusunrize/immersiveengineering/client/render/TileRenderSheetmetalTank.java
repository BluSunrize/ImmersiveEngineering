package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySheetmetalTank;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fluids.FluidStack;

public class TileRenderSheetmetalTank extends TileEntitySpecialRenderer<TileEntitySheetmetalTank>
{
	@Override
	public void renderTileEntityAt(TileEntitySheetmetalTank tile, double x, double y, double z, float f, int destroyStage)
	{
		if(!tile.formed || tile.pos!=4||!tile.getWorld().isBlockLoaded(tile.getPos()))
			return;
		GlStateManager.pushMatrix();

		GlStateManager.translate(x+.5, y, z+.5);

		FluidStack fs = tile.tank.getFluid();
		GlStateManager.translate(0,3.5f,0);
		float baseScale = .0625f;
		GlStateManager.scale(baseScale,-baseScale,baseScale);

		float xx = -.5f;
		float zz = 1.5f-.004f;
		xx/=baseScale;
		zz/=baseScale;
		for(int i=0; i<4; i++)
		{
			GlStateManager.translate(xx,0,zz);

			GlStateManager.disableTexture2D();
			GlStateManager.enableBlend();
			GlStateManager.disableAlpha();
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			GlStateManager.disableLighting();
			
			WorldRenderer worldrenderer = ClientUtils.tes().getWorldRenderer();
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos(-4, -4, 0).color(0x22,0x22,0x22,0xff).endVertex();
			worldrenderer.pos(-4, 20, 0).color(0x22,0x22,0x22,0xff).endVertex();
			worldrenderer.pos(20, 20, 0).color(0x22,0x22,0x22,0xff).endVertex();
			worldrenderer.pos(20, -4, 0).color(0x22,0x22,0x22,0xff).endVertex();
			ClientUtils.tes().draw();
			GlStateManager.enableLighting();
			GlStateManager.shadeModel(GL11.GL_FLAT);
			GlStateManager.disableBlend();
			GlStateManager.enableAlpha();
			GlStateManager.enableTexture2D();

			if(fs!=null)
			{
				float h = fs.amount/(float)tile.tank.getCapacity();
				GlStateManager.depthMask(false);
				GlStateManager.translate(0,0,.004f);
				ClientUtils.drawRepeatedFluidSprite(fs.getFluid(), 0,0+(1-h)*16, 16,h*16);
				GlStateManager.translate(0,0,-.004f);
				GlStateManager.depthMask(true);
			}

			GlStateManager.translate(-xx,0,-zz);
			GlStateManager.rotate(90, 0,1,0);
			GlStateManager.enableAlpha();
			GlStateManager.alphaFunc(516, 0.1F);
			GlStateManager.enableBlend();
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		}
		GlStateManager.popMatrix();
	}

}