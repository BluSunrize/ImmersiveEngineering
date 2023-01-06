/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.DieselGeneratorLogic.State;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

public class DieselGeneratorRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<DieselGeneratorLogic.State>>
{
	public static final String NAME = "diesel_gen_fan";
	public static DynamicModel FAN;

	@Override
	public void render(MultiblockBlockEntityMaster<DieselGeneratorLogic.State> te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		matrixStack.pushPose();
		matrixStack.translate(0, .6875, 0);
		matrixStack.translate(0.5, 0, 0.5);
		final IMultiblockBEHelperMaster<State> helper = te.getHelper();
		final Direction facing = helper.getContext().getLevel().getOrientation().front();
		final State state = helper.getState();

		matrixStack.mulPose(new Quaternionf().rotateAxis(
				(state.animation_fanRotation+(state.animation_fanRotationStep*partialTicks))*Mth.DEG_TO_RAD,
				Vec3.atLowerCornerOf(facing.getNormal()).toVector3f()
		));
		matrixStack.translate(-0.5, 0, -0.5);

		List<BakedQuad> quads = FAN.getNullQuads();
		rotateForFacing(matrixStack, facing);
		RenderUtils.renderModelTESRFast(
				quads, bufferIn.getBuffer(RenderType.solid()), matrixStack, combinedLightIn, combinedOverlayIn
		);

		matrixStack.popPose();
	}
}