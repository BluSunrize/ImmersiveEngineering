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

public class DummyVertexBuilder implements VertexConsumer
{
	public static final DummyVertexBuilder INSTANCE = new DummyVertexBuilder();

	private DummyVertexBuilder()
	{
	}

	@Override
	public VertexConsumer addVertex(float p_350761_, float p_350704_, float p_350711_)
	{
		return this;
	}

	@Override
	public VertexConsumer setColor(int p_350535_, int p_350875_, int p_350886_, int p_350775_)
	{
		return this;
	}

	@Override
	public VertexConsumer setUv(float p_350572_, float p_350917_)
	{
		return this;
	}

	@Override
	public VertexConsumer setUv1(int p_350815_, int p_350629_)
	{
		return this;
	}

	@Override
	public VertexConsumer setUv2(int p_350859_, int p_351004_)
	{
		return this;
	}

	@Override
	public VertexConsumer setNormal(float p_350429_, float p_350286_, float p_350836_)
	{
		return this;
	}
}
