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
import java.util.function.Consumer;

public class CollectingVertexBuilder implements VertexConsumer
{
	protected final List<List<Consumer<VertexConsumer>>> vertices = new ArrayList<>();
	private List<Consumer<VertexConsumer>> nextVertex = new ArrayList<>();

	@Nonnull
	@Override
	public VertexConsumer vertex(double x, double y, double z)
	{
		nextVertex.add(vb -> vb.vertex(x, y, z));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha)
	{
		nextVertex.add(vb -> vb.color(red, green, blue, alpha));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer uv(float u, float v)
	{
		nextVertex.add(vb -> vb.uv(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer overlayCoords(int u, int v)
	{
		nextVertex.add(vb -> vb.overlayCoords(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer uv2(int u, int v)
	{
		nextVertex.add(vb -> vb.uv2(u, v));
		return this;
	}

	@Nonnull
	@Override
	public VertexConsumer normal(float x, float y, float z)
	{
		nextVertex.add(vb -> vb.normal(x, y, z));
		return this;
	}

	@Override
	public void endVertex()
	{
		nextVertex.add(VertexConsumer::endVertex);
		vertices.add(nextVertex);
		nextVertex = new ArrayList<>();
	}

	public void pipeAndClear(VertexConsumer out)
	{
		for(List<Consumer<VertexConsumer>> l : vertices)
			for(Consumer<VertexConsumer> c : l)
				c.accept(out);
		vertices.clear();
	}
}
