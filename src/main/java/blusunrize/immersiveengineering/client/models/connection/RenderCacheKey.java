/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.mixin.accessors.client.RenderTypeAccess;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.RenderType;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Arrays;

public class RenderCacheKey
{
	private final BlockState state;
	private final RenderType layer;
	private final Object[] additionalProperties;

	public RenderCacheKey(BlockState state, RenderType l, Object... additional)
	{
		this.state = state;
		layer = l;
		additionalProperties = additional;
	}

	public RenderCacheKey(BlockState state, RenderType l, RenderCacheKey baseKey, IModelData data,
						  ImmutableList<Object> additional, ModelProperty<?>... toSave)
	{
		this(state, l, additional, getAllProperties(baseKey, data, toSave));
	}

	public RenderCacheKey(BlockState state, RenderType l, IModelData data, ImmutableList<Object> additional,
						  ModelProperty<?>... toSave)
	{
		this(state, l, null, data, additional, toSave);
	}

	private static Object[] getAllProperties(RenderCacheKey base, IModelData data, ModelProperty<?>[] toSave)
	{
		Object[] ret = Arrays.copyOf(base.additionalProperties, toSave.length+base.additionalProperties.length);
		for(int i = 0; i < toSave.length; ++i)
		{
			ret[i+base.additionalProperties.length] = data.getData(toSave[i]);
		}
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj==this)
			return true;
		if(!(obj instanceof RenderCacheKey))
			return false;
		RenderCacheKey o = (RenderCacheKey)obj;
		if(o.layer!=layer)
			return false;
		if(!Utils.areArraysEqualIncludingBlockstates(additionalProperties, o.additionalProperties))
			return false;
		return state.equals(o.state);
	}

	@Override
	public int hashCode()
	{
		int val = layer==null?0: ((RenderTypeAccess)layer).getName().hashCode();
		final int prime = 31;
		val = prime*val+Utils.hashBlockstate(state);
		val = prime*val+Arrays.hashCode(additionalProperties);
		return val;
	}
}
