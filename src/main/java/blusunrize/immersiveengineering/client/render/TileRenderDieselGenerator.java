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
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalMultiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityDieselGenerator;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

public class TileRenderDieselGenerator extends TileEntityRenderer<TileEntityDieselGenerator>
{
	@Override
	public void render(TileEntityDieselGenerator te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorld().isBlockLoaded(te.getPos(), false))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		IBlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=MetalMultiblocks.dieselGenerator)
			return;
		state = state.with(IEProperties.DYNAMICRENDER, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);

//		boolean b = te.getEnergyStored(null)>0 && !te.isRSDisabled() && !te.processQueue.isEmpty();
//		float angle = te.animation_barrelRotation+(b?18*partialTicks:0);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x, y, z);
		GlStateManager.translated(.5, .6875, .5);


		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
//		GlStateManager.translated(te.facing.getXOffset()*.25, 0, te.facing.getZOffset()*.25);
//		float angle = ClientUtils.mc().player.ticksExisted%100/100f;
//		GlStateManager.rotatef(angle*360, te.facing.getXOffset(), 0, te.facing.getZOffset());
		GlStateManager.rotatef(te.animation_fanRotation+(te.animation_fanRotationStep*partialTicks), te.facing.getXOffset(), 0, te.facing.getZOffset());

		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		worldRenderer.setTranslation(-.5-blockPos.getX(), -blockPos.getY(), -.5-blockPos.getZ());
		worldRenderer.color(255, 255, 255, 255);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, blockPos, worldRenderer, true,
				Utils.RAND, 0, EmptyModelData.INSTANCE);
		worldRenderer.setTranslation(0.0D, 0.0D, 0.0D);
		tessellator.draw();

		RenderHelper.enableStandardItemLighting();

		GlStateManager.popMatrix();
	}
}