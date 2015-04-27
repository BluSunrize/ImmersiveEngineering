package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelRelayHV;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityConnectorLV;

public class TileRenderRelayHV extends TileEntitySpecialRenderer
{
	static ModelRelayHV model = new ModelRelayHV();

	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float f)
	{
		TileEntityConnectorLV connector = (TileEntityConnectorLV)tile;

		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		ClientUtils.bindTexture("immersiveengineering:textures/models/relayHV.png");
		model.render(null, 0, 0, 0, 0, 0, .0625f);

		ClientUtils.renderAttachedConnections(connector);
		
		GL11.glPopMatrix();
	}
}