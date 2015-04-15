package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelConnectorMV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorMV;

public class TileRenderConnectorMV extends TileEntitySpecialRenderer
{
	static ModelConnectorMV model = new ModelConnectorMV();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityConnectorMV connector = (TileEntityConnectorMV)tile;

		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glPushMatrix();
		GL11.glTranslated(.5, .5, .5);
		switch(connector.facing)
		{
		case 0:
			GL11.glTranslated(0,-.4375,0);
			GL11.glRotatef(180, 1, 0, 0);
			break;
		case 1:
			GL11.glTranslated(0,.4375,0);
			break;
		case 2:
			GL11.glTranslated(0,0,-.4375);
			GL11.glRotatef(-90, 1, 0, 0);
			break;
		case 3:
			GL11.glTranslated(0,0,.4375);
			GL11.glRotatef(90, 1, 0, 0);
			break;
		case 4:
			GL11.glTranslated(-.4375,0,0);
			GL11.glRotatef(90, 0, 0, 1);
			break;
		case 5:
			GL11.glTranslated(.4375,0,0);
			GL11.glRotatef(-90, 0, 0, 1);
			break;
		}
		ClientUtils.bindTexture("immersiveengineering:textures/models/connectorHV.png");
		model.render(null, 0, 0, 0, 0, 0, .0625f);
		GL11.glPopMatrix();

		ClientUtils.renderAttachedConnections(connector);
		
		GL11.glPopMatrix();
	}

}