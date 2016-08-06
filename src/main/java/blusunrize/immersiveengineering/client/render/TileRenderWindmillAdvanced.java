package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.common.blocks.wooden.TileEntityWindmillAdvanced;
import com.google.common.collect.Lists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;

public class TileRenderWindmillAdvanced extends TileEntitySpecialRenderer<TileEntityWindmillAdvanced>
{
	@Override
	public void renderTileEntityAt(TileEntityWindmillAdvanced tile, double x, double y, double z, float f, int destroyStage)
	{
		if (!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		state = state.getActualState(getWorld(), blockPos);
//		IBakedModel model = blockRenderer.getModelFromBlockState(state, getWorld(), blockPos);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(Lists.newArrayList(OBJModel.Group.ALL), true));

		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldRenderer = tessellator.getBuffer();
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);

		GlStateManager.rotate(90, 0, 0, 1);

		float dir = tile.facing == EnumFacing.NORTH ? 180 : tile.facing == EnumFacing.SOUTH ? 0 : tile.facing == EnumFacing.WEST ? -90 : -90;
		float rot = 360 * tile.rotation - (!tile.canTurn || tile.rotation == 0 || tile.rotation - tile.prevRotation < 4 ? 0 : tile.facing.getAxis() == Axis.X ? -f : f);
		if(tile.facing.getAxisDirection() == AxisDirection.NEGATIVE)
			rot *= -1;
		GlStateManager.rotate(dir, 0, 0, 1);
		GlStateManager.rotate(rot, tile.facing.getAxis() == Axis.X ? 1 : 0, 0, tile.facing.getAxis() == Axis.Z ? 1 : 0);

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation( -.5-blockPos.getX(), -.5- blockPos.getY(),  -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		GlStateManager.popMatrix();
		RenderHelper.enableStandardItemLighting();
	}
}