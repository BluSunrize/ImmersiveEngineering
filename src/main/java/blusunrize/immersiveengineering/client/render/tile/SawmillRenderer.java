/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.SawmillTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.SawmillTileEntity.SawmillProcess;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class SawmillRenderer extends TileEntityRenderer<SawmillTileEntity>
{
	public static DynamicModel<Direction> BLADE;

	public SawmillRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(SawmillTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		//Grab model
		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.sawmill)
			return;

		//Outer GL Wrapping, initial translation
		matrixStack.push();
		matrixStack.translate(.5, 0, .5);
		bufferIn = TileRenderUtils.mirror(te, matrixStack, bufferIn);


		IVertexBuilder solidBuilder = bufferIn.getBuffer(RenderType.getSolid());

		Direction facing = te.getFacing();
		float dir = facing==Direction.SOUTH?180: facing==Direction.NORTH?0: facing==Direction.EAST?-90: 90;
		matrixStack.rotate(new Quaternion(0, dir, 0, true));

		// Sawblade
		boolean sawblade = !te.sawblade.isEmpty();
		if(sawblade)
		{
			matrixStack.push();
			matrixStack.translate(1, .125, -.5);
			float spin = te.animation_bladeRotation;
			if(te.shouldRenderAsActive())
				spin += 36f*partialTicks;
			matrixStack.rotate(new Quaternion(0, 0, spin, true));
			ClientUtils.renderModelTESRFast(
					BLADE.getNullQuads(Direction.NORTH, state),
					solidBuilder, matrixStack, combinedLightIn, combinedOverlayIn);
			matrixStack.pop();
		}

		// Items
		for(SawmillProcess process : te.sawmillProcessQueue)
		{
			float relative = process.getRelativeProcessStep();
			ItemStack rendered = process.getCurrentStack(sawblade);
			renderItem(rendered, relative, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
		}
		matrixStack.pop();
	}

	private void renderItem(ItemStack stack, float progress, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		float xOffset = -2.5f+progress*5;
		matrixStack.push();
		matrixStack.translate(xOffset, .375, 0);
		matrixStack.rotate(new Quaternion(0, 0, 90, true));
		ClientUtils.mc().getItemRenderer().renderItem(stack, TransformType.FIXED,
				combinedLightIn, combinedOverlayIn, matrixStack, bufferIn);
		matrixStack.pop();
	}
}
