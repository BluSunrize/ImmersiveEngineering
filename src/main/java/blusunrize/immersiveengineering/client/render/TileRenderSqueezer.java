package blusunrize.immersiveengineering.client.render;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntitySqueezer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.model.ISmartBlockModel;

public class TileRenderSqueezer extends TileEntitySpecialRenderer<TileEntitySqueezer>
{
	@Override
	public void renderTileEntityAt(TileEntitySqueezer te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(te.isDummy()||!te.getWorld().isBlockLoaded(te.getPos()))
			return;
		
		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);
		
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		
		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);
		if(te.mirrored)
			GlStateManager.scale(te.facing.getFrontOffsetX()==0?-1:1,1,te.facing.getFrontOffsetZ()==0?-1:1);
		
		float piston = te.animation_piston;
		
		
//		float shift[] = new float[te.processQueue.size()];
//		for(int i=0; i<shift.length; i++)
//		{
//			MultiblockProcess process = te.processQueue.get(i);
//			if(process==null)
//				continue;
//			float fProcess = process.processTick/(float)process.maxTicks;
//			if(fProcess<.4375f)
//				shift[i] = fProcess/.4375f*.5f;
//			else if(fProcess<.5625f)
//				shift[i] = .5f;
//			else
//				shift[i] = .5f+ (fProcess-.5625f)/.4375f*.5f;
//			if(te.mold!=null)
//				if(fProcess>=.4375f&&fProcess<.5625f)
//					if(fProcess<.46875f)
//						piston = (fProcess-.4375f)/.03125f;
//					else if(fProcess<.53125f)
//						piston = 1;
//					else
//						piston = 1 - (fProcess-.53125f)/.03125f;
//		}
		GlStateManager.translate(0,piston,0);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation( -.5-blockPos.getX(), -.5- blockPos.getY(),  -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		if(model instanceof ISmartBlockModel)
			model = ((ISmartBlockModel) model).handleBlockState(state);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, blockPos, worldRenderer);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();
		
		GlStateManager.popMatrix();
	}
}