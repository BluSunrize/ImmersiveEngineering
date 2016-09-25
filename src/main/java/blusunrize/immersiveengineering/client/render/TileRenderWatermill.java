package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWatermill;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;

public class TileRenderWatermill extends TileEntitySpecialRenderer<TileEntityWatermill>
{
	private static BakedModelTESRWrapper tesrWrapper;
	@Override
	public void renderTileEntityAt(TileEntityWatermill tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if (tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		if (tesrWrapper==null)
		{
			final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
			IBlockState state = tile.getWorld().getBlockState(tile.getPos());
			tesrWrapper = new BakedModelTESRWrapper(blockRenderer.getModelForState(state), state);
		}
		Tessellator tessellator = Tessellator.getInstance();
		GlStateManager.pushMatrix();

		GlStateManager.translate(x + .5, y + .5, z + .5);
		GlStateManager.rotate(90, 1, 0, 0);
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		final float dir = tile.facing == EnumFacing.NORTH ? 180 : tile.facing == EnumFacing.SOUTH ? 0 : tile.facing == EnumFacing.WEST ? 90 : -90;
		float wheelRotation = 360 * (tile.rotation + (!tile.canTurn || tile.rotation == 0 ? 0 : partialTicks)*(float)tile.perTick);
		if(tile.facing.getAxisDirection() == AxisDirection.NEGATIVE)
			wheelRotation *= -1;
		final float rot = wheelRotation;
		GlStateManager.rotate(dir, 0, 0, 1);
		GlStateManager.rotate(rot, 0, 1, 0);
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		VertexBuffer worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		tesrWrapper.render(worldRenderer, tile.getWorld().getCombinedLight(tile.getPos(), 0));
		worldRenderer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}
}