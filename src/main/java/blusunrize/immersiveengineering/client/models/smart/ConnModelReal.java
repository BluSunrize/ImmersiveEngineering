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
import blusunrize.immersiveengineering.api.IEProperties.ConnectionModelData;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.Connection.RenderData;
import blusunrize.immersiveengineering.api.energy.wires.ConnectionPoint;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class ConnModelReal implements IBakedModel
{

	TextureAtlasSprite textureAtlasSprite = Minecraft.getMinecraft().getTextureMapBlocks()
			.getAtlasSprite(ImmersiveEngineering.MODID.toLowerCase(Locale.ENGLISH)+":blocks/wire");
	public static final Cache<ModelKey, IBakedModel> cache = CacheBuilder.newBuilder()
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
			ExtBlockstateAdapter ad = new ExtBlockstateAdapter(ext, null, ExtBlockstateAdapter.CONNS_OBJ_CALLBACK, additional);
			Set<Connection.RenderData> data = new HashSet<>();
			ConnectionModelData orig = ext.getValue(IEProperties.CONNECTIONS);
			for(Connection c : orig.connections)
			{
				ConnectionPoint here = c.getEndFor(orig.here);
				data.add(new Connection.RenderData(c, c.getEndB().equals(here),
						ClientUtils.getVertexCountForSide(here, c, RenderData.POINTS_PER_WIRE)));
			}
			ModelKey key = new ModelKey(data, ad, orig.here);
			try
			{
				cache.invalidateAll();//TODO remove
				IBakedModel ret = cache.get(key, () -> new AssembledBakedModel(key, textureAtlasSprite, base));
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
		ModelKey key;
		List<BakedQuad>[] lists;
		TextureAtlasSprite texture;

		public AssembledBakedModel(ModelKey key, TextureAtlasSprite tex, IBakedModel b)
		{
			basic = b;
			this.key = key;
			texture = tex;
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand)
		{
			BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
			if(layer!=BlockRenderLayer.SOLID&&layer!=BlockRenderLayer.TRANSLUCENT)
				return getBaseQuads(layer, state, side, rand);
			if(lists==null)
				lists = ClientUtils.convertConnectionFromBlockstate(key.here, key.connections, texture);
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

	private class ModelKey
	{
		private final Set<Connection.RenderData> connections;
		private final ExtBlockstateAdapter state;
		private final BlockPos here;//TODO include in equals?

		private ModelKey(Set<RenderData> connections, ExtBlockstateAdapter state, BlockPos here)
		{
			this.connections = connections;
			this.state = state;
			this.here = here;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this==o) return true;
			if(o==null||getClass()!=o.getClass()) return false;

			ModelKey that = (ModelKey)o;

			if(!connections.equals(that.connections)) return false;
			return state.equals(that.state);
		}

		@Override
		public int hashCode()
		{
			int result = connections.hashCode();
			result = 31*result+state.hashCode();
			return result;
		}
	}
}
