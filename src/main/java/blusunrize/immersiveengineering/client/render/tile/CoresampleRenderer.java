/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.core.Direction;

public class CoresampleRenderer extends IEBlockEntityRenderer<CoresampleBlockEntity>
{
	@Override
	public void render(CoresampleBlockEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.getLevelNonnull().hasChunkAt(tile.getBlockPos())||tile.coresample==null)
			return;

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		matrixStack.mulPose(new Quaternionf().rotateY(
				tile.getFacing()==Direction.NORTH?Mth.PI: tile.getFacing()==Direction.WEST?-Mth.HALF_PI: tile.getFacing()==Direction.EAST?Mth.HALF_PI: 0
		));
		matrixStack.mulPose(new Quaternionf().rotateX(-Mth.HALF_PI / 2));
		matrixStack.translate(0, .04864, .02903);
		ClientUtils.mc().getItemRenderer().renderStatic(tile.coresample, TransformType.FIXED, combinedLightIn,
				combinedOverlayIn, matrixStack, bufferIn, 0);
		matrixStack.popPose();
	}
}