package blusunrize.immersiveengineering.client.render;

import net.minecraft.tileentity.TileEntity;
import blusunrize.immersiveengineering.client.ClientEventHandler;

public abstract class TileRenderImmersiveConnectable extends TileRenderIE
{
	@Override
	public void renderDynamic(TileEntity tile, double x, double y, double z, float f)
	{
		ClientEventHandler.renderAllIEConnections(f);
	}
}