/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.data.EmptyModelData;

public class CrusherRenderer extends TileEntityRenderer<CrusherTileEntity>
{
	public static DynamicModel<Direction> BARREL;

	public CrusherRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(CrusherTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.crusher)
			return;
		Direction dir = te.getFacing();
		IBakedModel model = BARREL.get(dir);

		boolean b = te.shouldRenderAsActive();
		float angle = te.animation_barrelRotation+(b?18*partialTicks: 0);

		matrixStack.push();

		matrixStack.translate(.5, 1.5, .5);
		matrixStack.translate(te.getFacing().getXOffset()*.5, 0, te.getFacing().getZOffset()*.5);

		matrixStack.push();
		matrixStack.rotate(new Quaternion(new Vector3f(-te.getFacing().getZOffset(), 0, te.getFacing().getXOffset()), angle, true));
		renderPart(matrixStack, bufferIn, blockPos, blockRenderer, te, model, state, combinedOverlayIn);
		matrixStack.pop();

		matrixStack.push();
		matrixStack.translate(te.getFacing().getXOffset()*-1, 0, te.getFacing().getZOffset()*-1);
		matrixStack.rotate(new Quaternion(new Vector3f(-te.getFacing().getZOffset(), 0, te.getFacing().getXOffset()), -angle, true));
		renderPart(matrixStack, bufferIn, blockPos, blockRenderer, te, model, state, combinedOverlayIn);
		matrixStack.pop();

		matrixStack.pop();
	}

	private void renderPart(MatrixStack matrix, IRenderTypeBuffer buffer, BlockPos pos, BlockRendererDispatcher blockRenderer,
							TileEntity te, IBakedModel model, BlockState state, int overlay)
	{
		matrix.push();
		matrix.translate(-.5, -.5, -.5);
		blockRenderer.getBlockModelRenderer().renderModel(te.getWorld(), model, state, pos, matrix,
				buffer.getBuffer(RenderType.getSolid()), true, te.getWorld().rand,
				0, overlay, EmptyModelData.INSTANCE);
		matrix.pop();
	}

}