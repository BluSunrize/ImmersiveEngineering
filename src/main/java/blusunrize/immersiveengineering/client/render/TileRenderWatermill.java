package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelWatermill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;

public class TileRenderWatermill extends TileEntitySpecialRenderer
{
	static ModelWatermill model = new ModelWatermill();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityWatermill wheel = (TileEntityWatermill)tile;
		if(wheel.offset[0]!=0||wheel.offset[1]!=0)
			return;

		GL11.glPushMatrix();
		GL11.glTranslated(x+.5, y+.5, z+.5);

		if(wheel.facing==4||wheel.facing==5)
			GL11.glRotated(90, 0, 1, 0);

		float rot = 360*wheel.rotation-(!wheel.canTurn||wheel.rotation==0?0:wheel.facing>3?-f:f);
		model.setRotateAngle(model.Axle, 0, 0, (float)Math.toRadians(rot));
		ClientUtils.bindTexture("immersiveengineering:textures/models/watermill.png");
		model.render(null, 0, 0, 0, 0, 0, .0625f);

		GL11.glPopMatrix();
	}

}