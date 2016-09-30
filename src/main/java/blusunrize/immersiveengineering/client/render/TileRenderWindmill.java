package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmill;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class TileRenderWindmill extends TileEntitySpecialRenderer<TileEntityWindmill>
{
	private List<BakedQuad> quads;
	@Override
	public void renderTileEntityAt(TileEntityWindmill tile, double x, double y, double z, float partialTicks, int destroyStage)
	//	public void renderTileEntityFast(TileEntityWindmill tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer vertexBuffer)
	{
		if (!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		if (quads==null)
		{
			IBlockState state = getWorld().getBlockState(blockPos);
			state = state.getActualState(getWorld(), blockPos);
			state = state.withProperty(IEProperties.FACING_ALL, EnumFacing.NORTH);
			quads = blockRenderer.getBlockModelShapes().getModelForState(state).getQuads(state, null, 0);
		}
		Tessellator tessellator = Tessellator.getInstance();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + .5, y + .5, z + .5);

		float dir = tile.facing == EnumFacing.SOUTH ? 180 : tile.facing == EnumFacing.NORTH ? 0 : tile.facing == EnumFacing.EAST ? -90 : 90;
		float rot = 360 * (tile.rotation + (!tile.canTurn || tile.rotation == 0 ? 0 : partialTicks)*tile.perTick);
		if(tile.facing.getAxisDirection() == AxisDirection.NEGATIVE)
			rot *= -1;

		GlStateManager.rotate(rot, tile.facing.getAxis() == Axis.X ? 1 : 0, 0, tile.facing.getAxis() == Axis.Z ? 1 : 0);
		GlStateManager.rotate(dir, 0, 1, 0);
		
		RenderHelper.disableStandardItemLighting();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		VertexBuffer worldRenderer = tessellator.getBuffer();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5, -.5, -.5);
		ClientUtils.renderModelTESR(quads, worldRenderer, tile.getWorld().getCombinedLight(tile.getPos(), 0));
		worldRenderer.setTranslation(0, 0, 0);
		tessellator.draw();
		GlStateManager.popMatrix();
	    RenderHelper.enableStandardItemLighting();
		GlStateManager.disableBlend();
		GlStateManager.enableCull();
	}
}