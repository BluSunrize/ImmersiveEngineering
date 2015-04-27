package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelWindmill;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;

public class TileRenderWindmill extends TileEntitySpecialRenderer
{
	static ModelWindmill model = new ModelWindmill();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityWindmill mill = (TileEntityWindmill)tile;

		GL11.glPushMatrix();
		GL11.glTranslated(x+.5, y+.5, z+.5);

		GL11.glRotated(mill.facing==3?180: mill.facing==4?90: -90, 0, 1, 0);

		model.setRotateAngle(model.axel, 0, 0, -(float)Math.toRadians(360*mill.rotation));

		ClientUtils.bindTexture("immersiveengineering:textures/models/windmill.png");
		model.render(null, 0, 0, 0, 0, 0, .0625f);

		GL11.glPopMatrix();
	}

}