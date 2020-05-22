package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.vertex.DefaultColorVertexBuilder;
import com.mojang.blaze3d.vertex.IVertexBuilder;

import javax.annotation.Nonnull;

public class BasicDefaultColorVertexBuilder extends DefaultColorVertexBuilder
{
	private final IVertexBuilder base;
	float u, v;
	boolean hasTex;
	double x, y, z;
	boolean hasPos;
	int overlayU, overlayV;
	boolean hasOverlay;
	int lightmapU, lightmapV;
	boolean hasLightmap;
	float normalX, normalY, normalZ;
	boolean hasNormal;

	public BasicDefaultColorVertexBuilder(IVertexBuilder base)
	{
		this.base = base;
	}

	@Nonnull
	@Override
	public IVertexBuilder pos(double x, double y, double z)
	{
		hasPos = true;
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder color(int red, int green, int blue, int alpha)
	{
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public IVertexBuilder tex(float u, float v)
	{
		hasTex = true;
		this.u = u;
		this.v = v;
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder overlay(int u, int v)
	{
		hasOverlay = true;
		overlayU = u;
		overlayV = v;
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder lightmap(int u, int v)
	{
		hasLightmap = true;
		lightmapU = u;
		lightmapV = v;
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder normal(float x, float y, float z)
	{
		hasNormal = true;
		normalX = x;
		normalY = y;
		normalZ = z;
		return this;
	}

	@Override
	public void endVertex()
	{
		if(hasPos)
		{
			base.pos(x, y, z);
			hasPos = false;
		}
		base.color(defaultRed, defaultGreen, defaultBlue, defaultAlpha);
		if(hasTex)
		{
			base.tex(u, v);
			hasTex = false;
		}
		if(hasOverlay)
		{
			base.overlay(overlayU, overlayV);
			hasOverlay = false;
		}
		if(hasLightmap)
		{
			base.lightmap(lightmapU, lightmapV);
			hasLightmap = false;
		}
		if(hasNormal)
		{
			base.normal(normalX, normalY, normalZ);
			hasNormal = false;
		}
		base.endVertex();
	}
}
