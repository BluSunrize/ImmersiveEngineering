/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.smart;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.energy.wires.ImmersiveNetHandler.Connection;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.IOBJModelCallback;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.util.Utils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ConnModelReal implements IBakedModel
{

	TextureAtlasSprite textureAtlasSprite = Minecraft.getMinecraft().getTextureMapBlocks()
			.getAtlasSprite(ImmersiveEngineering.MODID.toLowerCase(Locale.ENGLISH)+":blocks/wire");
	public static final Cache<Pair<Byte, ExtBlockstateAdapter>, IBakedModel> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();
	private final IBakedModel base;
	private final ImmutableSet<BlockRenderLayer> layers;

	public ConnModelReal(IBakedModel basic, ImmutableSet<BlockRenderLayer> layers)
	{
		base = basic;
		this.layers = layers;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		if(side==null&&state instanceof IExtendedBlockState)
		{
			IExtendedBlockState ext = (IExtendedBlockState)state;
			Object[] additional = null;
			if(ext.getUnlistedProperties().containsKey(IEProperties.TILEENTITY_PASSTHROUGH))
			{
				TileEntity te = ext.getValue(IEProperties.TILEENTITY_PASSTHROUGH);
				if(te instanceof IEBlockInterfaces.ICacheData)
					additional = ((IEBlockInterfaces.ICacheData)te).getCacheData();
			}
			int x = 0, z = 0;
			if(ext.getUnlistedProperties().containsKey(IEProperties.CONNECTIONS))
			{
				Set<Connection> conns = ext.getValue(IEProperties.CONNECTIONS);
				if(conns!=null&&conns.size() > 0)
				{
					BlockPos tmp = conns.iterator().next().start;
					x = (tmp.getX()%16+16)%16;
					z = (tmp.getZ()%16+16)%16;
				}
			}
			ExtBlockstateAdapter ad = new ExtBlockstateAdapter(ext, null, ExtBlockstateAdapter.ONLY_OBJ_CALLBACK, additional);
			Pair<Byte, ExtBlockstateAdapter> key = new ImmutablePair<>((byte)((x<<4)|z), ad);
			try
			{
				IBakedModel ret = cache.get(key, () -> new AssembledBakedModel(ext, textureAtlasSprite, base, layers));
				return ret.getQuads(state, null, rand);
			} catch(ExecutionException e)
			{
				e.printStackTrace();
			}
		}
		return getBaseQuads(MinecraftForgeClient.getRenderLayer(), state, side, rand);
	}

	@Override
	public boolean isAmbientOcclusion()
	{
		return false;
	}

	@Override
	public boolean isGui3d()
	{
		return false;
	}

	@Override
	public boolean isBuiltInRenderer()
	{
		return false;
	}

	@Nonnull
	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return base.getParticleTexture();
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}

	private List<BakedQuad> getBaseQuads(BlockRenderLayer currentLayer, IBlockState state, EnumFacing side, long rand)
	{
		if(layers.contains(currentLayer)||currentLayer==null)
			return base.getQuads(state, side, rand);
		return ImmutableList.of();
	}

	public class AssembledBakedModel implements IBakedModel
	{
		IBakedModel basic;
		IExtendedBlockState extendedState;
		List<BakedQuad>[] lists;
		TextureAtlasSprite texture;
		private final ImmutableSet<BlockRenderLayer> layers;//TODO remove

		public AssembledBakedModel(IExtendedBlockState iExtendedBlockState, TextureAtlasSprite tex, IBakedModel b,
								   ImmutableSet<BlockRenderLayer> layers)
		{
			basic = b;
			extendedState = iExtendedBlockState;
			texture = tex;
			this.layers = layers;
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
		{
			BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
			if(layer!=BlockRenderLayer.SOLID&&layer!=BlockRenderLayer.TRANSLUCENT)
				return getBaseQuads(layer, state, side, rand);
			if(lists==null)
				lists = ClientUtils.convertConnectionFromBlockstate(extendedState, texture);
			List<BakedQuad> l = new ArrayList<>(lists[layer==BlockRenderLayer.SOLID?0: 1]);
			l.addAll(getBaseQuads(layer, state, side, rand));
			return Collections.synchronizedList(l);
		}

		@Override
		public boolean isAmbientOcclusion()
		{
			return false;
		}

		@Override
		public boolean isGui3d()
		{
			return false;
		}

		@Override
		public boolean isBuiltInRenderer()
		{
			return false;
		}

		@Nonnull
		@Override
		public TextureAtlasSprite getParticleTexture()
		{
			return base.getParticleTexture();
		}

		@Nonnull
		@Override
		public ItemOverrideList getOverrides()
		{
			return ItemOverrideList.NONE;
		}

	}

	public static class ExtBlockstateAdapter
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
				for(IProperty<?> p : ((IBlockState)o).getPropertyKeys())
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

}
