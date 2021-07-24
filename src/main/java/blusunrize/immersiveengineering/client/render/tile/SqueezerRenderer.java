/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.SqueezerTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

public class SqueezerRenderer extends IEBlockEntityRenderer<SqueezerTileEntity>
{
	public static DynamicModel<Direction> PISTON;

	@Override
	public void render(SqueezerTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BlockPos blockPos = te.getBlockPos();
		BlockState state = te.getLevel().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.squeezer.get())
			return;
		BakedModel model = PISTON.get(te.getFacing());

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		bufferIn = TileRenderUtils.mirror(te, matrixStack, bufferIn);
		VertexConsumer buffer = bufferIn.getBuffer(RenderType.solid());

		float piston = te.animation_piston;
		//Smoothstep! TODO partial ticks?
		piston = piston*piston*(3.0f-2.0f*piston);

		matrixStack.translate(0, piston, 0);

		matrixStack.translate(-.5, -.5, -.5);
		blockRenderer.getModelRenderer().renderModel(
				matrixStack.last(), buffer, state, model,
				1, 1, 1,
				combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE
		);

		matrixStack.popPose();
	}
}