/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMetalPress;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityMultiblockMetal.MultiblockProcessInWorld;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class TileRenderMetalPress extends TileEntitySpecialRenderer<TileEntityMetalPress>
{
	@Override
	public void render(TileEntityMetalPress te, double x, double y, double z, float partialTicks, int destroyStage, float alpha)
	{
		if(!te.formed||te.isDummy()||!te.getWorld().isBlockLoaded(te.getPos(), false))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=IEContent.blockMetalMultiblock)
			return;
		state = state.getBlock().getActualState(state, getWorld(), blockPos);
		state = state.withProperty(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModelForState(state);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x+.5, y+.5, z+.5);
		float piston = 0;
		float shift[] = new float[te.processQueue.size()];
		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess process = te.processQueue.get(i);
			if(process==null)
				continue;
			float transportTime = 52.5f/120f;
			float pressTime = 3.75f/120f;
			float fProcess = (process.processTick+(te.shouldRenderAsActive()?partialTicks: 0))/(float)process.maxTicks;

			if(fProcess < transportTime)
				shift[i] = fProcess/transportTime*.5f;
			else if(fProcess < (1-transportTime))
				shift[i] = .5f;
			else
				shift[i] = .5f+(fProcess-(1-transportTime))/transportTime*.5f;
			if(!te.mold.isEmpty())
				if(fProcess >= transportTime&&fProcess < (1-transportTime))
				{
					if(fProcess < (transportTime+pressTime))
						piston = (fProcess-transportTime)/pressTime;
					else if(fProcess < (1-transportTime-pressTime))
						piston = 1;
					else
						piston = 1-(fProcess-(1-transportTime-pressTime))/pressTime;
				}
		}
		GlStateManager.translate(0, -piston*.6875f, 0);

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
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, blockPos, worldRenderer, true);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();
		RenderHelper.enableStandardItemLighting();

		GlStateManager.rotate(te.facing==EnumFacing.SOUTH?180: te.facing==EnumFacing.WEST?90: te.facing==EnumFacing.EAST?-90: 0, 0, 1, 0);
		if(!te.mold.isEmpty())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, .34, 0);
			GlStateManager.rotate(-90, 1, 0, 0);
			float scale = .75f;
			GlStateManager.scale(scale, scale, 1);
			ClientUtils.mc().getRenderItem().renderItem(te.mold, ItemCameraTransforms.TransformType.FIXED);
			GlStateManager.scale(1/scale, 1/scale, 1);
			GlStateManager.popMatrix();
		}
		GlStateManager.translate(0, piston*.6875f, 0);
		GlStateManager.translate(0, -.35, 1.25);
		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess process = te.processQueue.get(i);
			if(process==null||!(process instanceof MultiblockProcessInWorld))
				continue;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0, 0, -2.5*shift[i]);
			if(piston > .92)
				GlStateManager.translate(0, .92-piston, 0);

			List<ItemStack> displays = ((MultiblockProcessInWorld)process).getDisplayItem();
			if(!displays.isEmpty())
			{
				GlStateManager.rotate(-90, 1, 0, 0);
				float scale = .625f;
				GlStateManager.scale(scale, scale, 1);
				ClientUtils.mc().getRenderItem().renderItem(displays.get(0), ItemCameraTransforms.TransformType.FIXED);
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
	}
}