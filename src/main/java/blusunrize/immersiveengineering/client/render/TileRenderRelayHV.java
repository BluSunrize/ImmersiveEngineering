package blusunrize.immersiveengineering.client.render;

import java.util.Iterator;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.ImmersiveNetHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.ModelRelayHV;
import blusunrize.immersiveengineering.common.Utils;
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
		GL11.glPushMatrix();
		GL11.glTranslated(.5, 1, .5);
		GL11.glRotated(180, 1, 0, 0);
		ClientUtils.bindTexture("immersiveengineering:textures/models/relayHV.png");
		model.render(null, 0, 0, 0, 0, 0, .0625f);
		GL11.glPopMatrix();

		if(tile.getWorldObj()!=null)
		{
			ClientUtils.bindTexture("immersiveengineering:textures/models/white.png");
			Iterator<ImmersiveNetHandler.Connection> itCon = ImmersiveNetHandler.getConnections(connector.getWorldObj(), Utils.toCC(connector)).iterator();
			while(itCon.hasNext())
			{
				ImmersiveNetHandler.Connection con = itCon.next();
				TileEntity tileEnd = connector.getWorldObj().getTileEntity(con.end.posX,con.end.posY,con.end.posZ);
				if(tileEnd instanceof IImmersiveConnectable)
					ClientUtils.drawConnection(con, connector, Utils.toIIC(tileEnd, connector.getWorldObj()));
			}
		}
		GL11.glPopMatrix();
	}
}