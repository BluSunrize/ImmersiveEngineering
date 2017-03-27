package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.TileEntityCoresample;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;

public class TileRenderCoresample extends TileEntitySpecialRenderer<TileEntityCoresample>
{
	@Override
	public void renderTileEntityAt(TileEntityCoresample tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!tile.getWorld().isBlockLoaded(tile.getPos(), false) || tile.coresample==null)
			return;

		GlStateManager.pushMatrix();
		GlStateManager.disableLighting();
		GlStateManager.translate(x+.5,y+.54864,z+.52903);
		GlStateManager.rotate(tile.facing==EnumFacing.NORTH?180:tile.facing==EnumFacing.WEST?-90: tile.facing==EnumFacing.EAST?90:0, 0,1,0);
		GlStateManager.rotate(-45,1,0,0);
		ClientUtils.mc().getRenderItem().renderItem(tile.coresample, ItemCameraTransforms.TransformType.FIXED);
		GlStateManager.enableLighting();
		GlStateManager.popMatrix();
	}
}