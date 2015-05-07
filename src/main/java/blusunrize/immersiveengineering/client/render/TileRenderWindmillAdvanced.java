package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelWindmillAdvanced;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;

public class TileRenderWindmillAdvanced extends TileEntitySpecialRenderer
{
	static ModelWindmillAdvanced model = new ModelWindmillAdvanced();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityWindmillAdvanced mill = (TileEntityWindmillAdvanced)tile;
		GL11.glPushMatrix();
		GL11.glTranslated(x+.5, y+.5, z+.5);
		GL11.glRotated(mill.facing==2?0: mill.facing==3?180: mill.facing==4?90: -90, 0, 1, 0);

		model.setRotateAngle(model.axel, 0, 0, -(float)Math.toRadians(360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		model.setRotateAngle(model.sail1, 0, 0, -(float)Math.toRadians(360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		model.setRotateAngle(model.sail2, 0, 0, -(float)Math.toRadians(45+360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		model.setRotateAngle(model.sail3, 0, 0, -(float)Math.toRadians(360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		model.setRotateAngle(model.sail4, 0, 0, -(float)Math.toRadians(45+360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		model.setRotateAngle(model.sail5, 0, 0, -(float)Math.toRadians(360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		model.setRotateAngle(model.sail6, 0, 0, -(float)Math.toRadians(45+360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		model.setRotateAngle(model.sail7, 0, 0, -(float)Math.toRadians(360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		model.setRotateAngle(model.sail8, 0, 0, -(float)Math.toRadians(45+360*mill.rotation + (mill.rotation == 0? 0 :  f)));
		ClientUtils.bindTexture("immersiveengineering:textures/models/windmillAdvanced.png");
		model.render(EntitySheep.fleeceColorTable[15-mill.dye], .0625f);

		GL11.glPopMatrix();
	}

}