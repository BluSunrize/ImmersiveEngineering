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
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.Connection.RenderData;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.client.models.BakedIEModel;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import blusunrize.immersiveengineering.mixin.accessors.client.RenderTypeAccess;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Either;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockDisplayReader;
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
import java.util.function.Function;

public class BakedConnectionModel<T> extends BakedIEModel
{
	Lazy<TextureAtlasSprite> textureAtlasSprite = Lazy.of(() -> Minecraft.getInstance().getModelManager()
			.getAtlasTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
			.getSprite(new ResourceLocation(ImmersiveEngineering.MODID, "block/wire")));
	public static final Cache<ModelKey, SpecificConnectionModel> cache = CacheBuilder.newBuilder()
			.expireAfterAccess(2, TimeUnit.MINUTES)
			.maximumSize(100)
			.build();
	@Nullable
	private final IBakedModel base;
	private final Either<ICacheKeyProvider<T>, T> extraCacheKey;
	private final ImmutableSet<String> layers;

	public BakedConnectionModel(@Nullable IBakedModel basic, Collection<String> layers, T extraCacheKey)
	{
		base = basic;
		this.layers = ImmutableSet.copyOf(layers);
		this.extraCacheKey = Either.right(extraCacheKey);
	}

	public BakedConnectionModel(
			@Nullable IBakedModel basic, Collection<String> layers, ICacheKeyProvider<T> extraCacheKey
	)
	{
		base = basic;
		this.layers = ImmutableSet.copyOf(layers);
		this.extraCacheKey = Either.left(extraCacheKey);
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		if(side==null&&extraData.hasProperty(Model.CONNECTIONS))
		{
			T extraKey = extraCacheKey.map(ickp -> ickp.getKey(state, side, rand, extraData), Function.identity());
			RenderCacheKey ad = new RenderCacheKey(state, null, extraKey);
			Set<Connection.RenderData> data = new HashSet<>();
			ConnectionModelData orig = extraData.getData(Model.CONNECTIONS);
			assert (orig!=null);
			for(Connection c : orig.connections)
			{
				ConnectionPoint here = c.getEndFor(orig.here);
				data.add(new Connection.RenderData(c, c.getEndB().equals(here),
						getSolidVertexCountForSide(here, c, RenderData.POINTS_PER_WIRE)));
			}
			ModelKey key = new ModelKey(data, ad, orig.here);
			try
			{
				SpecificConnectionModel ret = cache.get(key, () -> new SpecificConnectionModel(key, textureAtlasSprite.get()));
				RenderType current = MinecraftForgeClient.getRenderLayer();
				List<BakedQuad> connectionQuads = new ArrayList<>(ret.getQuads(current));
				connectionQuads.addAll(getBaseQuads(current, state, side, rand, extraData));
				return connectionQuads;
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
		if(base!=null&&(currentLayer==null||layers.contains(((RenderTypeAccess)currentLayer).getName())))
			return base.getQuads(state, side, rand, data);
		return ImmutableList.of();
	}

	@Nonnull
	@Override
	public IModelData getModelData(@Nonnull IBlockDisplayReader world, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nonnull IModelData tileData)
	{
		if(base==null)
			return EmptyModelData.INSTANCE;
		else
			return base.getModelData(world, pos, state, tileData);
	}

	public static List<BakedQuad> convertConnectionFromBlockstate(BlockPos here, Set<Connection.RenderData> data, TextureAtlasSprite t)
	{
		List<BakedQuad> ret = new ArrayList<>();
		if(data==null)
			return ret;
		Vector3d dir = Vector3d.ZERO;

		Vector3d up = new Vector3d(0, 1, 0);
		for(Connection.RenderData connData : data)
		{
			int color = connData.color;
			float[] rgb = {(color >> 16&255)/255f, (color >> 8&255)/255f, (color&255)/255f, (color >> 24&255)/255f};
			if(rgb[3]==0)
				rgb[3] = 1;
			float radius = (float)(connData.type.getRenderDiameter()/2);

			for(int segmentEndId = 1; segmentEndId <= connData.pointsToRenderSolid; segmentEndId++)
			{
				int segmentStartId = segmentEndId-1;
				Vector3d segmentEnd = connData.getPoint(segmentEndId);
				Vector3d segmentStart = connData.getPoint(segmentStartId);
				boolean vertical = segmentEnd.x==segmentStart.x&&segmentEnd.z==segmentStart.z;
				Vector3d cross;
				if(!vertical)
				{
					dir = segmentEnd.subtract(segmentStart);
					cross = up.crossProduct(dir);
					cross = cross.scale(radius/cross.length());
				}
				else
					cross = new Vector3d(radius, 0, 0);
				Vector3d[] vertices = {segmentEnd.add(cross),
						segmentEnd.subtract(cross),
						segmentStart.subtract(cross),
						segmentStart.add(cross)};
				ret.add(ModelUtils.createSmartLightingBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.DOWN, t, rgb, false, here));
				ret.add(ModelUtils.createSmartLightingBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.UP, t, rgb, true, here));

				if(!vertical)
				{
					cross = dir.crossProduct(cross);
					cross = cross.scale(radius/cross.length());
				}
				else
					cross = new Vector3d(0, 0, radius);
				vertices = new Vector3d[]{segmentEnd.add(cross),
						segmentEnd.subtract(cross),
						segmentStart.subtract(cross),
						segmentStart.add(cross)};
				ret.add(ModelUtils.createSmartLightingBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.WEST, t, rgb, false, here));
				ret.add(ModelUtils.createSmartLightingBakedQuad(DefaultVertexFormats.BLOCK, vertices, Direction.EAST, t, rgb, true, here));
			}
		}
		return ret;
	}

	public static int getSolidVertexCountForSide(ConnectionPoint start, Connection conn, int totalPoints)
	{
		List<Integer> crossings = new ArrayList<>();
		Vector3d lastPoint = conn.getPoint(0, start);
		for(int i = 1; i <= totalPoints; i++)
		{
			Vector3d current = conn.getPoint(i/(double)totalPoints, start);
			if(crossesChunkBoundary(current, lastPoint, start.getPosition()))
				crossings.add(i);
			lastPoint = current;
		}
		boolean greater = conn.isPositiveEnd(start);
		if(crossings.size() > 0)
		{
			int index = crossings.size()/2;
			if(crossings.size()%2==0&&greater)
				index--;
			return crossings.get(index)-(greater?0: 1);
		}
		else
			return greater?totalPoints: 0;
	}

	public static boolean crossesChunkBoundary(Vector3d start, Vector3d end, BlockPos offset)
	{
		if(crossesChunkBorderSingleDim(start.x, end.x, offset.getX()))
			return true;
		if(crossesChunkBorderSingleDim(start.y, end.y, offset.getY()))
			return true;
		return crossesChunkBorderSingleDim(start.z, end.z, offset.getZ());
	}

	private static boolean crossesChunkBorderSingleDim(double a, double b, int offset)
	{
		return ((int)Math.floor(a+offset)) >> 4!=((int)Math.floor(b+offset)) >> 4;
	}

	public static class SpecificConnectionModel
	{
		private final Lazy<List<BakedQuad>> connectionQuads;

		public SpecificConnectionModel(ModelKey key, TextureAtlasSprite texture)
		{
			connectionQuads = Lazy.concurrentOf(() -> convertConnectionFromBlockstate(key.here, key.connections, texture));
		}

		@Nonnull
		public List<BakedQuad> getQuads(RenderType layer)
		{
			if(layer!=RenderType.getSolid())
				return ImmutableList.of();
			else
				return connectionQuads.get();
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
