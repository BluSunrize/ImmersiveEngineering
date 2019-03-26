/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.smart;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ExtBlockstateAdapter
{
	public static final Set<Object> ONLY_OBJ_CALLBACK = ImmutableSet.of(IOBJModelCallback.PROPERTY, IEProperties.TILEENTITY_PASSTHROUGH);
	public static final Set<Object> CONNS_OBJ_CALLBACK = ImmutableSet.of(IOBJModelCallback.PROPERTY, IEProperties.TILEENTITY_PASSTHROUGH,
			IEProperties.CONNECTIONS);
	final IExtendedBlockState state;
	final BlockRenderLayer layer;
	final String extraCacheKey;
	final Set<Object> ignoredProperties;
	Object[] additionalProperties = null;

	public ExtBlockstateAdapter(IExtendedBlockState s, BlockRenderLayer l, Set<Object> ignored)
	{
		state = s;
		layer = l;
		ignoredProperties = ignored;
		if(s.getUnlistedNames().contains(IOBJModelCallback.PROPERTY))
		{
			IOBJModelCallback callback = s.getValue(IOBJModelCallback.PROPERTY);
			if(callback!=null)
				extraCacheKey = callback.getClass()+";"+callback.getCacheKey(state);
			else
				extraCacheKey = null;
		}
		else
			extraCacheKey = null;
		if(Config.IEConfig.enableDebug)
		{
			//Debug code for #2887
			if(!this.equals(this)||this.hashCode()!=this.hashCode())
			{
				String debug = "Basic state:\n";
				debug += toStringDebug(state);
				debug += "Layer: "+layer+"\n";
				debug += "Cache key: "+extraCacheKey+"\nAdditional:\n";
				debug += "Ignored:\n";
				for(Object o : ignoredProperties)
					debug += toStringDebug(o);
				throw new IllegalStateException(debug);
			}
		}
	}

	private String toStringProp(IProperty<?> o)
	{
		if(o==null)
			return "PROPERTY WAS NULL";
		return o.getClass()+": listed, Type: "+o.getValueClass()+", Name: "+o.getName();
	}

	private String toStringProp(IUnlistedProperty<?> o)
	{
		if(o==null)
			return "PROPERTY WAS NULL";
		return o.getClass()+": unlisted, Type: "+o.getType()+", Name: "+o.getName();
	}

	private String toStringDebug(Object o)
	{
		if(o==null)
			return "NULL";
		if(o instanceof IBlockState)
		{
			String ret = "";
			for(IProperty<?> p : ((IBlockState)o).getProperties())
			{
				ret += toStringProp(p)+" has value "+toStringDebug(((IBlockState)o).getValue(p))+"\n";
			}
			if(o instanceof IExtendedBlockState)
			{
				for(Map.Entry<IUnlistedProperty<?>, Optional<?>> p : ((IExtendedBlockState)o).getUnlistedProperties().entrySet())
				{
					ret += toStringProp(p.getKey())+" has value "+toStringDebug(p.getValue().orElse(null))+"\n";
				}
			}
			return ret;
		}
		if(o instanceof IUnlistedProperty)
			return toStringProp((IUnlistedProperty)o);
		if(o instanceof IProperty)
			return toStringProp((IProperty)o);
		return o.getClass()+": "+o;
	}

	public ExtBlockstateAdapter(IExtendedBlockState s, BlockRenderLayer l, Set<Object> ignored, Object[] additional)
	{
		this(s, l, ignored);
		additionalProperties = additional;
		if(Config.IEConfig.enableDebug)
		{
			//Debug code for #2887
			if(!this.equals(this)||this.hashCode()!=this.hashCode())
			{
				String debug = "Basic state:\n";
				debug += toStringDebug(state);
				debug += "Layer: "+layer+"\n";
				debug += "Cache key: "+extraCacheKey+"\nAdditional:\n";
				if(additionalProperties!=null)
					for(Object o : additionalProperties)
						debug += toStringDebug(o);
				debug += "Ignored:\n";
				for(Object o : ignoredProperties)
					debug += toStringDebug(o);
				throw new IllegalStateException(debug);
			}
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj==this)
			return true;
		if(!(obj instanceof ExtBlockstateAdapter))
			return false;
		ExtBlockstateAdapter o = (ExtBlockstateAdapter)obj;
		if(o.layer!=layer)
			return false;
		if(extraCacheKey==null^o.extraCacheKey==null)
			return false;
		if(extraCacheKey!=null&&!extraCacheKey.equals(o.extraCacheKey))
			return false;
		if(!Utils.areArraysEqualIncludingBlockstates(additionalProperties, o.additionalProperties))
			return false;
		return Utils.areStatesEqual(state, o.state, ignoredProperties, true);
	}

	@Override
	public int hashCode()
	{
		int val = layer==null?0: layer.ordinal();
		final int prime = 31;
		if(extraCacheKey!=null)
			val = val*prime+extraCacheKey.hashCode();
		val = prime*val+Utils.hashBlockstate(state, ignoredProperties, true);
		val = prime*val+Arrays.hashCode(additionalProperties);
		return val;
	}
}
