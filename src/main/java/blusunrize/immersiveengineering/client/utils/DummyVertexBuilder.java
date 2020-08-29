/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.utils;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import javax.annotation.Nonnull;

public class DummyVertexBuilder implements IVertexBuilder
{
	@Nonnull
	@Override
	public IVertexBuilder pos(double x, double y, double z)
	{
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder color(int red, int green, int blue, int alpha)
	{
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder tex(float u, float v)
	{
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder overlay(int u, int v)
	{
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder lightmap(int u, int v)
	{
		return this;
	}

	@Nonnull
	@Override
	public IVertexBuilder normal(float x, float y, float z)
	{
		return this;
	}

	@Override
	public void endVertex()
	{

	}
}
