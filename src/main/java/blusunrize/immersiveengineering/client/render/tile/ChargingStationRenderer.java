/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ChargingStationTileEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

public class ChargingStationRenderer extends TileEntityRenderer<ChargingStationTileEntity>
{
	public ChargingStationRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(ChargingStationTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(te.getWorldNonnull().isBlockLoaded(te.getPos()))
		{
			te.particles.render(matrixStack, te.getPos(), bufferIn, partialTicks);
			matrixStack.push();
			matrixStack.translate(.5, .3125, .5);
			matrixStack.scale(.75f, .75f, .75f);
			switch(te.getFacing())
			{
				case NORTH:
					matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), 180, true));
					break;
				case SOUTH:
					break;
				case WEST:
					matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), -90, true));
					break;
				case EAST:
					matrixStack.rotate(new Quaternion(new Vector3f(0, 1, 0), 90, true));
					break;
			}
			if(!te.inventory.get(0).isEmpty())
			{
				matrixStack.push();
				float scale = .625f;
				matrixStack.scale(scale, scale, 1);
				ClientUtils.mc().getItemRenderer().renderItem(te.inventory.get(0), TransformType.FIXED, combinedLightIn,
						combinedOverlayIn, matrixStack, bufferIn);
				matrixStack.pop();
			}
			matrixStack.pop();
		}
	}
}