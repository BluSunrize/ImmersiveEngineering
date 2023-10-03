/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.api.multiblocks.blocks.env.IMultiblockBEHelperMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.registry.MultiblockBlockEntityMaster;
import blusunrize.immersiveengineering.api.multiblocks.blocks.util.MultiblockOrientation;
import blusunrize.immersiveengineering.api.utils.client.ModelDataUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.obj.callback.DynamicSubmodelCallbacks;
import blusunrize.immersiveengineering.client.utils.BatchingRenderTypeBuffer;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingMachineLogic.State;
import blusunrize.immersiveengineering.common.blocks.multiblocks.logic.bottling_machine.BottlingProcess;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.fluids.FluidStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class BottlingMachineRenderer extends IEBlockEntityRenderer<MultiblockBlockEntityMaster<State>>
{
	public static final String NAME = "bottling_machine_dynamic";
	public static DynamicModel DYNAMIC;

	@Override
	public void render(MultiblockBlockEntityMaster<State> te, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		final IMultiblockBEHelperMaster<State> helper = te.getHelper();
		final MultiblockOrientation orientation = helper.getContext().getLevel().getOrientation();
		final State state = helper.getState();
		Direction facing = orientation.front();

		final float pixelHeight = 1f/16f;

		//Outer GL Wrapping, initial translation
		matrixStack.pushPose();
		matrixStack.translate(.5, .5, .5);
		final MultiBufferSource originalBuffer = bufferIn;
		bufferIn = BERenderUtils.mirror(orientation, matrixStack, bufferIn);

		//Item Displacement
		record ItemDisplay(float itemFill, Vector3f translation, BottlingProcess process)
		{
		}
		List<ItemDisplay> itemDisplays = new ArrayList<>();
		//Animations
		float lift = 0;

		VertexConsumer solidBuilder = bufferIn.getBuffer(RenderType.solid());
		for(int i = 0; i < state.processor.getQueueSize(); i++)
		{
			BottlingProcess process = (BottlingProcess)state.processor.getQueue().get(i);
			if(process==null)
				continue;
			float processMaxTicks = process.getMaxTicks(te.getLevel());
			float transportTime = BottlingMachineLogic.getTransportTime(processMaxTicks);
			float liftTime = BottlingMachineLogic.getLiftTime(processMaxTicks);
			float fProcess = process.processTick;

			float itemX;
			float itemY = 0;
			float itemFill = 0;

			if(fProcess < transportTime)
				itemX = .5f*fProcess/transportTime;
			else if(fProcess < (processMaxTicks-transportTime))
			{
				itemX = .5f;
				if(fProcess < transportTime+liftTime)
					lift = (fProcess-transportTime)/liftTime;
				else if(fProcess < processMaxTicks-(transportTime+liftTime))
				{
					lift = 1;
					itemFill = (fProcess-(transportTime+liftTime))/(processMaxTicks-2*(transportTime+liftTime));
				}
				else
				{
					lift = 1-(fProcess-(processMaxTicks-transportTime-liftTime))/liftTime;
					itemFill = 1;
				}
				lift *= .125f;
				if(lift > pixelHeight)
					itemY += lift-pixelHeight;
			}
			else
			{
				itemX = .5f+.5f*(fProcess-(processMaxTicks-transportTime))/transportTime;
				itemFill = 1;
			}
			itemDisplays.add(new ItemDisplay(
					itemFill,
					new Vector3f((itemX-0.5f)*BottlingMachineLogic.TRANSLATION_DISTANCE, itemY-.15625f, 1),
					process
			));
		}

		matrixStack.pushPose();

		matrixStack.translate(0, lift, 0);
		renderModelPart(matrixStack, solidBuilder, facing, combinedLightIn, combinedOverlayIn, "lift");
		matrixStack.translate(0, -lift, 0);

		matrixStack.popPose();

		float dir = facing==Direction.SOUTH?Mth.PI: facing==Direction.NORTH?0: facing==Direction.EAST?-Mth.HALF_PI: Mth.HALF_PI;
		matrixStack.mulPose(new Quaternionf().rotateY(dir));

		FluidStack fs = state.tank.getFluid();
		if(!fs.isEmpty())
		{
			final float tankWidth = 7;
			matrixStack.pushPose();
			float level = fs.getAmount()/(float)state.tank.getCapacity();
			matrixStack.translate(-.21875, .376, 1.21875);
			matrixStack.scale(pixelHeight, pixelHeight, pixelHeight);
			matrixStack.translate(tankWidth/2, 0, -tankWidth/2);
			float h = level*9;
			// TODO does not work on fabulous
			VertexConsumer builder = originalBuffer.getBuffer(RenderType.translucent());
			for(int i = 0; i < 4; ++i)
			{
				matrixStack.pushPose();
				matrixStack.translate(0, 0, -tankWidth/2);
				GuiHelper.drawRepeatedFluidSprite(builder, matrixStack, fs, -tankWidth/2, 0, tankWidth, h);
				matrixStack.popPose();
				matrixStack.mulPose(new Quaternionf().rotateY(Mth.HALF_PI));
			}

			matrixStack.mulPose(new Quaternionf().rotateX(-Mth.HALF_PI));
			GuiHelper.drawRepeatedFluidSprite(builder, matrixStack, fs, -tankWidth/2, -tankWidth/2, tankWidth, tankWidth);
			matrixStack.mulPose(new Quaternionf().rotateX(Mth.PI));
			matrixStack.translate(0, 0, -h);
			GuiHelper.drawRepeatedFluidSprite(builder, matrixStack, fs, -tankWidth/2, -tankWidth/2, tankWidth, tankWidth);

			matrixStack.popPose();
		}


		//DRAW ITEMS HERE
		for(ItemDisplay item : itemDisplays)
		{
			List<ItemStack> display = item.process.getDisplayItem(te.getLevel());

			matrixStack.pushPose();
			matrixStack.translate(item.translation.x(), item.translation.y(), item.translation.z());
			matrixStack.scale(.4375f, .4375f, .4375f);

			if(!ClientUtils.mc().getMainRenderTarget().isStencilEnabled())
			{
				for(ItemStack displayS : display)
					ClientUtils.mc().getItemRenderer().renderStatic(
							displayS, ItemDisplayContext.FIXED,
							combinedLightIn, combinedOverlayIn, matrixStack, bufferIn,
							te.getLevel(), 0
					);
			}
			else
			{
				float h0 = -.5f;
				float h1 = h0+item.itemFill;

				for(ItemStack inputS : item.process.inputItems)
					renderItemPart(bufferIn, matrixStack, inputS, h0, h1, combinedLightIn, combinedOverlayIn, 0, te.getLevel());
				for(ItemStack displayS : display)
					renderItemPart(bufferIn, matrixStack, displayS, h0, h1, combinedLightIn, combinedOverlayIn, 1, te.getLevel());
			}

			matrixStack.popPose();
		}
		matrixStack.popPose();
	}

	public static void renderModelPart(PoseStack matrixStack, VertexConsumer builder, Direction facing,
									   int combinedLightIn, int combinedOverlayIn, String... parts)
	{
		ModelData data = ModelDataUtils.single(DynamicSubmodelCallbacks.getProperty(), VisibilityList.show(parts));
		matrixStack.pushPose();
		matrixStack.translate(-.5, -.5, -.5);
		List<BakedQuad> quads = DYNAMIC.getNullQuads(data);
		rotateForFacing(matrixStack, facing);
		RenderUtils.renderModelTESRFast(quads, builder, matrixStack, combinedLightIn, combinedOverlayIn);
		matrixStack.popPose();
	}

	private void renderItemPart(
			MultiBufferSource baseBuffer, PoseStack matrix, ItemStack item, float minY, float maxY,
			int combinedLightIn, int combinedOverlayIn, int ref, Level level
	)
	{
		PoseStack innerStack = new PoseStack();
		innerStack.last().pose().mul(matrix.last().pose());
		innerStack.last().normal().mul(matrix.last().normal());
		// TODO may be broken?
		MultiBufferSource stencilWrapper = IERenderTypes.wrapWithStencil(
				baseBuffer,
				vertexBuilder -> {
					innerStack.pushPose();
					innerStack.mulPose(new Quaternionf()
							.rotateY((90.0F-ClientUtils.mc().getEntityRenderDispatcher().camera.getYRot())*Mth.DEG_TO_RAD)
					);
					RenderUtils.renderBox(vertexBuilder, innerStack, -.5f, minY, -.5f, .5f, maxY, .5f);
					innerStack.popPose();
				},
				"min"+minY+"max"+maxY,
				ref
		);
		BatchingRenderTypeBuffer batchBuffer = new BatchingRenderTypeBuffer();
		ClientUtils.mc().getItemRenderer().renderStatic(
				item, ItemDisplayContext.FIXED, combinedLightIn, combinedOverlayIn, matrix, batchBuffer, level, 0
		);
		batchBuffer.pipe(stencilWrapper);
	}
}
