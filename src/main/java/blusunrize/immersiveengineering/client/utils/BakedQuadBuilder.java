package blusunrize.immersiveengineering.client.utils;

import com.google.common.base.Preconditions;
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

		data[next++] = (int)(faceNormal.x*255)|((int)(faceNormal.y*255)<<8)|((int)(faceNormal.z*255)<<16);


		Preconditions.checkState(next==FORMAT.getIntegerSize());
		++nextVertex;
	}

	public BakedQuad bake(int tint, Direction side, TextureAtlasSprite texture, boolean shade)
	{
		return new BakedQuad(data, tint, side, texture, shade);
	}
}
