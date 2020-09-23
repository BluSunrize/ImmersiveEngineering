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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.fluids.FluidStack;

public class BottlingMachineRenderer extends TileEntityRenderer<BottlingMachineTileEntity>
{
	public static DynamicModel<Direction> DYNAMIC;
	private static final float pixelHeight = 1f/16f;

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
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.bottlingMachine)
			return;
		IBakedModel model = DYNAMIC.get(te.getFacing());

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
			if(process==null||process.processTick==process.maxProcessTick)
				continue;

			//+partialTicks
			float processTimer = ((float)process.processTick)/process.maxProcessTick*120;

			float itemX = -1.5f;//-1;
			float itemY = -.15625f;// -.34375f;
			float itemZ = 1;//-.9375f;
			float itemFill = 0;//ClientUtils.mc().player.ticksExisted%100; //0f;

			if(processTimer <= 35)//slide
			{
				itemX += processTimer/35f*1.5;
			}
			else if(processTimer <= 85)//slide
			{
				itemX = 0;
				if(processTimer <= 55)
					lift = (processTimer-35)/20f*.125f;
				else if(processTimer <= 65)
				{
					lift = .125f;
					itemFill = (processTimer-55)/10f;
				}
				else
				{
					lift = (85-processTimer)/20f*.125f;
					itemFill = 1;
				}
				if (lift > pixelHeight)
				itemY += lift-pixelHeight;
			}
			else
			{
				itemX = (processTimer-85)/35f*1.5f;
				itemFill = 1;
			}
			itemDisplays[i] = new float[]{processTimer, itemX, itemY, itemZ, itemFill};

		}

		matrixStack.push();

		matrixStack.translate(0, lift, 0);
		renderModelPart(blockRenderer, matrixStack, solidBuilder, te.getWorldNonnull(), state, model, blockPos, combinedOverlayIn, "lift");
		matrixStack.translate(0, -lift, 0);

		matrixStack.pop();

		float dir = te.getFacing()==Direction.SOUTH?180: te.getFacing()==Direction.NORTH?0: te.getFacing()==Direction.EAST?-90: 90;
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

	public static void renderModelPart(final BlockRendererDispatcher blockRenderer, MatrixStack matrixStack,
									   IVertexBuilder builder, World world, BlockState state, IBakedModel model,
									   BlockPos pos, int combinedOverlayIn, String... parts)
	{
		IModelData data = new SinglePropertyModelData<>(new IEObjState(VisibilityList.show(parts)), Model.IE_OBJ_STATE);
		matrixStack.push();
		matrixStack.translate(-.5, -.5, -.5);
		blockRenderer.getBlockModelRenderer().renderModel(world, model, state, pos, matrixStack, builder, true, world.rand, 0,
				combinedOverlayIn, data);
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
}
