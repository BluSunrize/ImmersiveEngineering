/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.client.ICacheKeyProvider;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class BakedDynamicSplitModel<K, T extends ICacheKeyProvider<K> & BakedModel> extends AbstractSplitModel<T>
{
	private static final Set<BakedDynamicSplitModel<?, ?>> WEAK_INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());
	static {
		IEApi.renderCacheClearers.add(() -> WEAK_INSTANCES.forEach(m -> m.subModelCache.invalidateAll()));
	}

	private final LoadingCache<K, Map<Vec3i, List<BakedQuad>>> subModelCache;

	public BakedDynamicSplitModel(T base, Set<Vec3i> parts, ModelState transform, Vec3i size)
	{
		super(base, size);
		this.subModelCache = CacheBuilder.newBuilder()
				.maximumSize(10)
				.expireAfterAccess(1, TimeUnit.MINUTES)
				.build(CacheLoader.from(key -> {
					List<BakedQuad> baseQuads = base.getQuads(key);
					return split(baseQuads, parts, transform);
				}));
	}

	@Nonnull
	@Override
	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull RandomSource rand, @Nonnull IModelData extraData)
	{
		BlockPos offset = extraData.getData(Model.SUBMODEL_OFFSET);
		if(offset==null)
			return super.getQuads(state, side, rand, extraData);
		K key = base.getKey(state, side, rand, extraData);
		if(key==null)
			return ImmutableList.of();
		return subModelCache.getUnchecked(key).getOrDefault(offset, ImmutableList.of());
	}
}
