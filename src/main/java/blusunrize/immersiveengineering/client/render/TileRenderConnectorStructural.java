package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConnectorStructural;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorStructural;

public class TileRenderConnectorStructural extends TileEntitySpecialRenderer
{
	static ModelConnectorStructural model = new ModelConnectorStructural();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityConnectorStructural connector = (TileEntityConnectorStructural)tile;
//		model = new ModelConnectorStructural();
		
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		float yRot = (float)Math.toRadians(connector.rotation);
		switch(connector.facing)
		{
		case 0:
			model.setRotateAngle(model.base, 0, yRot, (float)Math.toRadians(180));
			break;
		case 1:
			model.setRotateAngle(model.base, 0, yRot, 0);
			break;
		case 2:
			model.setRotateAngle(model.base, (float)Math.toRadians(-90), 0, yRot);
			break;
		case 3:
			model.setRotateAngle(model.base, (float)Math.toRadians(90), 0, yRot);
			break;
		case 4:
			model.setRotateAngle(model.base, 0, yRot, (float)Math.toRadians(90));
			break;
		case 5:
			model.setRotateAngle(model.base, 0, yRot, (float)Math.toRadians(-90));
			break;
		}
		
		ClientUtils.bindTexture("immersiveengineering:textures/models/connectorStructural.png");
		model.render(null, 0, 0, 0, 0, 0, .0625f);
		
		ClientUtils.renderAttachedConnections(connector);
		
		GL11.glPopMatrix();
	}

}