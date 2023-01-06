/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.MetalPressLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;

import java.util.List;

import static blusunrize.immersiveengineering.common.blocks.metal.MetalPressBlockEntity.*;

public class MetalPressRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	public static final String NAME = "metal_press_piston";
	public static DynamicModel PISTON;

	@Override
	public void render(MultiblockBlockEntityMaster<State> te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final var helper = te.getHelper();
		final var state = helper.getState();
		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		float piston = 0;
		final var queue = state.processor.getQueue();
		float[] shift = new float[queue.size()];

		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess<MetalPressRecipe, ?> process = queue.get(i);
			float processMaxTicks = process.getMaxTicks(te.getLevel());
			float transportTime = getTransportTime(processMaxTicks);
			float pressTime = getPressTime(processMaxTicks);
			//+partialTicks
			float fProcess = process.processTick;

			if(fProcess < transportTime)
				shift[i] = .5f*fProcess/transportTime;
			else if(fProcess < (processMaxTicks-transportTime))
				shift[i] = .5f;
			else
				shift[i] = .5f+.5f*(fProcess-(processMaxTicks-transportTime))/transportTime;
			if(!state.mold.isEmpty())
				if(fProcess >= transportTime&&fProcess < (processMaxTicks-transportTime))
				{
					if(fProcess < (transportTime+pressTime))
						piston = (fProcess-transportTime)/pressTime;
					else if(fProcess < (processMaxTicks-transportTime-pressTime))
						piston = 1;
					else
						piston = 1-(fProcess-(processMaxTicks-transportTime-pressTime))/pressTime;
				}
		}

		rotateForFacing(matrixStack, helper.getContext().getLevel().getOrientation().front());
		matrixStack.pushPose();
		matrixStack.translate(0, -piston*.6875f, 0);
		matrixStack.pushPose();
		matrixStack.translate(-0.5, -0.5, -0.5);
		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BakedModel pistonModel = PISTON.get();
		blockRenderer.getModelRenderer().renderModel(
				matrixStack.last(), bufferIn.getBuffer(RenderType.solid()), null, pistonModel,
				1, 1, 1,
				combinedLightIn, combinedOverlayIn, ModelData.EMPTY, RenderType.solid()
		);
		matrixStack.popPose();

		if(!state.mold.isEmpty())
		{
			matrixStack.translate(0, .34, 0);
			matrixStack.mulPose(new Quaternionf().rotateX(-Mth.HALF_PI));
			float scale = .75f;
			matrixStack.scale(scale, scale, 1);
			ClientUtils.mc().getItemRenderer().renderStatic(state.mold, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStack, bufferIn, 0);
		}
		matrixStack.popPose();
		matrixStack.translate(-1.25, -.35, 0);
		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess<?, ?> process = queue.get(i);
			if(!(process instanceof MultiblockProcessInWorld<?> inWorld))
				continue;
			List<ItemStack> displays = inWorld.getDisplayItem(te.getLevel());
			if(displays.isEmpty())
				continue;
			matrixStack.pushPose();
			matrixStack.translate(TRANSLATION_DISTANCE*shift[i], 0, 0);
			if(piston > .92)
				matrixStack.translate(0, .92-piston, 0);

			matrixStack.mulPose(new Quaternionf().rotateX(-Mth.HALF_PI));
			float scale = .625f;
			matrixStack.scale(scale, scale, 1);
			ClientUtils.mc().getItemRenderer().renderStatic(displays.get(0), TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStack, bufferIn, 0);
			matrixStack.popPose();
		}
		matrixStack.popPose();
	}
}