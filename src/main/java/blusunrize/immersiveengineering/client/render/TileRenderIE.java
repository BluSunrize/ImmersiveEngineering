package blusunrize.immersiveengineering.client.render;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;

public abstract class TileRenderIE extends TileEntitySpecialRenderer
{
	@Override
	public void renderTileEntityAt(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (tile.getWorld().isBlockLoaded(tile.getPos()))
			renderDynamic(tile, x,y,z, partialTicks, destroyStage);
	}

	public abstract void renderDynamic(TileEntity tile, double x, double y, double z, float partialTicks, int destroyStage);
	public abstract void renderStatic(TileEntity tile, Tessellator tes, Matrix4 translationMatrix, Matrix4 rotationMatrix);
}