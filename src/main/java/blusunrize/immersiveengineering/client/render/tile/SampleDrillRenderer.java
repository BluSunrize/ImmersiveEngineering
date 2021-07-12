/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillTileEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;

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

		BlockState state = tile.getWorldNonnull().getBlockState(tile.getPos());
		if(state.getBlock()!=MetalDevices.sampleDrill)
			return;

		matrixStack.push();
		matrixStack.translate(.5, .5, .5);

		int max = IEServerConfig.MACHINES.coredrill_time.get();
		if(tile.process > 0&&tile.process < max)
		{
			float currentProcess = tile.process;
			if (tile.isRunning)
				currentProcess += partialTicks;
			matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), (currentProcess*22.5f)%360f, true));
			float push = tile.process/(float)max;
			if(tile.process > max/2)
				push = 1-push;
			matrixStack.translate(0, -2.8f*push, 0);
		}

		matrixStack.translate(-0.5, -0.5, -0.5);
		List<BakedQuad> quads = DRILL.getNullQuads(null, state);
		RenderUtils.renderModelTESRFast(
				quads, bufferIn.getBuffer(RenderType.getSolid()), matrixStack, combinedLightIn, combinedOverlayIn
		);
		matrixStack.pop();
	}
}