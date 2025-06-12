/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.vertex.VertexConsumer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

//TODO actually handle default color!
public class CollectingVertexBuilder implements VertexConsumer
{
	protected final List<Vertex> vertices = new ArrayList<>();
	private final List<Vertex> pool = new ArrayList<>();
	private Vertex currentVertex = null;

	@Nonnull
	@Override
	public VertexConsumer addVertex(float x, float y, float z)
	{
		if(currentVertex!=null)
			this.endVertex();
		currentVertex = makeVertex();
		currentVertex.order.add(Element.POSITION);
		currentVertex.position[0] = x;
		currentVertex.position[1] = y;
		currentVertex.position[2] = z;
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setColor(int red, int green, int blue, int alpha)
	{
		currentVertex.order.add(Element.COLOR);
		currentVertex.color[0] = red;
		currentVertex.color[1] = green;
		currentVertex.color[2] = blue;
		currentVertex.color[3] = alpha;
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setUv(float u, float v)
	{
		currentVertex.order.add(Element.UV);
		currentVertex.uv[0] = u;
		currentVertex.uv[1] = v;
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setUv1(int u, int v)
	{
		currentVertex.order.add(Element.OVERLAY);
		currentVertex.overlay[0] = u;
		currentVertex.overlay[1] = v;
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setUv2(int u, int v)
	{
		currentVertex.order.add(Element.UV2);
		currentVertex.uv2[0] = u;
		currentVertex.uv2[1] = v;
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer setNormal(float x, float y, float z)
	{
		currentVertex.order.add(Element.NORMAL);
		currentVertex.normal[0] = x;
		currentVertex.normal[1] = y;
		currentVertex.normal[2] = z;
		return this;
	}

	protected void endVertex()
	{
		if(currentVertex!=null)
			vertices.add(currentVertex);
	}

	public void pipeAndClear(VertexConsumer out)
	{
		endVertex();
		for(Vertex v : vertices)
			v.pipe(out);
		clear();
	}

	protected void clear()
	{
		pool.addAll(vertices);
		this.currentVertex = null;
		vertices.clear();
	}

	private Vertex makeVertex()
	{
		if(!pool.isEmpty())
		{
			Vertex result = pool.remove(pool.size()-1);
			result.order.clear();
			return result;
		}
		else
			return new Vertex();
	}

	protected static class Vertex
	{
		private final float[] position = new float[3];
		private final int[] color = new int[4];
		private final float[] uv = new float[2];
		private final int[] overlay = new int[2];
		private final int[] uv2 = new int[2];
		private final float[] normal = new float[3];
		private final List<Element> order = new ArrayList<>();

		public void pipe(VertexConsumer out)
		{
			for(Element e : order)
				switch(e)
				{
					case POSITION -> out.addVertex(position[0], position[1], position[2]);
					case COLOR -> out.setColor(color[0], color[1], color[2], color[3]);
					case UV -> out.setUv(uv[0], uv[1]);
					case OVERLAY -> out.setUv1(overlay[0], overlay[1]);
					case UV2 -> out.setUv2(uv2[0], uv2[1]);
					case NORMAL -> out.setNormal(normal[0], normal[1], normal[2]);
				}
		}
	}

	protected enum Element
	{
		POSITION,
		COLOR,
		UV,
		OVERLAY,
		UV2,
		NORMAL
	}
}