package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityTurret;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.Properties;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

public class TileRenderTurret extends TileEntitySpecialRenderer<TileEntityTurret>
{
	@Override
	public void renderTileEntityAt(TileEntityTurret tile, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(tile.isDummy()||!tile.getWorld().isBlockLoaded(tile.getPos(), false))
			return;

		//Grab model + correct eextended state
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = tile.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock() != IEContent.blockMetalDevice1)
			return;
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
		if(state instanceof IExtendedBlockState)
			state = ((IExtendedBlockState)state).withProperty(Properties.AnimationProperty, new OBJState(Arrays.asList("gun"), true));

		//Initialize Tesselator and VertexBuffer
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldRenderer = tessellator.getBuffer();
		//Outer GL Wrapping, initial translation
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+0.9375, z+.5);
		ClientUtils.bindAtlas();

		GlStateManager.rotate(tile.rotationYaw, 0,1,0);
		GlStateManager.rotate(tile.rotationPitch, tile.facing.getFrontOffsetZ(),0,-tile.facing.getFrontOffsetX());

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-tile.getPos().getX(), -.5-tile.getPos().getY(), -.5-tile.getPos().getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, state, tile.getPos(), worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		GlStateManager.popMatrix();
	}

}