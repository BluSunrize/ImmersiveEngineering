/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.common.blocks.metal.SheetmetalTankTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraftforge.fluids.FluidStack;

public class SheetmetalTankRenderer extends TileEntityRenderer<SheetmetalTankTileEntity>
{
	public SheetmetalTankRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(SheetmetalTankTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.formed||tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;
		matrixStack.push();

		matrixStack.translate(.5, 0, .5);

		FluidStack fs = tile.tank.getFluid();
		matrixStack.translate(0, 3.5f, 0);
		float baseScale = .0625f;
		matrixStack.scale(baseScale, -baseScale, baseScale);

		float xx = -.5f;
		float zz = 1.5f-.004f;
		xx /= baseScale;
		zz /= baseScale;
		for(int i = 0; i < 4; i++)
		{
			matrixStack.push();
			matrixStack.translate(xx, 0, zz);

			Matrix4f mat = matrixStack.getLast().getMatrix();
			final IVertexBuilder builder = bufferIn.getBuffer(IERenderTypes.TRANSLUCENT_POSITION_COLOR);
			builder.pos(mat, -4, -4, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			builder.pos(mat, -4, 20, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			builder.pos(mat, 20, 20, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();
			builder.pos(mat, 20, -4, 0).color(0x22, 0x22, 0x22, 0xff).endVertex();

			if(!fs.isEmpty())
			{
				float h = fs.getAmount()/(float)tile.tank.getCapacity();
				matrixStack.translate(0, 0, .004f);
				ClientUtils.drawRepeatedFluidSprite(bufferIn.getBuffer(RenderType.getSolid()), matrixStack, fs,
						0, 0+(1-h)*16, 16, h*16);
			}
			matrixStack.pop();
			matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), 90, true));
		}
		matrixStack.pop();
	}

}