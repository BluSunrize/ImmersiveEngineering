/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.SawmillBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.SawmillBlockEntity.SawmillProcess;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Quaternion;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class SawmillRenderer extends IEBlockEntityRenderer<SawmillBlockEntity>
{
	public static final String NAME = "sawmill_blade";
	public static DynamicModel BLADE;

	@Override
	public void render(SawmillBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		//Grab model
		BlockPos blockPos = te.getBlockPos();
		BlockState state = te.getLevel().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.SAWMILL.get())
			return;

		//Outer GL Wrapping, initial translation
		matrixStack.pushPose();
		matrixStack.translate(.5, 0, .5);
		bufferIn = BERenderUtils.mirror(te, matrixStack, bufferIn);


		VertexConsumer solidBuilder = bufferIn.getBuffer(RenderType.solid());

		Direction facing = te.getFacing();
		float dir = facing==Direction.SOUTH?180: facing==Direction.NORTH?0: facing==Direction.EAST?-90: 90;
		matrixStack.mulPose(new Quaternion(0, dir, 0, true));

		// Sawblade
		boolean sawblade = !te.sawblade.isEmpty();
		if(sawblade)
		{
			matrixStack.pushPose();
			matrixStack.translate(1, .125, -.5);
			float spin = te.animation_bladeRotation;
			if(te.shouldRenderAsActive() && !te.isRSDisabled())
				spin += 36f*partialTicks;
			matrixStack.mulPose(new Quaternion(0, 0, spin, true));
			RenderUtils.renderModelTESRFast(
					BLADE.getNullQuads(), solidBuilder, matrixStack, combinedLightIn, combinedOverlayIn
			);
			matrixStack.popPose();
		}

		// Items
		for(SawmillProcess process : te.sawmillProcessQueue)
		{
			float relative = process.getRelativeProcessStep(te.getLevel());
			ItemStack rendered = process.getCurrentStack(te.getLevel(), sawblade);
			renderItem(rendered, relative, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
		}
		matrixStack.popPose();
	}

	private void renderItem(ItemStack stack, float progress, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		float xOffset = -2.5f+progress*5;
		matrixStack.pushPose();
		matrixStack.translate(xOffset, .375, 0);
		matrixStack.mulPose(new Quaternion(0, 0, 90, true));
		ClientUtils.mc().getItemRenderer().renderStatic(stack, TransformType.FIXED,
				combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
		matrixStack.popPose();
	}
}
