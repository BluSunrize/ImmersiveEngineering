/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement.Usage;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class BakedQuadBuilder
{
	public static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;

	private int nextVertex = 0;
	private int[] data = new int[FORMAT.getIntegerSize()*4];

	public void putVertexData(
			Vec3 pos, Vec3 faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha
	)
	{
		putVertexData(pos, faceNormal, sprite.getU(u), sprite.getV(v), colour, alpha);
	}

	public void putVertexData(Vec3 pos, Vec3 faceNormal, double u, double v, float[] colour, float alpha)
	{
		int next = nextVertex*FORMAT.getIntegerSize();

		data[next++] = Float.floatToIntBits((float)pos.x);
		data[next++] = Float.floatToIntBits((float)pos.y);
		data[next++] = Float.floatToIntBits((float)pos.z);

		data[next++] = (int)(colour[0]*255)|
				((int)(colour[1]*255)<<8)|
				((int)(colour[2]*255)<<16)|
				((int)(colour[3]*alpha*255)<<24);

		data[next++] = Float.floatToIntBits((float)u);
		data[next++] = Float.floatToIntBits((float)v);

		data[next++] = 0;

		data[next] |= (int)(faceNormal.x*127)&255;
		data[next] |= ((int)(faceNormal.y*127)&255)<<8;
		data[next] |= ((int)(faceNormal.z*127)&255)<<16;
		++next;

		// Optifine adds some extra elements when shaders are enabled (all marked as PADDING), skip those
		int extraPaddingBytes = 0;
		for(int i = 6; i < FORMAT.getElements().size(); ++i)
		{
			final var extraElement = FORMAT.getElements().get(i);
			Preconditions.checkState(extraElement.getUsage()==Usage.PADDING);
			extraPaddingBytes += extraElement.getByteSize();
		}
		Preconditions.checkState(extraPaddingBytes%Integer.BYTES==0);
		next += extraPaddingBytes/Integer.BYTES;

		++nextVertex;
		Preconditions.checkState(next==nextVertex*FORMAT.getIntegerSize());
	}

	public BakedQuad bake(int tint, Direction side, TextureAtlasSprite texture, boolean shade)
	{
		return new BakedQuad(data, tint, side, texture, shade);
	}
}
