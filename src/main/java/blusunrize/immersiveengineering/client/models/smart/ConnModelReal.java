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
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;

public class ConnModelReal implements IBakedModel
{

	TextureAtlasSprite textureAtlasSprite = Minecraft.getMinecraft().getTextureMapBlocks()
			.getAtlasSprite(ImmersiveEngineering.MODID.toLowerCase(Locale.ENGLISH) + ":blocks/wire");
	public static final HashMap<Pair<Byte, ExtBlockstateAdapter>, IBakedModel> cache = new HashMap<>();
	IBakedModel base;

	public ConnModelReal(IBakedModel basic)
	{
		base = basic;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
	{
		if(side==null&&state instanceof IExtendedBlockState)
		{
			IExtendedBlockState iExtendedBlockState = (IExtendedBlockState) state;
			ExtBlockstateAdapter ad = new ExtBlockstateAdapter(iExtendedBlockState, null, ExtBlockstateAdapter.ONLY_OBJ_CALLBACK);
			int x = 0, z = 0;
			if (iExtendedBlockState.getUnlistedProperties().containsKey(IEProperties.CONNECTIONS))
			{
				Set<Connection> conns = iExtendedBlockState.getValue(IEProperties.CONNECTIONS);
				if (conns!=null&&conns.size()>0)
				{
					BlockPos tmp = conns.iterator().next().start;
					x = (tmp.getX()%16+16)%16;
					z = (tmp.getZ()%16+16)%16;
				}
			}
			Pair<Byte, ExtBlockstateAdapter> key = new ImmutablePair<>((byte)((x<<4)|z), ad);
			IBakedModel ret = cache.get(key);
			if (ret==null)
			{
				ret = new AssembledBakedModel(iExtendedBlockState, textureAtlasSprite, base, rand);
				cache.put(key, ret);
			}
			return ret.getQuads(state, side, rand);
		}
		return base.getQuads(state, side, rand);
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

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return base.getParticleTexture();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms()
	{
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.NONE;
	}

	public class AssembledBakedModel implements IBakedModel
	{
		IBakedModel basic;
		IExtendedBlockState extendedState;
		List<BakedQuad>[] lists;
		TextureAtlasSprite texture;

		public AssembledBakedModel(IExtendedBlockState iExtendedBlockState, TextureAtlasSprite tex, IBakedModel b, long posRand)
		{
			basic = b;
			extendedState = iExtendedBlockState;
			texture = tex;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
		{
			BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
			if (layer != BlockRenderLayer.SOLID&&layer!=BlockRenderLayer.TRANSLUCENT)
				return basic.getQuads(state, side, rand);
			if(lists==null)
				lists = ClientUtils.convertConnectionFromBlockstate(extendedState, texture);
			List<BakedQuad> l = new ArrayList<>(lists[layer==BlockRenderLayer.SOLID?0:1]);
			l.addAll(basic.getQuads(state, side, rand));
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

		@Override
		public TextureAtlasSprite getParticleTexture()
		{
			return base.getParticleTexture();
		}

		@Override
		public ItemCameraTransforms getItemCameraTransforms()
		{
			return ItemCameraTransforms.DEFAULT;
		}

		@Override
		public ItemOverrideList getOverrides()
		{
			return ItemOverrideList.NONE;
		}

	}

	public static class ExtBlockstateAdapter
	{
		public static final Set<Object> ONLY_OBJ_CALLBACK = ImmutableSet.of(IOBJModelCallback.PROPERTY);
		public static final Set<Object> CONNS_OBJ_CALLBACK = ImmutableSet.of(IOBJModelCallback.PROPERTY, IEProperties.CONNECTIONS);
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
			if (s.getUnlistedNames().contains(IOBJModelCallback.PROPERTY))
			{
				IOBJModelCallback callback = s.getValue(IOBJModelCallback.PROPERTY);
				if (callback!=null)
					extraCacheKey = callback.getClass()+";"+callback.getCacheKey(state);
				else
					extraCacheKey = null;
			}
			else
				extraCacheKey = null;
		}
		public ExtBlockstateAdapter(IExtendedBlockState s, BlockRenderLayer l, Set<Object> ignored, Object[] additional)
		{
			this(s, l, ignored);
			additionalProperties = additional;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == this)
				return true;
			if (!(obj instanceof ExtBlockstateAdapter))
				return false;
			ExtBlockstateAdapter o = (ExtBlockstateAdapter) obj;
			if (o.layer!=layer)
				return false;
			if (extraCacheKey==null^o.extraCacheKey==null)
				return false;
			if (extraCacheKey!=null&&!extraCacheKey.equals(o.extraCacheKey))
				return false;
			if (!Arrays.equals(additionalProperties, o.additionalProperties))
				return false;
			for(IProperty<?> i : state.getPropertyKeys())
			{
				if(!o.state.getProperties().containsKey(i))
					return false;
				if (ignoredProperties.contains(i))
					continue;
				Object valThis = state.getValue(i);
				Object valOther = o.state.getValue(i);
				if(valThis==null&&valOther==null)
					continue;
				else if(valOther == null || !valOther.equals(state.getValue(i)))
					return false;
			}
			for(IUnlistedProperty<?> i : state.getUnlistedNames())
			{
				if(!o.state.getUnlistedProperties().containsKey(i))
					return false;
				if (ignoredProperties.contains(i))
					continue;
				Object valThis = state.getValue(i);
				Object valOther = o.state.getValue(i);
				if(valThis==null&&valOther==null)
					continue;
				else if (valOther == null || !valOther.equals(valThis))
					return false;
			}
			return true;
		}

		@Override
		public int hashCode()
		{
			int val = layer==null?0:layer.ordinal();
			final int prime = 31;
			if (extraCacheKey!=null)
				val = val*prime+extraCacheKey.hashCode();
			for (IProperty<?> n : state.getPropertyKeys())
				if (!ignoredProperties.contains(n))
				{
					Object o = state.getValue(n);
					val = prime * val + (o == null ? 0 : o.hashCode());
				}
			for (IUnlistedProperty<?> n : state.getUnlistedNames())
				if (!ignoredProperties.contains(n))
				{
					Object o = state.getValue(n);
					val = prime * val + (o == null ? 0 : o.hashCode());
				}
			val = prime*val+Arrays.hashCode(additionalProperties);
			return val;
		}
	}

}
