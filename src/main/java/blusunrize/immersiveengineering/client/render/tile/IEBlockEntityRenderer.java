/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.config.IEClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Quaternionf;

public abstract class IEBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T>
{
	@Override
	public int getViewDistance()
	{
		double increase = IEClientConfig.increasedTileRenderdistance.get();
		return (int)(BlockEntityRenderer.super.getViewDistance()*increase);
	}

	protected static void rotateForFacingNoCentering(PoseStack stack, Direction facing)
	{
		stack.mulPose(new Quaternionf().rotateXYZ(0, (float) Math.toRadians(180-facing.toYRot()), 0));
	}

	protected static void rotateForFacing(PoseStack stack, Direction facing)
	{
		stack.translate(0.5, 0.5, 0.5);
		rotateForFacingNoCentering(stack, facing);
		stack.translate(-0.5, -0.5, -0.5);
	}
}
