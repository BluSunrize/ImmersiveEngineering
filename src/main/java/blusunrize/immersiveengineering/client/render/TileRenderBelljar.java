package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.tool.BelljarHandler.IPlantHandler;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityBelljar;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class TileRenderBelljar extends TileEntitySpecialRenderer<TileEntityBelljar>
{
	@Override
	public void renderTileEntityAt(TileEntityBelljar tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.dummy || !tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		IPlantHandler plantHandler = tile.getCurrentPlantHandler();
		if(plantHandler==null)
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();

		ClientUtils.bindAtlas();
		GlStateManager.enableCull();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y+.125, z);
		GlStateManager.rotate(-90,0,1,0);
		GlStateManager.color(1,1,1,1);
		float scale = plantHandler.getRenderSize(tile.getInventory()[1], tile.getInventory()[0], tile.renderGrowth, tile);
		GlStateManager.translate((1-scale)/2, 0, -(1-scale)/2);
		GlStateManager.scale(scale,scale,scale);
		if(!plantHandler.overrideRender(tile.getInventory()[1], tile.getInventory()[0], tile.renderGrowth, tile, blockRenderer))
		{
			IBlockState[] states = plantHandler.getRenderedPlant(tile.getInventory()[1], tile.getInventory()[0], tile.renderGrowth, tile);
			if(states==null || states.length<1)
				return;
			for(IBlockState s : states)
			{
				IBakedModel model = blockRenderer.getModelForState(s);
				GlStateManager.pushMatrix();
				blockRenderer.getBlockModelRenderer().renderModelBrightness(model, s, 1, true);
				GlStateManager.popMatrix();
				GlStateManager.translate(0, 1, 0);
			}
		}
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
	}
}