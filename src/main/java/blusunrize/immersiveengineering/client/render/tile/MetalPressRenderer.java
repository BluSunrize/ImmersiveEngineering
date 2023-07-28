/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import java.util.List;

import static blusunrize.immersiveengineering.common.blocks.multiblocks.logic.MetalPressLogic.*;

public class MetalPressRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	public static final String NAME = "metal_press_piston";
	public static DynamicModel PISTON;

	@Override
	public void render(MultiblockBlockEntityMaster<State> te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final IMultiblockBEHelperMaster<State> helper = te.getHelper();
		final State state = helper.getState();
		float piston = 0;
		float[] shift = new float[state.processor.getQueueSize()];

		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess<MetalPressRecipe, ?> process = state.processor.getQueue().get(i);
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

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		MultiblockOrientation orientation = helper.getContext().getLevel().getOrientation();
		Direction effectiveFacing = orientation.mirrored()?orientation.front().getOpposite(): orientation.front();
		rotateForFacingNoCentering(matrixStack, effectiveFacing);
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
			ClientUtils.mc().getItemRenderer().renderStatic(
					state.mold, ItemDisplayContext.FIXED,
					combinedLightIn, combinedOverlayIn, matrixStack, bufferIn,
					te.getLevel(), 0
			);
		}
		matrixStack.popPose();
		matrixStack.translate(-1.25, -.35, 0);
		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess<?, ?> process = state.processor.getQueue().get(i);
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
			ClientUtils.mc().getItemRenderer().renderStatic(
					displays.get(0), ItemDisplayContext.FIXED,
					combinedLightIn, combinedOverlayIn, matrixStack, bufferIn,
					te.getLevel(), 0);
			matrixStack.popPose();
		}
		matrixStack.popPose();
	}
}