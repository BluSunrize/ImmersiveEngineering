/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.multiblocks.blocks.util;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockContext;
import blusunrize.immersiveengineering.api.multiblocks.blocks.logic.IMultiblockState;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public interface MultiblockRenderer<State extends IMultiblockState>
		extends BlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	@Override
	default void render(
			@NotNull MultiblockBlockEntityMaster<State> te,
			float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn
	)
	{
		render(te.getHelper().getContext(), partialTicks, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
	}

	@Override
	@NotNull
	default AABB getRenderBoundingBox(@NotNull MultiblockBlockEntityMaster<State> blockEntity)
	{
		return blockEntity.getHelper().getRenderBoundingBox();
	}

	void render(
			@NotNull IMultiblockContext<State> ctx,
			float partialTicks, @NotNull PoseStack matrixStack, @NotNull MultiBufferSource bufferIn,
			int combinedLightIn, int combinedOverlayIn
	);
}
