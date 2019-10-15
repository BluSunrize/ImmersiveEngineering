/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.SqueezerTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

public class SqueezerRenderer extends TileEntityRenderer<SqueezerTileEntity>
{
	@Override
	public void render(SqueezerTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.squeezer)
			return;
		//TODO state = state.with(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);
		if(te.getIsMirrored())
			GlStateManager.scalef(te.getFacing().getXOffset()==0?-1: 1, 1, te.getFacing().getZOffset()==0?-1: 1);

		float piston = te.animation_piston;
		//Smoothstep!
		piston = piston*piston*(3.0f-2.0f*piston);

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
		GlStateManager.translated(0, piston, 0);

		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-blockPos.getX(), -.5-blockPos.getY(), -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorldNonnull(), model, state, blockPos, worldRenderer, true,
				getWorld().rand, 0, EmptyModelData.INSTANCE);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}
}