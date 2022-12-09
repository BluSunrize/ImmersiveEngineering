/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.SampleDrillBlockEntity;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class SampleDrillRenderer extends IEBlockEntityRenderer<SampleDrillBlockEntity>
{
	public static final String NAME = "sample_drill";
	public static DynamicModel DRILL;

	@Override
	public void render(SampleDrillBlockEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.getLevelNonnull().hasChunkAt(tile.getBlockPos()))
			return;

		BlockState state = tile.getLevelNonnull().getBlockState(tile.getBlockPos());
		if(state.getBlock()!=MetalDevices.SAMPLE_DRILL.get())
			return;

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);

		int max = IEServerConfig.MACHINES.coredrill_time.get();
		if(tile.process > 0&&tile.process < max)
		{
			float currentProcess = tile.process;
			if (tile.isRunning)
				currentProcess += partialTicks;
			matrixStack.mulPose(new Quaternionf().rotateY( (currentProcess*22.5f)%360f *Mth.DEG_TO_RAD));
			float push = tile.process/(float)max;
			if(tile.process > max/2)
				push = 1-push;
			matrixStack.translate(0, -2.8f*push, 0);
		}

		matrixStack.translate(-0.5, -0.5, -0.5);
		List<BakedQuad> quads = DRILL.getNullQuads();
		RenderUtils.renderModelTESRFast(
				quads, bufferIn.getBuffer(RenderType.solid()), matrixStack, combinedLightIn, combinedOverlayIn
		);
		matrixStack.popPose();
	}
}