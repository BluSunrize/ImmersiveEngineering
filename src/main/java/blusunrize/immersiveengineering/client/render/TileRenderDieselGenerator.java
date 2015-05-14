package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelDieselGenerator;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;

public class TileRenderDieselGenerator extends TileEntitySpecialRenderer
{
	static ModelDieselGenerator model = new ModelDieselGenerator();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityDieselGenerator gen = (TileEntityDieselGenerator)tile;
		if(!gen.formed || gen.pos!=31)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		model.Generator.rotateAngleZ=(float) Math.toRadians(180);
		GL11.glTranslated(+.5, +.5, +.5);

		model.Generator.rotateAngleY=(float) Math.toRadians(gen.facing==2?180: gen.facing==4?90: gen.facing==5?-90: 0);

		ClientUtils.bindTexture("immersiveengineering:textures/models/dieselGenerator.png");

		model.Fan_axle3.rotateAngleZ = (float) Math.toRadians(gen.fanRotation+(gen.fanRotationStep*f));

		model.render(null, 0, 0, 0, 0, 0, .0625f);

		GL11.glPopMatrix();
	}
}