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
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SampleDrillRenderer extends BlockEntityRenderer<SampleDrillTileEntity>
{
	public static DynamicModel<Void> DRILL;

	public SampleDrillRenderer(BlockEntityRenderDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}


	@Override
	public void render(SampleDrillTileEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().hasChunkAt(tile.getBlockPos()))
			return;

		BlockState state = tile.getWorldNonnull().getBlockState(tile.getBlockPos());
		if(state.getBlock()!=MetalDevices.sampleDrill.get())
			return;

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);

		int max = IEServerConfig.MACHINES.coredrill_time.get();
		if(tile.process > 0&&tile.process < max)
		{
			float currentProcess = tile.process;
			if (tile.isRunning)
				currentProcess += partialTicks;
			matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), (currentProcess*22.5f)%360f, true));
			float push = tile.process/(float)max;
			if(tile.process > max/2)
				push = 1-push;
			matrixStack.translate(0, -2.8f*push, 0);
		}

		matrixStack.translate(-0.5, -0.5, -0.5);
		List<BakedQuad> quads = DRILL.getNullQuads(null, state);
		RenderUtils.renderModelTESRFast(
				quads, bufferIn.getBuffer(RenderType.solid()), matrixStack, combinedLightIn, combinedOverlayIn
		);
		matrixStack.popPose();
	}
}