/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.connection;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEProperties.ConnectionModelData;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.RenderData;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.models.BakedIEModel;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.ModelLoader.White;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BakedConnectionModel extends BakedIEModel
{
	Lazy<TextureAtlasSprite> textureAtlasSprite =  Lazy.of(() -> Minecraft.getInstance().getModelManager()
			.getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
			.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/wire")));
	public static final Cache<ModelKey, IBakedModel> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();
	@Nullable
	private final IBakedModel base;
	private final ImmutableSet<String> layers;

	public BakedConnectionModel(@Nullable IBakedModel basic, Collection<String> layers)
	{
		base = basic;
		this.layers = ImmutableSet.copyOf(layers);
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		if(side==null&&extraData.hasProperty(Model.CONNECTIONS))
		{
			//TODO more general system for this!
			Object[] additional = null;
			if(extraData.hasProperty(Model.TILEENTITY_PASSTHROUGH))
			{
				TileEntity te = extraData.getData(Model.TILEENTITY_PASSTHROUGH);
				if(te instanceof IEBlockInterfaces.ICacheData)
					additional = ((IEBlockInterfaces.ICacheData)te).getCacheData();
			}
			RenderCacheKey ad = new RenderCacheKey(state, null, additional);
			Set<Connection.RenderData> data = new HashSet<>();
			ConnectionModelData orig = extraData.getData(Model.CONNECTIONS);
			assert (orig!=null);
			for(Connection c : orig.connections)
			{
				ConnectionPoint here = c.getEndFor(orig.here);
				data.add(new Connection.RenderData(c, c.getEndB().equals(here),
						ClientUtils.getVertexCountForSide(here, c, RenderData.POINTS_PER_WIRE)));
			}
			ModelKey key = new ModelKey(data, ad, orig.here);
			try
			{
				IBakedModel ret = cache.get(key, () -> new AssembledBakedModel(key, textureAtlasSprite.get(), base));
				return ret.getQuads(state, null, rand, extraData);
			} catch(ExecutionException e)
			{
				e.printStackTrace();
			}
		}
		return getBaseQuads(MinecraftForgeClient.getRenderLayer(), state, side, rand, extraData);
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
		if(base!=null)
			return base.getParticleTexture();
		else
			return White.instance();
	}

	@Nonnull
	@Override
	public ItemOverrideList getOverrides()
	{
		return ItemOverrideList.EMPTY;
	}

	private List<BakedQuad> getBaseQuads(RenderType currentLayer, BlockState state, Direction side, Random rand, IModelData data)
	{
		if(base!=null&&(currentLayer==null||layers.contains(currentLayer.toString())))
			return base.getQuads(state, side, rand, data);
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull ILightReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		if(base==null)
			return EmptyModelData.INSTANCE;
		else
			return base.getModelData(world, pos, state, tileData);
	}

	public class AssembledBakedModel implements IBakedModel
	{
		ModelKey key;
		List<BakedQuad>[] lists;
		TextureAtlasSprite texture;

		public AssembledBakedModel(ModelKey key, TextureAtlasSprite tex, IBakedModel b)
		{
			this.key = key;
			texture = tex;
		}

		@Nonnull
		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand, IModelData data)
		{
			RenderType layer = MinecraftForgeClient.getRenderLayer();
			if(layer!=RenderType.getSolid()&&layer!=RenderType.getTranslucent())
				return getBaseQuads(layer, state, side, rand, data);
			if(lists==null)
				lists = ClientUtils.convertConnectionFromBlockstate(key.here, key.connections, texture);
			List<BakedQuad> l = new ArrayList<>(lists[layer==RenderType.getSolid()?0: 1]);
			l.addAll(getBaseQuads(layer, state, side, rand, data));
			return Collections.synchronizedList(l);
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand)
		{
			return ImmutableList.of();
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

		//TODO
		@Override
		public boolean func_230044_c_()
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
			throw new UnsupportedOperationException();
		}

		@Nonnull
		@Override
		public ItemOverrideList getOverrides()
		{
			return ItemOverrideList.EMPTY;
		}

	}

	private static class ModelKey
	{
		private final Set<Connection.RenderData> connections;
		private final RenderCacheKey state;
		private final BlockPos here;//TODO include in equals?

		private ModelKey(Set<RenderData> connections, RenderCacheKey state, BlockPos here)
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
