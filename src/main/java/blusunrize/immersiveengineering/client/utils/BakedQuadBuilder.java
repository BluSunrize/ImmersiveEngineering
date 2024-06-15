/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public class BakedQuadBuilder
{
	public static final VertexFormat FORMAT = DefaultVertexFormat.BLOCK;

	private int nextVertex = 0;
	private int[] data = new int[FORMAT.getVertexSize()];

	public void putVertexData(
			Vec3 pos, Vec3 faceNormal, double u, double v, TextureAtlasSprite sprite, float[] colour, float alpha
	)
	{
		// TODO pass un-16-nification further up in the logic
		putVertexData(pos, faceNormal, sprite.getU((float)(u/16)), sprite.getV((float)(v/16)), colour, alpha);
	}

	public void putVertexData(Vec3 pos, Vec3 faceNormal, double u, double v, float[] colour, float alpha)
	{
		int next = nextVertex*FORMAT.getVertexSize()/4;

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

		++nextVertex;
	}

	public BakedQuad bake(int tint, Direction side, TextureAtlasSprite texture, boolean shade)
	{
		return new BakedQuad(data, tint, side, texture, shade);
	}
}
