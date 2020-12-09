/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IModelTransform;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BakedDynamicSplitModel<K, T extends ICacheKeyProvider<K> & IBakedModel> extends AbstractSplitModel<T>
{
	private final Set<Vector3i> parts;
	private final IModelTransform transform;
	private final Cache<K, Map<Vector3i, List<BakedQuad>>> subModelCache = CacheBuilder.newBuilder()
			.maximumSize(10)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build();

	public BakedDynamicSplitModel(T base, Set<Vector3i> parts, IModelTransform transform, Vector3i size)
	{
		super(base, size);
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
						return split(baseQuads, parts, transform);
					}
			).getOrDefault(offset, ImmutableList.of());
		} catch(ExecutionException e)
		{
			throw new RuntimeException(e);
		}
	}
}
