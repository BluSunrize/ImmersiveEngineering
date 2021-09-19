/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.DieselGeneratorTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DieselGeneratorRenderer extends BlockEntityRenderer<DieselGeneratorTileEntity>
{
	public static DynamicModel<Direction> FAN;

	public DieselGeneratorRenderer(BlockEntityRenderDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(DieselGeneratorTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().hasChunkAt(te.getBlockPos()))
			return;

		BlockPos blockPos = te.getBlockPos();
		BlockState state = te.getLevel().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.dieselGenerator)
			return;

		matrixStack.pushPose();
		matrixStack.translate(0, .6875, 0);
		matrixStack.translate(0.5, 0, 0.5);

		matrixStack.mulPose(new Quaternion(new Vector3f(Vec3.atLowerCornerOf(te.getFacing().getNormal())),
				te.animation_fanRotation+(te.animation_fanRotationStep*partialTicks), true));
		matrixStack.translate(-0.5, 0, -0.5);

		List<BakedQuad> quads = FAN.getNullQuads(te.getFacing(), state);
		RenderUtils.renderModelTESRFast(quads, bufferIn.getBuffer(RenderType.solid()), matrixStack, combinedLightIn,
				combinedOverlayIn);

		matrixStack.popPose();
	}
}