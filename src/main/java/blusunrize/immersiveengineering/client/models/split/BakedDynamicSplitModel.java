package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.client.models.CompositeBakedModel;
import blusunrize.immersiveengineering.client.utils.CombinedModelData;
import blusunrize.immersiveengineering.client.utils.SinglePropertyModelData;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BakedDynamicSplitModel<K, T extends ICacheKeyProvider<K> & IBakedModel> extends CompositeBakedModel<T>
{
	private final Set<Vec3i> parts;
	private final IModelTransform transform;
	private final Cache<K, Map<Vec3i, List<BakedQuad>>> subModelCache = CacheBuilder.newBuilder()
			.maximumSize(10)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build();

	public BakedDynamicSplitModel(T base, Set<Vec3i> parts, IModelTransform transform)
	{
		super(base);
		this.parts = parts;
		this.transform = transform;
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData)
	{
		BlockPos offset = extraData.getData(Model.SUBMODEL_OFFSET);
		if(offset==null)
			return super.getQuads(state, side, rand, extraData);
		K key = base.getKey(state, side, rand, extraData);
		if(key==null)
			return ImmutableList.of();
		try
		{
			subModelCache.invalidateAll();
			return subModelCache.get(
					key,
					() -> {
						List<BakedQuad> baseQuads = base.getQuads(state, side, rand, extraData);
						return BakedBasicSplitModel.split(baseQuads, parts, transform);
					}
			).getOrDefault(offset, ImmutableList.of());
		} catch(ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	@Override
	public IModelData getModelData(
			@Nonnull ILightReader world,
			@Nonnull BlockPos pos,
			@Nonnull BlockState state,
			@Nonnull IModelData tileData
	)
	{
		IModelData baseData = super.getModelData(world, pos, state, tileData);
		TileEntity te = world.getTileEntity(pos);
		BlockPos offset = null;
		if(te instanceof IModelOffsetProvider)
			offset = ((IModelOffsetProvider)te).getModelOffset(state);
		else if(state.getBlock() instanceof IModelOffsetProvider)
			offset = ((IModelOffsetProvider)state.getBlock()).getModelOffset(state);
		if(offset!=null)
			return new CombinedModelData(
					new SinglePropertyModelData<>(
							offset,
							Model.SUBMODEL_OFFSET
					),
					baseData
			);
		else
			return baseData;
	}
}
