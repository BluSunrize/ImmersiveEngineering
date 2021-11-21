/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.MetalPressBlockEntity;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.multiblocks.process.MultiblockProcessInWorld;
import blusunrize.immersiveengineering.common.register.IEBlocks.Multiblocks;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;

import java.util.List;

import static blusunrize.immersiveengineering.common.blocks.metal.MetalPressBlockEntity.*;

public class MetalPressRenderer extends IEBlockEntityRenderer<MetalPressBlockEntity>
{
	public static final String NAME = "metal_press_piston";
	public static DynamicModel PISTON;

	@Override
	public void render(MetalPressBlockEntity te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getLevelNonnull().hasChunkAt(te.getBlockPos()))
			return;

		final BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
		BlockPos blockPos = te.getBlockPos();
		BlockState state = te.getLevel().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.METAL_PRESS.get())
			return;
		BakedModel model = PISTON.get();

		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		float piston = 0;
		float[] shift = new float[te.processQueue.size()];

		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess<MetalPressRecipe> process = te.processQueue.get(i);
			if(process==null)
				continue;
			float processMaxTicks = process.maxTicks;
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
			if(!te.mold.isEmpty())
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

		matrixStack.mulPose(new Quaternion(new Vector3f(0, 1, 0), te.getFacing()==Direction.SOUTH?180: te.getFacing()==Direction.WEST?90: te.getFacing()==Direction.EAST?-90: 0, true));
		matrixStack.pushPose();
		matrixStack.translate(0, -piston*.6875f, 0);
		matrixStack.pushPose();
		matrixStack.translate(-0.5, -0.5, -0.5);
		blockRenderer.getModelRenderer().renderModel(
				matrixStack.last(), bufferIn.getBuffer(RenderType.solid()), state, model,
				1, 1, 1,
				combinedLightIn, combinedOverlayIn, EmptyModelData.INSTANCE
		);
		matrixStack.popPose();

		if(!te.mold.isEmpty())
		{
			matrixStack.translate(0, .34, 0);
			matrixStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), -90, true));
			float scale = .75f;
			matrixStack.scale(scale, scale, 1);
			ClientUtils.mc().getItemRenderer().renderStatic(te.mold, TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStack, bufferIn, 0);
		}
		matrixStack.popPose();
		matrixStack.translate(0, -.35, 1.25);
		for(int i = 0; i < shift.length; i++)
		{
			MultiblockProcess<?> process = te.processQueue.get(i);
			if(!(process instanceof MultiblockProcessInWorld<?> inWorld))
				continue;
			List<ItemStack> displays = inWorld.getDisplayItem();
			if(displays.isEmpty())
				continue;
			matrixStack.pushPose();
			matrixStack.translate(0, 0, -TRANSLATION_DISTANCE*shift[i]);
			if(piston > .92)
				matrixStack.translate(0, .92-piston, 0);

			matrixStack.mulPose(new Quaternion(new Vector3f(1, 0, 0), -90, true));
			float scale = .625f;
			matrixStack.scale(scale, scale, 1);
			ClientUtils.mc().getItemRenderer().renderStatic(displays.get(0), TransformType.FIXED, combinedLightIn, combinedOverlayIn,
					matrixStack, bufferIn, 0);
			matrixStack.popPose();
		}
		matrixStack.popPose();
	}
}