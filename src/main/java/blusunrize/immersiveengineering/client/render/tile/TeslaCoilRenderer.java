/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.client.utils.IERenderTypes;
import blusunrize.immersiveengineering.client.utils.TransformingVertexBuilder;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.TeslaCoilBlockEntity.LightningAnimation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TeslaCoilRenderer extends IEBlockEntityRenderer<TeslaCoilBlockEntity>
{
	@Override
	public void render(TeslaCoilBlockEntity tile, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn)
	{
		if(!tile.getLevelNonnull().hasChunkAt(tile.getBlockPos()))
			return;

		for(LightningAnimation animation : tile.effectMap)
		{
			if(animation.shoudlRecalculateLightning())
				animation.createLightning(ApiUtils.RANDOM_SOURCE);

			double tx = tile.getBlockPos().getX();
			double ty = tile.getBlockPos().getY();
			double tz = tile.getBlockPos().getZ();
			drawAnimation(animation, tx, ty, tz, new float[]{77/255f, 74/255f, 152/255f, .75f}, 4f, bufferIn, matrixStack);
			drawAnimation(animation, tx, ty, tz, new float[]{1, 1, 1, 1}, 1f, bufferIn, matrixStack);
		}
	}

	public static void drawAnimation(LightningAnimation animation, double tileX, double tileY, double tileZ,
									 float[] rgba, float lineWidth, MultiBufferSource buffers,
									 PoseStack transform)
	{
		RenderType type = IERenderTypes.getLines(lineWidth);
		TransformingVertexBuilder builder = new TransformingVertexBuilder(buffers, type, transform);
		builder.defaultColor((int)(255*rgba[0]), (int)(255*rgba[1]), (int)(255*rgba[2]), (int)(255*rgba[3]));

		drawLine(animation.startPos, animation.subPoints.get(0), tileX, tileY, tileZ, builder);
		for(int i = 0; i < animation.subPoints.size()-1; i++)
			drawLine(animation.subPoints.get(i), animation.subPoints.get(i+1), tileX, tileY, tileZ, builder);
		Vec3 end = (animation.targetEntity!=null?animation.targetEntity.position(): animation.targetPos);
		drawLine(animation.subPoints.get(animation.subPoints.size()-1), end, tileX, tileY, tileZ, builder);
	}

	private static void drawLine(Vec3 start, Vec3 end, double offX, double offY, double offZ, VertexConsumer out)
	{
		Vec3 normal = new Vec3(start.x()-end.x(), start.y()-end.y(), start.z()-end.z()).normalize();
		out.addVertex((float)(start.x-offX), (float)(start.y-offY), (float)(start.z-offZ))
				.setNormal((float)normal.x, (float)normal.y, (float)normal.z);
		out.addVertex((float)(end.x-offX), (float)(end.y-offY), (float)(end.z-offZ))
				.setNormal((float)normal.x, (float)normal.y, (float)normal.z);
	}

	@Override
	public AABB getRenderBoundingBox(TeslaCoilBlockEntity blockEntity)
	{
		if(blockEntity.renderBB==null)
			blockEntity.renderBB = new AABB(new Vec3(-8, -8, -8), new Vec3(8, 8, 8)).move(blockEntity.getBlockPos());
		return blockEntity.renderBB;
	}

}