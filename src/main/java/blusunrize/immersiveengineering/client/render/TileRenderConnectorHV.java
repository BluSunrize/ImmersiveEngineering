package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConnectorHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;

public class TileRenderConnectorHV extends TileEntitySpecialRenderer
{
	static ModelConnectorHV model = new ModelConnectorHV();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityConnectorLV connector = (TileEntityConnectorLV)tile;

		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		switch(connector.facing)
		{
		case 0:
			model.setRotateAngle(model.HVtop, 0, 0, (float)Math.toRadians(180));
			break;
		case 1:
			model.setRotateAngle(model.HVtop, 0, 0, 0);
			break;
		case 2:
			model.setRotateAngle(model.HVtop, (float)Math.toRadians(-90), 0, 0);
			break;
		case 3:
			model.setRotateAngle(model.HVtop, (float)Math.toRadians(90), 0, 0);
			break;
		case 4:
			model.setRotateAngle(model.HVtop, 0, 0, (float)Math.toRadians(90));
			break;
		case 5:
			model.setRotateAngle(model.HVtop, 0, 0, (float)Math.toRadians(-90));
			break;
		}
		
		ClientUtils.bindTexture("immersiveengineering:textures/models/connectorHV.png");
		model.render(null, 0, 0, 0, 0, 0, .0625f);
		
		ClientUtils.renderAttachedConnections(connector);
		
		GL11.glPopMatrix();
	}

}