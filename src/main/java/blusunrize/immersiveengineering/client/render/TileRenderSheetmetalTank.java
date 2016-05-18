package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySheetmetalTank;
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
		GL11.glPushMatrix();

		GL11.glTranslated(x+.5, y, z+.5);

		FluidStack fs = tile.tank.getFluid();
		GL11.glTranslatef(0,3.5f,0);
		float baseScale = .0625f;
		GL11.glScalef(baseScale,-baseScale,baseScale);

		float xx = -.5f;
		float zz = 1.5f-.004f;
		xx/=baseScale;
		zz/=baseScale;
		for(int i=0; i<4; i++)
		{
			GL11.glTranslatef(xx,0,zz);

			GL11.glDisable(GL11.GL_TEXTURE_2D);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_ALPHA_TEST);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			GL11.glDisable(GL11.GL_LIGHTING);
			
			WorldRenderer worldrenderer = ClientUtils.tes().getWorldRenderer();
			worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos(-4, -4, 0).color(0x22,0x22,0x22,0xff).endVertex();
			worldrenderer.pos(-4, 20, 0).color(0x22,0x22,0x22,0xff).endVertex();
			worldrenderer.pos(20, 20, 0).color(0x22,0x22,0x22,0xff).endVertex();
			worldrenderer.pos(20, -4, 0).color(0x22,0x22,0x22,0xff).endVertex();
			ClientUtils.tes().draw();
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_ALPHA_TEST);
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			if(fs!=null)
			{
				float h = fs.amount/(float)tile.tank.getCapacity();
				GL11.glDepthMask(false);
				GL11.glTranslatef(0,0,.004f);
				ClientUtils.drawRepeatedFluidSprite(fs.getFluid(), 0,0+(1-h)*16, 16,h*16);
				GL11.glTranslatef(0,0,-.004f);
				GL11.glDepthMask(true);
			}

			GL11.glTranslatef(-xx,0,-zz);
			GL11.glRotatef(90, 0,1,0);

			GL11.glEnable(3008);
			GL11.glAlphaFunc(516, 0.1F);
			GL11.glEnable(3042);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		}
		GL11.glPopMatrix();
	}

}