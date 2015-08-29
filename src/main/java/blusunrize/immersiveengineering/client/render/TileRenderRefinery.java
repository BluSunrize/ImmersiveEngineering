package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelRefinery;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;

public class TileRenderRefinery extends TileEntitySpecialRenderer
{
	static ModelRefinery model = new ModelRefinery();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityRefinery refinery = (TileEntityRefinery)tile;
		if(!refinery.formed || refinery.pos!=17)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glTranslated(+.5, +1.5, +.5);
		GL11.glScalef(1.0F, -1.0F, -1.0F);

		model.base.rotateAngleY=(float) Math.toRadians(refinery.facing==2?180: refinery.facing==4?90: refinery.facing==5?-90: 0);

		ClientUtils.bindTexture("immersiveengineering:textures/models/refinery.png");

		model.render(null, 0, 0, 0, 0, 0, .0625f);

		GL11.glPopMatrix();
	}

}