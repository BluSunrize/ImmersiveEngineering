package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;
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
		if(preheater.dummy==0 && preheater.energyStorage.getEnergyStored()>=Config.getInt("preheater_consumption"))
		{
			GL11.glPushMatrix();
			GL11.glTranslated(x+.5, y+.5, z+.5);
			GL11.glRotatef(preheater.facing==2?180f: preheater.facing==4?-90f: preheater.facing==5?90f :0, 0,1,0);
			ClientUtils.bindAtlas(0);
			
			float step = tile.getWorldObj().getTotalWorldTime()%18;
			float angle = step*20f + (f*20);
			angle %= 360;
			GL11.glRotatef(angle, 0,0,1);
			TileRenderBlastFurnaceAdvanced.model.model.renderOnly("fan");
			GL11.glRotatef(-angle, 0,0,1);
			
			GL11.glScalef(1.001f, .94f, 1.001f);
			TileRenderBlastFurnaceAdvanced.model.model.renderOnly("preheater_active");
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
