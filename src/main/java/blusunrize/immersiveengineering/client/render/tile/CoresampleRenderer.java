/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;

public class CoresampleRenderer extends BlockEntityRenderer<CoresampleTileEntity>
{
	public CoresampleRenderer(BlockEntityRenderDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(CoresampleTileEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.getWorldNonnull().hasChunkAt(tile.getBlockPos())||tile.coresample==null)
			return;

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), tile.getFacing()==Direction.NORTH?180: tile.getFacing()==Direction.WEST?-90: tile.getFacing()==Direction.EAST?90: 0, true));
		matrixStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), -45, true));
		matrixStack.translate(0, .04864, .02903);
		ClientUtils.mc().getItemRenderer().renderStatic(tile.coresample, TransformType.FIXED, combinedLightIn,
				combinedOverlayIn, matrixStack, bufferIn);
		matrixStack.popPose();
	}
}