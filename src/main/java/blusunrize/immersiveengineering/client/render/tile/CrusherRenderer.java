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
import blusunrize.immersiveengineering.common.blocks.metal.CrusherTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import java.util.List;

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

		Direction dir = te.getFacing();

		boolean b = te.shouldRenderAsActive();
		float angle = te.animation_barrelRotation+(b?18*partialTicks: 0);

		matrixStack.push();

		matrixStack.translate(.5, 1.5, .5);
		matrixStack.translate(te.getFacing().getXOffset()*.5, 0, te.getFacing().getZOffset()*.5);

		matrixStack.push();
		matrixStack.rotate(new Quaternion(new Vector3f(-te.getFacing().getZOffset(), 0, te.getFacing().getXOffset()), angle, true));
		renderBarrel(matrixStack, bufferIn, dir, combinedLightIn, combinedOverlayIn);
		matrixStack.pop();

		matrixStack.push();
		matrixStack.translate(te.getFacing().getXOffset()*-1, 0, te.getFacing().getZOffset()*-1);
		matrixStack.rotate(new Quaternion(new Vector3f(-te.getFacing().getZOffset(), 0, te.getFacing().getXOffset()), -angle, true));
		renderBarrel(matrixStack, bufferIn, dir, combinedLightIn, combinedOverlayIn);
		matrixStack.pop();

		matrixStack.pop();
	}

	private void renderBarrel(MatrixStack matrix, IRenderTypeBuffer buffer, Direction facing, int light, int overlay)
	{
		matrix.push();
		matrix.translate(-.5, -.5, -.5);
		List<BakedQuad> quads = BARREL.getNullQuads(facing, Multiblocks.crusher.getDefaultState());
		RenderUtils.renderModelTESRFast(quads, buffer.getBuffer(RenderType.getSolid()), matrix, light, overlay);
		matrix.pop();
	}

}