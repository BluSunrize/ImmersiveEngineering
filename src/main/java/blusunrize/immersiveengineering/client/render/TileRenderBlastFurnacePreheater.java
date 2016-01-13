package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBlastFurnacePreheater;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;

public class TileRenderBlastFurnacePreheater extends TileRenderIE
{
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityBlastFurnacePreheater preheater = (TileEntityBlastFurnacePreheater)tile;
		if(preheater.dummy==0)
		{
			GL11.glPushMatrix();
			GL11.glTranslated(x+.5, y+.5, z+.5);
			GL11.glRotatef(preheater.facing==2?180f: preheater.facing==4?-90f: preheater.facing==5?90f :0, 0,1,0);
			ClientUtils.bindAtlas(0);
			if (preheater.active)
			{
				long tick = tile.getWorldObj().getTotalWorldTime();
				if (tick>preheater.lastRenderTick)
				{
					int dif = (int) (preheater.lastRenderTick==-1?0:tick-preheater.lastRenderTick);
					preheater.angle+=20f*dif;
					preheater.angle %= 360;
					preheater.lastRenderTick = tick;
				}
			}
			GL11.glRotatef(preheater.angle+(preheater.active ? (f * 20) : 0), 0,0,1);
			TileRenderBlastFurnaceAdvanced.model.model.renderOnly("fan");
			GL11.glRotatef(-preheater.angle-(preheater.active ? (f * 20) : 0), 0,0,1);

			if (preheater.active)
			{
				GL11.glScalef(1.001f, .94f, 1.001f);
				TileRenderBlastFurnaceAdvanced.model.model.renderOnly("preheater_active");
			}
			GL11.glPopMatrix();
		}
	}

	@Override
	public void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix)
	{
		TileEntityBlastFurnacePreheater preheater = (TileEntityBlastFurnacePreheater)tile;
		translationMatrix.translate(.5, .5, .5);
		rotationMatrix.rotate(Math.toRadians(preheater.facing==2?180: preheater.facing==4?-90: preheater.facing==5?90 :0), 0,1,0);
		TileRenderBlastFurnaceAdvanced.model.render(tile, tes, translationMatrix, rotationMatrix, tile.getWorldObj()==null?-1:0, false, "preheater");
	}
}
