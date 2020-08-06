/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SampleDrillRenderer extends TileEntityRenderer<SampleDrillTileEntity>
{
	public static DynamicModel<Void> DRILL;

	public SampleDrillRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}


	@Override
	public void render(SampleDrillTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = tile.getWorldNonnull().getBlockState(tile.getPos());
		IBakedModel model = DRILL.get(null);
		if(state.getBlock()!=MetalDevices.sampleDrill)
			return;

		matrixStack.push();
		matrixStack.translate(.5, .5, .5);

		int max = IEConfig.MACHINES.coredrill_time.get();
		if(tile.process > 0&&tile.process < max)
		{
			matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), ((tile.process+partialTicks)*22.5f)%360f, true));
			float push = tile.process/(float)max;
			if(tile.process > max/2)
				push = 1-push;
			matrixStack.translate(0, -2.8f*push, 0);
		}

		matrixStack.translate(-0.5, -0.5, -0.5);
		blockRenderer.getBlockModelRenderer().renderModel(tile.getWorldNonnull(), model, state, tile.getPos(), matrixStack,
				bufferIn.getBuffer(RenderType.getSolid()), true, tile.getWorld().rand, 0, combinedOverlayIn, EmptyModelData.INSTANCE);
		matrixStack.pop();
		RenderHelper.enableStandardItemLighting();
	}
}