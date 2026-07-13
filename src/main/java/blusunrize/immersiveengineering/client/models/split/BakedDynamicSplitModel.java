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
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

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

	public BakedDynamicSplitModel(T base, Set<Vec3i> parts, ModelState transform, @Nonnull Vec3i size)
	{
		super(base, size);
		this.subModelCache = CacheBuilder.newBuilder()
				.maximumSize(64)
				.expireAfterAccess(10, TimeUnit.MINUTES)
				.build(CacheLoader.from(key -> {
					List<BakedQuad> baseQuads = base.getQuads(key);
					return split(baseQuads, parts, transform);
				}));
		// Register with the reload cache so resource pack reloads actually clear data.
		// Cache size increased since the old settings caused constant re-splitting.
		WEAK_INSTANCES.add(this);
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType)
	{
		BlockPos offset = data.get(Model.SUBMODEL_OFFSET);
		if(offset==null)
			return super.getQuads(state, side, rand, data, renderType);
		if(side!=null)
			return ImmutableList.of();
		K key = base.getKey(state, null, rand, data, renderType);
		if(key==null)
			return ImmutableList.of();
		return subModelCache.getUnchecked(key).getOrDefault(offset, ImmutableList.of());
	}
}
