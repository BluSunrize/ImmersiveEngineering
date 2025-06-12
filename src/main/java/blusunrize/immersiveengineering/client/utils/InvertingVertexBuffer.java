/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;

public class InvertingVertexBuffer extends CollectingVertexBuilder
{
	private final int verticesPerPrimitive;
	private final VertexConsumer baseBuilder;

	public InvertingVertexBuffer(int verticesPerPrimitive, VertexConsumer baseBuilder)
	{
		this.verticesPerPrimitive = verticesPerPrimitive;
		this.baseBuilder = baseBuilder;
	}

	@Override
	public void putBulkData(PoseStack.Pose pose, BakedQuad quad, float[] brightness, float red, float green, float blue, float alpha, int[] lightmap, int packedOverlay, boolean p_331268_)
	{
		super.putBulkData(pose, quad, brightness, red, green, blue, alpha, lightmap, packedOverlay, p_331268_);
		this.endVertex();
	}

	@Override
	public void endVertex()
	{
		super.endVertex();
		if(vertices.size() >= verticesPerPrimitive)
		{
			for(int i = vertices.size()-1; i >= 0; --i)
				vertices.get(i).pipe(baseBuilder);
			clear();
		}
	}
}
