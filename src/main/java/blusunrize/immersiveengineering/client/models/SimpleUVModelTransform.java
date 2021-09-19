/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models;

import com.google.common.collect.ImmutableMap;
import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraftforge.client.model.SimpleModelTransform;

import javax.annotation.Nonnull;

public class SimpleUVModelTransform implements ModelState
{
	public static final SimpleModelTransform IDENTITY = new SimpleModelTransform(Transformation.identity());

	private final ImmutableMap<?, Transformation> map;
	private final Transformation base;
	private final boolean uvLock;

	public SimpleUVModelTransform(ImmutableMap<?, Transformation> map, boolean uvLock)
	{
		this(map, Transformation.identity(), uvLock);
	}

	public SimpleUVModelTransform(ImmutableMap<?, Transformation> map, Transformation base, boolean uvLock)
	{
		this.map = map;
		this.base = base;
		this.uvLock = uvLock;
	}

	@Override
	public boolean isUvLocked()
	{
		return uvLock;
	}

	@Override
	@Nonnull
	public Transformation getRotation()
	{
		return base;
	}

	@Override
	public Transformation getPartTransformation(Object part)
	{
		return map.getOrDefault(part, Transformation.identity());
	}
}
