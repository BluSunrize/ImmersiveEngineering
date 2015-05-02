package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelCrusher;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityCrusher;

public class TileRenderCrusher extends TileEntitySpecialRenderer
{
	static ModelCrusher model = new ModelCrusher();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityCrusher crusher = (TileEntityCrusher)tile;
		if(!crusher.formed || crusher.pos!=17)
			return;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glTranslated(+.5, +1.5, +.5);
	      GL11.glScalef(1.0F, -1.0F, -1.0F);

		model.base.rotateAngleY=(float) Math.toRadians(crusher.facing==2?180: crusher.facing==4?90: crusher.facing==5?-90: 0);

		ClientUtils.bindTexture("immersiveengineering:textures/models/crusher.png");

		model.axle1.rotateAngleX = (float) Math.toRadians(crusher.barrelRotation);
		model.axle2.rotateAngleX = (float) Math.toRadians(-crusher.barrelRotation);
		
		model.render(null, 0, 0, 0, 0, 0, .0625f);

		GL11.glPopMatrix();
	}

}