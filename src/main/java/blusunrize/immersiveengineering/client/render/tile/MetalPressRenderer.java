/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.blocks.metal.MetalPressTileEntity;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class MetalPressRenderer extends TileEntityRenderer<MetalPressTileEntity>
{
	public static DynamicModel<Void> PISTON;

	@Override
	public void render(MetalPressTileEntity te, double x, double y, double z, float partialTicks, int destroyStage)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.metalPress)
			return;
		IBakedModel model = PISTON.get(null);

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder worldRenderer = tessellator.getBuffer();

		ClientUtils.bindAtlas();
		GlStateManager.pushMatrix();
		GlStateManager.translated(x+.5, y+.5, z+.5);
		float piston = 0;
		float[] shift = new float[te.processQueue.size()];
		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess<MetalPressRecipe> process = te.processQueue.get(i);
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
		GlStateManager.translated(0, -piston*.6875f, 0);

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

		GlStateManager.rotatef(te.getFacing()==Direction.SOUTH?180: te.getFacing()==Direction.WEST?90: te.getFacing()==Direction.EAST?-90: 0, 0, 1, 0);
		if(!te.mold.isEmpty())
		{
			GlStateManager.pushMatrix();
			GlStateManager.translated(0, .34, 0);
			GlStateManager.rotatef(-90, 1, 0, 0);
			float scale = .75f;
			GlStateManager.scalef(scale, scale, 1);
			ClientUtils.mc().getItemRenderer().renderItem(te.mold, TransformType.FIXED);
			GlStateManager.scalef(1/scale, 1/scale, 1);
			GlStateManager.popMatrix();
		}
		GlStateManager.translated(0, piston*.6875f, 0);
		GlStateManager.translated(0, -.35, 1.25);
		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess process = te.processQueue.get(i);
			if(!(process instanceof PoweredMultiblockTileEntity.MultiblockProcessInWorld))
				continue;
			GlStateManager.pushMatrix();
			GlStateManager.translated(0, 0, -2.5*shift[i]);
			if(piston > .92)
				GlStateManager.translated(0, .92-piston, 0);

			List<ItemStack> displays = ((MultiblockProcessInWorld<?>)process).getDisplayItem();
			if(!displays.isEmpty())
			{
				GlStateManager.rotatef(-90, 1, 0, 0);
				float scale = .625f;
				GlStateManager.scalef(scale, scale, 1);
				ClientUtils.mc().getItemRenderer().renderItem(displays.get(0), TransformType.FIXED);
				GlStateManager.popMatrix();
			}
		}
		GlStateManager.popMatrix();
	}
}