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
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraftforge.client.model.SimpleModelTransform;

import javax.annotation.Nonnull;

public class SimpleUVModelTransform implements IModelTransform
{
	public static final SimpleModelTransform IDENTITY = new SimpleModelTransform(TransformationMatrix.identity());

	private final ImmutableMap<?, TransformationMatrix> map;
	private final TransformationMatrix base;
	private final boolean uvLock;

	public SimpleUVModelTransform(ImmutableMap<?, TransformationMatrix> map, boolean uvLock)
	{
		this(map, TransformationMatrix.identity(), uvLock);
	}

	public SimpleUVModelTransform(ImmutableMap<?, TransformationMatrix> map, TransformationMatrix base, boolean uvLock)
	{
		this.map = map;
		this.base = base;
		this.uvLock = uvLock;
	}

	@Override
	public boolean isUvLock()
	{
		return uvLock;
	}

	@Override
	@Nonnull
	public TransformationMatrix getRotation()
	{
		return base;
	}

	@Override
	public TransformationMatrix getPartTransformation(Object part)
	{
		return map.getOrDefault(part, TransformationMatrix.identity());
	}
}
