/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.metal.SiloTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemHandlerHelper;

public class SiloRenderer extends IEBlockEntityRenderer<SiloTileEntity>
{
	@Override
	public void render(SiloTileEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||tile.isDummy()||!tile.getLevelNonnull().hasChunkAt(tile.getBlockPos()))
			return;
		matrixStack.pushPose();

		matrixStack.translate(.5, 0, .5);

		if(!tile.identStack.isEmpty())
		{
			matrixStack.translate(0, 5, 0);
			float baseScale = .0625f;
			float itemScale = .5f;
			float flatScale = .001f;
			float textScale = .375f*0.75f;
			matrixStack.scale(baseScale, baseScale, baseScale);
			ItemStack stack = ItemHandlerHelper.copyStackWithSize(tile.identStack, tile.storageAmount);
			String s = ""+stack.getCount();
			float w = ClientUtils.mc().font.width(s);

			float zz = 1.501f;
			zz /= baseScale;
			w *= textScale;
			for(int i = 0; i < 4; i++)
			{
				matrixStack.pushPose();
				matrixStack.translate(0, 0, zz);

				matrixStack.pushPose();
				matrixStack.scale(itemScale/baseScale, itemScale/baseScale, flatScale);
				matrixStack.translate(0, -0.75, 0);
				ClientUtils.mc().getItemRenderer().renderStatic(
						stack, TransformType.GUI, combinedLightIn, combinedOverlayIn, matrixStack, IERenderTypes.disableLighting(bufferIn), 0
				);
				matrixStack.popPose();

				matrixStack.pushPose();
				matrixStack.translate(-w/2, -11, .001f);
				matrixStack.scale(textScale, -textScale, 1);
				ClientUtils.font().drawInBatch(
						""+stack.getCount(),
						0, 0,
						0x888888,
						true,
						matrixStack.last().pose(),
						bufferIn,
						false,
						0,
						combinedLightIn
				);
				matrixStack.popPose();

				matrixStack.popPose();
				matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), 90, true));
			}
		}
		matrixStack.popPose();
	}

}