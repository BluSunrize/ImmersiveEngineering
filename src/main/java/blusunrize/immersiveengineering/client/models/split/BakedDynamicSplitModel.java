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
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BakedDynamicSplitModel<K, T extends ICacheKeyProvider<K> & BakedModel> extends AbstractSplitModel<T>
{
	private static final Set<BakedDynamicSplitModel<?, ?>> WEAK_INSTANCES = Collections.newSetFromMap(new WeakHashMap<>());
	static {
		IEApi.renderCacheClearers.add(() -> WEAK_INSTANCES.forEach(m -> m.subModelCache.invalidateAll()));
	}

	private final Set<Vec3i> parts;
	private final ModelState transform;
	private final Cache<K, Map<Vec3i, List<BakedQuad>>> subModelCache = CacheBuilder.newBuilder()
			.maximumSize(10)
			.expireAfterAccess(1, TimeUnit.MINUTES)
			.build();

	public BakedDynamicSplitModel(T base, Set<Vec3i> parts, ModelState transform, Vec3i size)
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
