package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.config.IEClientConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Quaternionf;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

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
		stack.mulPose(new Quaternionf(0, 180-facing.toYRot(), 0, true));
	}

	protected static void rotateForFacing(PoseStack stack, Direction facing)
	{
		stack.translate(0.5, 0.5, 0.5);
		rotateForFacingNoCentering(stack, facing);
		stack.translate(-0.5, -0.5, -0.5);
	}
}
