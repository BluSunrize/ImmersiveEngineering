/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.sawmill.SawmillLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.sawmill.SawmillProcess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public class SawmillRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	public static final String NAME = "sawmill_blade";
	public static DynamicModel BLADE;

	@Override
	public void render(MultiblockBlockEntityMaster<State> te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final var helper = te.getHelper();
		final var level = helper.getContext().getLevel();
		final var state = helper.getState();

		//Outer GL Wrapping, initial translation
		matrixStack.pushPose();
		matrixStack.translate(.5, 0, .5);
		bufferIn = BERenderUtils.mirror(level.getOrientation(), matrixStack, bufferIn);


		VertexConsumer solidBuilder = bufferIn.getBuffer(RenderType.solid());

		Direction facing = level.getOrientation().front();
		float dir = facing==Direction.SOUTH?Mth.PI: facing==Direction.NORTH?0: facing==Direction.EAST?-Mth.HALF_PI: Mth.HALF_PI;
		matrixStack.mulPose(new Quaternionf().rotateY(dir));

		// Sawblade
		boolean sawblade = !state.sawblade.isEmpty();
		if(sawblade)
		{
			matrixStack.pushPose();
			matrixStack.translate(1, .125, -.5);
			float spin = state.animation_bladeRotation;
			if(state.active)
				spin += 36f*partialTicks;
			matrixStack.mulPose(new Quaternionf().rotateZ(spin*Mth.DEG_TO_RAD));
			RenderUtils.renderModelTESRFast(
					BLADE.getNullQuads(), solidBuilder, matrixStack, combinedLightIn, combinedOverlayIn
			);
			matrixStack.popPose();
		}

		// Items
		for(SawmillProcess process : state.sawmillProcessQueue)
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
		matrixStack.mulPose(new Quaternionf().rotateZ(Mth.HALF_PI));
		ClientUtils.mc().getItemRenderer().renderStatic(stack, TransformType.FIXED,
				combinedLightIn, combinedOverlayIn, matrixStack, bufferIn, 0);
		matrixStack.popPose();
	}
}
