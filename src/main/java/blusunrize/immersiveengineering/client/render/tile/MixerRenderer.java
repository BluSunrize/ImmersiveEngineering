/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.mixer.MixerLogic.State;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Quaternionf;

public class MixerRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	public static final String NAME = "mixer_agitator";
	public static DynamicModel AGITATOR;

	@Override
	public void render(MultiblockBlockEntityMaster<State> te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final IMultiblockBEHelperMaster<State> helper = te.getHelper();
		final State state = helper.getState();
		final MultiblockOrientation orientation = helper.getContext().getLevel().getOrientation();
		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);

		bufferIn = BERenderUtils.mirror(orientation, matrixStack, bufferIn);
		matrixStack.pushPose();
		final Direction front = orientation.front();
		matrixStack.translate(front==Direction.SOUTH||front==Direction.WEST?-.5: .5, 0, front==Direction.SOUTH||front==Direction.EAST?.5: -.5);
		float agitator = state.animation_agitator-(!state.isActive?0: (1-partialTicks)*9f);
		matrixStack.mulPose(new Quaternionf().rotateY(agitator *Mth.DEG_TO_RAD));

		matrixStack.translate(-0.5, -0.5, -0.5);
		blockRenderer.getModelRenderer().renderModel(
				matrixStack.last(), bufferIn.getBuffer(RenderType.solid()), null, AGITATOR.get(),
				1, 1, 1,
				combinedLightIn, combinedOverlayIn, ModelData.EMPTY, RenderType.solid()
		);

		matrixStack.popPose();

		matrixStack.translate(front==Direction.SOUTH||front==Direction.WEST?-.5: .5, -.625f, front==Direction.SOUTH||front==Direction.EAST?.5: -.5);
		matrixStack.scale(.0625f, 1, .0625f);
		matrixStack.mulPose(new Quaternionf().rotateX(Mth.HALF_PI));

		for(int i = state.tank.getFluidTypes()-1; i >= 0; i--)
		{
			FluidStack fs = state.tank.fluids.get(i);
			if(fs!=null&&fs.getFluid()!=null)
			{
				float yy = fs.getAmount()/(float)state.tank.getCapacity()*1.0625f;
				matrixStack.translate(0, 0, -yy);
				float w = (i < state.tank.getFluidTypes()-1||yy >= .125)?26: 16+yy/.0125f;
				GuiHelper.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.translucent()), matrixStack, fs,
						-w/2, -w/2, w, w);
			}
		}

		matrixStack.popPose();
	}
}