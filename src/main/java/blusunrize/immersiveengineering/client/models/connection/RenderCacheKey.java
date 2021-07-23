/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.mixin.accessors.client.RenderTypeAccess;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.state.BlockState;

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
		if(!Arrays.equals(additionalProperties, o.additionalProperties))
			return false;
		return state.equals(o.state);
	}

	@Override
	public int hashCode()
	{
		int val = layer==null?0: ((RenderTypeAccess)layer).getName().hashCode();
		final int prime = 31;
		val = prime*val+state.hashCode();
		val = prime*val+Arrays.hashCode(additionalProperties);
		return val;
	}
}
