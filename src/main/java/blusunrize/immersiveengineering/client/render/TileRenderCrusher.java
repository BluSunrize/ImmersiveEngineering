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

		boolean b = (crusher.active&&crusher.process>0)||crusher.mobGrinding||crusher.grindingTimer>0;
		model.axle1.rotateAngleX = (float) Math.toRadians(crusher.barrelRotation+(b?18*f:0));
		model.axle2.rotateAngleX = (float) Math.toRadians(-crusher.barrelRotation-(b?18*f:0));

		model.render(null, 0, 0, 0, 0, 0, .0625f);
		GL11.glTranslated(0,-1,0);
		GL11.glScalef(.75F, -.75F, -.75F);

		GL11.glPopMatrix();
	}

}