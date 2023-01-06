/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SheetmetalTankLogic.State;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class SheetmetalTankRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	@Override
	public void render(MultiblockBlockEntityMaster<State> tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final State state = tile.getHelper().getState();
		matrixStack.pushPose();

		matrixStack.translate(.5, 0, .5);

		FluidStack fs = state.tank.getFluid();
		matrixStack.translate(0, 3.5f, 0);
		float baseScale = .0625f;
		matrixStack.scale(baseScale, -baseScale, baseScale);

		float xx = -.5f;
		float zz = 1.5f-.004f;
		xx /= baseScale;
		zz /= baseScale;
		for(int i = 0; i < 4; i++)
		{
			matrixStack.pushPose();
			matrixStack.translate(xx, 0, zz);

			Matrix4f mat = matrixStack.last().pose();
			final VertexConsumer builder = bufferIn.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
			builder.vertex(mat, -4, -4, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			builder.vertex(mat, -4, 20, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			builder.vertex(mat, 20, 20, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			builder.vertex(mat, 20, -4, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();

			if(!fs.isEmpty())
			{
				float h = fs.getAmount()/(float)state.tank.getCapacity();
				matrixStack.translate(0, 0, .004f);
				GuiHelper.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.solid()), matrixStack, fs,
						0, 0+(1-h)*16, 16, h*16);
			}
			matrixStack.popPose();
			matrixStack.mulPose(new Quaternionf().rotateY(Mth.HALF_PI));
		}
		matrixStack.popPose();
	}

}