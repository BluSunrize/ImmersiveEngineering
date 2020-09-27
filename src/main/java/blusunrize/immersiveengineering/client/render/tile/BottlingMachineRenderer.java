/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.IEProperties.IEObjState;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.IEProperties.VisibilityList;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.BatchingRenderTypeBuffer;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.BottlingMachineTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.BottlingMachineTileEntity.BottlingProcess;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class BottlingMachineRenderer extends TileEntityRenderer<BottlingMachineTileEntity>
{
	public static DynamicModel<Direction> DYNAMIC;
	private static final float TRANSLATION_DISTANCE = 3f;
	private static final float STANDARD_TRANSPORT_TIME = 16f*(TRANSLATION_DISTANCE/2); //16 frames in conveyor animation, 1 frame/tick, 2.5 blocks of total translation distance, halved because transport time just affects half the distance
	private static final float STANDARD_LIFT_TIME = 3.75f;
	private static final float MIN_CYCLE_TIME = 60f; //set >= 2*(STANDARD_LIFT_TIME+STANDARD_TRANSPORT_TIME)

	public BottlingMachineRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(BottlingMachineTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		//Grab model
		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.bottlingMachine)
			return;
		Direction facing = te.getFacing();

		final float pixelHeight = 1f/16f;

		//Outer GL Wrapping, initial translation
		matrixStack.push();
		matrixStack.translate(.5, .5, .5);
		final IRenderTypeBuffer originalBuffer = bufferIn;
		bufferIn = TileRenderUtils.mirror(te, matrixStack, bufferIn);

		//Item Displacement
		float[][] itemDisplays = new float[te.bottlingProcessQueue.size()][];
		//Animations
		float lift = 0;

		IVertexBuilder solidBuilder = bufferIn.getBuffer(RenderType.getSolid());
		for(int i = 0; i < itemDisplays.length; i++)
		{
			BottlingProcess process = te.bottlingProcessQueue.get(i);
			if(process==null)
				continue;
			float processMaxTicks = process.maxProcessTick;
			float transportTime = getTransportTime(processMaxTicks);
			float liftTime = getLiftTime(processMaxTicks);
			//+partialTicks
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
			itemDisplays[i] = new float[]{fProcess, (itemX-0.5f)*TRANSLATION_DISTANCE, itemY-.15625f, 1, itemFill};
		}

		matrixStack.push();

		matrixStack.translate(0, lift, 0);
		renderModelPart(matrixStack, solidBuilder, facing, combinedLightIn, combinedOverlayIn, "lift");
		matrixStack.translate(0, -lift, 0);

		matrixStack.pop();

		float dir = facing==Direction.SOUTH?180: facing==Direction.NORTH?0: facing==Direction.EAST?-90: 90;
		matrixStack.rotate(new Quaternion(0, dir, 0, true));

		float scale = pixelHeight;
		FluidStack fs = te.tanks[0].getFluid();
		if(!fs.isEmpty())
		{
			final float tankWidth = 7;
			matrixStack.push();
			float level = fs.getAmount()/(float)te.tanks[0].getCapacity();
			matrixStack.translate(-.21875, .376, 1.21875);
			matrixStack.scale(scale, scale, scale);
			matrixStack.translate(tankWidth/2, 0, -tankWidth/2);
			float h = level*9;
			IVertexBuilder builder = originalBuffer.getBuffer(RenderType.getTranslucent());
			for(int i = 0; i < 4; ++i)
			{
				matrixStack.push();
				matrixStack.translate(0, 0, -tankWidth/2);
				ClientUtils.drawRepeatedFluidSprite(builder, matrixStack, fs, -tankWidth/2, 0, tankWidth, h);
				matrixStack.pop();
				matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), 90, true));
			}

			matrixStack.rotate(new Quaternion(new Vector3f(1, 0, 0), -90, true));
			ClientUtils.drawRepeatedFluidSprite(builder, matrixStack, fs, -tankWidth/2, -tankWidth/2, tankWidth, tankWidth);
			matrixStack.rotate(new Quaternion(new Vector3f(1, 0, 0), 180, true));
			matrixStack.translate(0, 0, -h);
			ClientUtils.drawRepeatedFluidSprite(builder, matrixStack, fs, -tankWidth/2, -tankWidth/2, tankWidth, tankWidth);

			matrixStack.pop();
		}


		//DRAW ITEMS HERE
		for(int i = 0; i < itemDisplays.length; i++)
			if(itemDisplays[i]!=null)
			{
				BottlingProcess process = te.bottlingProcessQueue.get(i);
				if(process==null)
					continue;

				ItemStack display = itemDisplays[i][4]==0||process.items.get(1).isEmpty()?process.items.get(0): process.items.get(1);
				scale = .4375f;

				matrixStack.translate(itemDisplays[i][1], itemDisplays[i][2], itemDisplays[i][3]);
				matrixStack.scale(scale, scale, scale);

				if(itemDisplays[i][4]==0)
					ClientUtils.mc().getItemRenderer().renderItem(process.items.get(0), TransformType.FIXED,
							combinedLightIn, combinedOverlayIn, matrixStack, bufferIn);
				else if(itemDisplays[i][4]==1||!ClientUtils.mc().getFramebuffer().isStencilEnabled())
					ClientUtils.mc().getItemRenderer().renderItem(display, TransformType.FIXED,
							combinedLightIn, combinedOverlayIn, matrixStack, bufferIn);
				else
				{
					float h0 = -.5f;
					float h1 = h0+itemDisplays[i][4];
					renderItemPart(bufferIn, matrixStack, process.items.get(0), h0, h1, combinedLightIn, combinedOverlayIn, 0);
					renderItemPart(bufferIn, matrixStack, process.items.get(1), h0, h1, combinedLightIn, combinedOverlayIn, 1);
				}

				matrixStack.scale(1/scale, 1/scale, 1/scale);
				matrixStack.translate(-itemDisplays[i][1], -itemDisplays[i][2], -itemDisplays[i][3]);
			}
		matrixStack.pop();
	}

	public static void renderModelPart(MatrixStack matrixStack, IVertexBuilder builder, Direction facing,
									   int combinedLightIn, int combinedOverlayIn, String... parts)
	{
		IModelData data = new SinglePropertyModelData<>(new IEObjState(VisibilityList.show(parts)), Model.IE_OBJ_STATE);
		matrixStack.push();
		matrixStack.translate(-.5, -.5, -.5);
		List<BakedQuad> quads = DYNAMIC.getNullQuads(facing, Multiblocks.bottlingMachine.getDefaultState(), data);
		ClientUtils.renderModelTESRFast(quads, builder, matrixStack, combinedLightIn, combinedOverlayIn);
		matrixStack.pop();
	}

	private void renderItemPart(IRenderTypeBuffer baseBuffer, MatrixStack matrix, ItemStack item, float minY, float maxY,
								int combinedLightIn, int combinedOverlayIn, int ref)
	{
		MatrixStack innerStack = new MatrixStack();
		innerStack.getLast().getMatrix().mul(matrix.getLast().getMatrix());
		innerStack.getLast().getNormal().mul(matrix.getLast().getNormal());
		IRenderTypeBuffer stencilWrapper = IERenderTypes.wrapWithStencil(
				baseBuffer,
				vertexBuilder -> {
					innerStack.push();
					innerStack.rotate(new Quaternion(new Vector3f(0.0F, 1.0F, 0.0F), 90.0F-ClientUtils.mc().getRenderManager().info.getYaw(), true));
					ClientUtils.renderBox(vertexBuilder, innerStack, -.5f, minY, -.5f, .5f, maxY, .5f);
					innerStack.pop();
				},
				"min"+minY+"max"+maxY,
				ref
		);
		BatchingRenderTypeBuffer batchBuffer = new BatchingRenderTypeBuffer();
		ClientUtils.mc().getItemRenderer().renderItem(item, TransformType.FIXED,
				combinedLightIn, combinedOverlayIn, matrix, batchBuffer);
		batchBuffer.pipe(stencilWrapper);
	}

	public static float getTransportTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_TRANSPORT_TIME;
		else
			return processMaxTicks*STANDARD_TRANSPORT_TIME/MIN_CYCLE_TIME;
	}

	public static float getLiftTime(float processMaxTicks)
	{
		if(processMaxTicks >= MIN_CYCLE_TIME)
			return STANDARD_LIFT_TIME;
		else
			return processMaxTicks*STANDARD_LIFT_TIME/MIN_CYCLE_TIME;
	}
}
