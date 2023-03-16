/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SiloLogic.State;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font.DisplayMode;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import org.joml.Quaternionf;

public class SiloRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	@Override
	public void render(MultiblockBlockEntityMaster<State> tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final State state = tile.getHelper().getState();
		if(state.identStack.isEmpty())
			return;
		matrixStack.pushPose();

		matrixStack.translate(.5, 5, .5);
		float baseScale = .0625f;
		float itemScale = .5f;
		float flatScale = .001f;
		float textScale = .375f*0.75f;
		matrixStack.scale(baseScale, baseScale, baseScale);
		String label = ""+state.storageAmount;
		float w = ClientUtils.mc().font.width(label);

		float zz = 1.501f;
		zz /= baseScale;
		w *= textScale;
		for(int i = 0; i < 4; i++)
		{
			matrixStack.pushPose();
			matrixStack.translate(0, 0, zz);

			matrixStack.pushPose();
			// Do not multiply the normal matrix here, it messes up lighting for some reason
			matrixStack.last().pose().scale(itemScale/baseScale, itemScale/baseScale, flatScale)
			;
			matrixStack.translate(0, -0.75, 0);
			ClientUtils.mc().getItemRenderer().renderStatic(
					state.identStack, ItemDisplayContext.GUI,
					combinedLightIn, combinedOverlayIn, matrixStack, bufferIn,
					tile.getLevel(), 0
			);
			matrixStack.popPose();

			matrixStack.pushPose();
			matrixStack.translate(-w/2, -11, .001f);
			matrixStack.scale(textScale, -textScale, 1);
			ClientUtils.font().drawInBatch(
					label, 0, 0, 0x888888, true, matrixStack.last().pose(), bufferIn, DisplayMode.NORMAL, 0, combinedLightIn
			);
			matrixStack.popPose();

			matrixStack.popPose();
			matrixStack.mulPose(new Quaternionf().rotateY(Mth.HALF_PI));
		}
		matrixStack.popPose();
	}

}