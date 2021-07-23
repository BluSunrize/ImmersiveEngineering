/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ChargingStationTileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;

public class ChargingStationRenderer extends BlockEntityRenderer<ChargingStationTileEntity>
{
	public ChargingStationRenderer(BlockEntityRenderDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(ChargingStationTileEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(te.getWorldNonnull().hasChunkAt(te.getBlockPos()))
		{
			te.particles.get().render(matrixStack, te.getBlockPos(), bufferIn, partialTicks);
			matrixStack.pushPose();
			matrixStack.translate(.5, .3125, .5);
			matrixStack.scale(.75f, .75f, .75f);
			switch(te.getFacing())
			{
				case NORTH:
					matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), 180, true));
					break;
				case SOUTH:
					break;
				case WEST:
					matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), -90, true));
					break;
				case EAST:
					matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), 90, true));
					break;
			}
			if(!te.inventory.get(0).isEmpty())
			{
				matrixStack.pushPose();
				float scale = .625f;
				matrixStack.scale(scale, scale, 1);
				ClientUtils.mc().getItemRenderer().renderStatic(te.inventory.get(0), TransformType.FIXED, combinedLightIn,
						combinedOverlayIn, matrixStack, bufferIn);
				matrixStack.popPose();
			}
			matrixStack.popPose();
		}
	}
}