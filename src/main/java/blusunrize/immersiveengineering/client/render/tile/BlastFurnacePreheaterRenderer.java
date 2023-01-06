/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.metal.BlastFurnacePreheaterBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

public class BlastFurnacePreheaterRenderer extends IEBlockEntityRenderer<BlastFurnacePreheaterBlockEntity>
{
	public static final String NAME = "blastfurnace_preheater_fan";
	public static DynamicModel MODEL;

	@Override
	public void render(
			@Nonnull BlastFurnacePreheaterBlockEntity bEntity,
			float partial, @Nonnull PoseStack transform, @Nonnull MultiBufferSource buffers, int light, int overlay
	)
	{
		transform.pushPose();
		transform.translate(0.5, 0.5, 0.5);
		rotateForFacingNoCentering(transform, bEntity.getFacing());
		final float angle = bEntity.angle+BlastFurnacePreheaterBlockEntity.ANGLE_PER_TICK*(bEntity.active?partial: 0);
		Vector3f axis = new Vector3f(0, 0, 1);
		transform.mulPose(new Quaternionf().rotateAxis(angle, axis));
		transform.translate(-0.5, -0.5, -0.5);
		RenderUtils.renderModelTESRFast(
				MODEL.getNullQuads(), buffers.getBuffer(RenderType.solid()), transform, light, overlay
		);
		transform.popPose();
	}
}
