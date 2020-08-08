package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CollectingVertexBuilder implements IVertexBuilder
{
	protected final List<List<Consumer<IVertexBuilder>>> vertices = new ArrayList<>();
	private List<Consumer<IVertexBuilder>> nextVertex = new ArrayList<>();

	@Nonnull
	@Override
	public IVertexBuilder pos(double x, double y, double z)
	{
		nextVertex.add(vb -> vb.pos(x, y, z));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder color(int red, int green, int blue, int alpha)
	{
		nextVertex.add(vb -> vb.color(red, green, blue, alpha));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder tex(float u, float v)
	{
		nextVertex.add(vb -> vb.tex(u, v));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder overlay(int u, int v)
	{
		nextVertex.add(vb -> vb.overlay(u, v));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder lightmap(int u, int v)
	{
		nextVertex.add(vb -> vb.lightmap(u, v));
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder normal(float x, float y, float z)
	{
		nextVertex.add(vb -> vb.normal(x, y, z));
		return this;
	}

	@Override
	public void endVertex()
	{
		nextVertex.add(IVertexBuilder::endVertex);
		vertices.add(nextVertex);
		nextVertex = new ArrayList<>();
	}

	public void pipeAndClear(IVertexBuilder out)
	{
		for(List<Consumer<IVertexBuilder>> l : vertices)
			for(Consumer<IVertexBuilder> c : l)
				c.accept(out);
		vertices.clear();
	}
}
