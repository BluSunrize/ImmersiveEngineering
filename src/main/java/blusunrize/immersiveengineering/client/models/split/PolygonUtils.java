/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.client.utils.BakedQuadBuilder;
import com.google.common.base.Preconditions;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import org.joml.Vector3f;
import org.joml.Vector4f;
import malte0811.modelsplitter.math.Vec3d;
import malte0811.modelsplitter.model.Polygon;
import malte0811.modelsplitter.model.UVCoords;
import malte0811.modelsplitter.model.Vertex;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PolygonUtils
{
	private static int getOffset(VertexFormatElement element)
	{
		int offset = 0;
		for(VertexFormatElement e : DefaultVertexFormat.BLOCK.getElements())
			if(e==element)
				return offset/4;
			else
				offset += e.getByteSize();
		throw new IllegalStateException("Did not find element with usage "+element.getUsage().name()+" and type "+element.getType().name());
	}

	public static Polygon<ExtraQuadData> toPolygon(BakedQuad quad)
	{
		List<Vertex> vertices = new ArrayList<>(4);
		final int posOffset = getOffset(DefaultVertexFormat.ELEMENT_POSITION);
		final int uvOffset = getOffset(DefaultVertexFormat.ELEMENT_UV);
		final int normalOffset = getOffset(DefaultVertexFormat.ELEMENT_NORMAL);
		final int colorOffset = getOffset(DefaultVertexFormat.ELEMENT_COLOR);
		final int color = quad.getVertices()[colorOffset];
		for(int v = 0; v < 4; ++v)
		{
			final int baseOffset = v*DefaultVertexFormat.BLOCK.getVertexSize()/4;
			int packedNormal = quad.getVertices()[normalOffset+baseOffset];
			final Vec3d normalVec = new Vec3d(
					(byte)(packedNormal),
					(byte)(packedNormal >> 8),
					(byte)(packedNormal >> 16)
			).normalize();
			final UVCoords uv = new UVCoords(
					Float.intBitsToFloat(quad.getVertices()[uvOffset+baseOffset]),
					Float.intBitsToFloat(quad.getVertices()[uvOffset+baseOffset+1])
			);
			final Vec3d pos = new Vec3d(
					Float.intBitsToFloat(quad.getVertices()[baseOffset+posOffset]),
					Float.intBitsToFloat(quad.getVertices()[baseOffset+posOffset+1]),
					Float.intBitsToFloat(quad.getVertices()[baseOffset+posOffset+2])
			);
			vertices.add(new Vertex(pos, normalVec, uv));
			Preconditions.checkState(quad.getVertices()[baseOffset+colorOffset]==color, "All vertices in a quad must have the same color, otherwise we need changes in BMS");
		}
		return new Polygon<>(vertices, new ExtraQuadData(
				quad.getSprite(),
				new Vector4f((color&255)/255f, ((color >> 8)&255)/255f, ((color >> 16)&255)/255f, (color >> 24)/255f))
		);
	}

	public static BakedQuad toBakedQuad(Polygon<ExtraQuadData> poly, ModelState transform)
	{
		return toBakedQuad(poly.getPoints(), poly.getTexture(), transform.getRotation().blockCenterToCorner(), true);
	}

	public static BakedQuad toBakedQuad(List<Vertex> points, ExtraQuadData data, Transformation rotation, boolean absoluteUV)
	{
		Preconditions.checkArgument(points.size()==4);
		BakedQuadBuilder quadBuilder = new BakedQuadBuilder();
		Vector3f normal = new Vector3f();
		for(Vertex v : points)
		{
			Vector4f pos = new Vector4f();
			pos.set(toArray(v.position(), 4));
			normal.set(toArray(v.normal(), 3));
			rotation.transformPosition(pos);
			rotation.transformNormal(normal);
			pos.mul(1 / pos.w);
			final double epsilon = 1e-5;
			for(int i = 0; i < 2; ++i)
			{
				if(Math.abs(i-pos.x()) < epsilon) pos.setComponent(0, i);
				if(Math.abs(i-pos.y()) < epsilon) pos.setComponent(1, i);
				if(Math.abs(i-pos.z()) < epsilon) pos.setComponent(2, i);
			}
			quadBuilder.putVertexData(
					new Vec3(pos.x(), pos.y(), pos.z()),
					new Vec3(normal),
					absoluteUV?v.uv().u(): data.sprite().getU(16*v.uv().u()),
					absoluteUV?v.uv().v(): data.sprite().getV(16*(1-v.uv().v())),
					new float[]{data.color.x(), data.color.y(), data.color.z(), data.color.w()},
					1
			);
		}
		return quadBuilder.bake(-1, Direction.getNearest(normal.x(), normal.y(), normal.z()), data.sprite(), true);
	}

	private static float[] toArray(Vec3d vec, int length)
	{
		float[] ret = new float[length];
		for(int i = 0; i < 3; ++i)
			ret[i] = (float)vec.get(i);
		for(int i = 3; i < length; ++i)
			ret[i] = 1;
		return ret;
	}

	public record ExtraQuadData(TextureAtlasSprite sprite, Vector4f color)
	{
	}
}