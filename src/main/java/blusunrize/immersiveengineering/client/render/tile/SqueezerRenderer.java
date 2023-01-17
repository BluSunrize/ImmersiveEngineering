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
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.SqueezerLogic.State;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraftforge.client.model.data.ModelData;

public class SqueezerRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	public static final String NAME = "squeezer_piston";
	public static DynamicModel PISTON;

	@Override
	public void render(MultiblockBlockEntityMaster<State> te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BakedModel model = PISTON.get();
		final IMultiblockBEHelperMaster<State> helper = te.getHelper();
		final MultiblockOrientation orientation = helper.getContext().getLevel().getOrientation();

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		bufferIn = BERenderUtils.mirror(orientation, matrixStack, bufferIn);
		VertexConsumer buffer = bufferIn.getBuffer(RenderType.solid());

		float piston = helper.getState().animation_piston;
		//Smoothstep! TODO partial ticks?
		piston = piston*piston*(3.0f-2.0f*piston);

		matrixStack.translate(0, piston, 0);

		matrixStack.translate(-.5, -.5, -.5);
		rotateForFacing(matrixStack, orientation.front());
		blockRenderer.getModelRenderer().renderModel(
				matrixStack.last(), buffer, null, model,
				1, 1, 1,
				combinedLightIn, combinedOverlayIn, ModelData.EMPTY, RenderType.solid()
		);

		matrixStack.popPose();
	}
}