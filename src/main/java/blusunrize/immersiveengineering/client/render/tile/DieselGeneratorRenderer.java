/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.RenderUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Multiblocks;
import blusunrize.immersiveengineering.common.blocks.metal.DieselGeneratorTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;

public class DieselGeneratorRenderer extends TileEntityRenderer<DieselGeneratorTileEntity>
{
	public static DynamicModel<Direction> FAN;

	public DieselGeneratorRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(DieselGeneratorTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!te.formed||te.isDummy()||!te.getWorldNonnull().isBlockLoaded(te.getPos()))
			return;

		BlockPos blockPos = te.getPos();
		BlockState state = te.getWorld().getBlockState(blockPos);
		if(state.getBlock()!=Multiblocks.dieselGenerator)
			return;

		matrixStack.push();
		matrixStack.translate(0, .6875, 0);
		matrixStack.translate(0.5, 0, 0.5);

		matrixStack.rotate(new Quaternion(new Vector3f(Vector3d.copy(te.getFacing().getDirectionVec())),
				te.animation_fanRotation+(te.animation_fanRotationStep*partialTicks), true));
		matrixStack.translate(-0.5, 0, -0.5);

		List<BakedQuad> quads = FAN.getNullQuads(te.getFacing(), state);
		RenderUtils.renderModelTESRFast(quads, bufferIn.getBuffer(RenderType.getSolid()), matrixStack, combinedLightIn,
				combinedOverlayIn);

		matrixStack.pop();
	}
}