package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

public class TileRenderDieselGenerator extends TileEntitySpecialRenderer<TileEntityDieselGenerator>
{
	@Override
	public void renderTileEntityAt(TileEntityDieselGenerator te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed || te.isDummy() || !te.getWorld().isBlockLoaded(te.getPos(), false))
			return;
		
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock() != IEContent.blockMetalMultiblock)
			return;
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

//		boolean b = te.getEnergyStored(null)>0 && !te.isRSDisabled() && !te.processQueue.isEmpty();
//		float angle = te.animation_barrelRotation+(b?18*partialTicks:0);
		
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer worldRenderer = tessellator.getBuffer();
		
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(.5, .6875, .5);
		

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
//		GlStateManager.translate(te.facing.getFrontOffsetX()*.25, 0, te.facing.getFrontOffsetZ()*.25);
//		float angle = ClientUtils.mc().thePlayer.ticksExisted%100/100f;
//		GlStateManager.rotate(angle*360, te.facing.getFrontOffsetX(), 0, te.facing.getFrontOffsetZ());
		GlStateManager.rotate(te.animation_fanRotation+(te.animation_fanRotationStep*partialTicks), te.facing.getFrontOffsetX(), 0, te.facing.getFrontOffsetZ());
		
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation( -.5-blockPos.getX(), - blockPos.getY(),  -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, blockPos, worldRenderer,true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		
		RenderHelper.enableStandardItemLighting();
		
		GlStateManager.popMatrix();
	}
}