/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilTileEntity.LightningAnimation;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;

public class TeslaCoilRenderer extends TileEntityRenderer<TeslaCoilTileEntity>
{
	public TeslaCoilRenderer(TileEntityRendererDispatcher rendererDispatcherIn)
	{
		super(rendererDispatcherIn);
	}

	@Override
	public void render(TeslaCoilTileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(tile.isDummy()||!tile.getWorldNonnull().isBlockLoaded(tile.getPos()))
			return;

		for(LightningAnimation animation : TeslaCoilTileEntity.effectMap.get(tile.getPos()))
		{
			if(animation.shoudlRecalculateLightning())
				animation.createLightning(Utils.RAND);

			double tx = tile.getPos().getX();
			double ty = tile.getPos().getY();
			double tz = tile.getPos().getZ();
			drawAnimation(animation, tx, ty, tz, new float[]{77/255f, 74/255f, 152/255f, .75f}, 4f, bufferIn, matrixStack);
			drawAnimation(animation, tx, ty, tz, new float[]{1, 1, 1, 1}, 1f, bufferIn, matrixStack);
		}
	}

	public static void drawAnimation(LightningAnimation animation, double tileX, double tileY, double tileZ,
									 float[] rgba, float lineWidth, IRenderTypeBuffer buffers,
									 MatrixStack transform)
	{
		IVertexBuilder base = buffers.getBuffer(IERenderTypes.getLines(lineWidth));
		TransformingVertexBuilder builder = new TransformingVertexBuilder(base, transform);
		builder.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
		List<Vector3d> subs = animation.subPoints;
		builder.pos(animation.startPos.x-tileX, animation.startPos.y-tileY, animation.startPos.z-tileZ).endVertex();

		for(Vector3d sub : subs)
		{
			builder.pos(sub.x-tileX, sub.y-tileY, sub.z-tileZ).endVertex();
			builder.pos(sub.x-tileX, sub.y-tileY, sub.z-tileZ).endVertex();
		}

		Vector3d end = (animation.targetEntity!=null?animation.targetEntity.getPositionVector(): animation.targetPos).add(-tileX, -tileY, -tileZ);
		builder.pos(end.x, end.y, end.z).endVertex();
	}
}