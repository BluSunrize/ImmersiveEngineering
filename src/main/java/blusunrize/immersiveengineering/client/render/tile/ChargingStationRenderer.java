/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ChargingStationBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

public class ChargingStationRenderer extends IEBlockEntityRenderer<ChargingStationBlockEntity>
{
	@Override
	public void render(ChargingStationBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
		{
			matrixStack.pushPose();
			matrixStack.translate(.5, .3125, .5);
			matrixStack.scale(.75f, .75f, .75f);
			switch(te.getFacing())
			{
				case NORTH:
					matrixStack.mulPose(new Quaternionf().rotateY(Mth.PI));
					break;
				case SOUTH:
					break;
				case WEST:
					matrixStack.mulPose(new Quaternionf().rotateY(-Mth.HALF_PI));
					break;
				case EAST:
					matrixStack.mulPose(new Quaternionf().rotateY(Mth.HALF_PI));
					break;
			}
			if(!te.inventory.get(0).isEmpty())
			{
				matrixStack.pushPose();
				float scale = .625f;
				matrixStack.scale(scale, scale, 1);
				ClientUtils.mc().getItemRenderer().renderStatic(
						te.inventory.get(0), ItemDisplayContext.FIXED,
						combinedLightIn, combinedOverlayIn, matrixStack, bufferIn,
						te.getLevel(), 0
				);
				matrixStack.popPose();
			}
			matrixStack.popPose();
		}
	}
}