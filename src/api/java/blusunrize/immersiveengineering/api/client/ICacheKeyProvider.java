/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * Implement in IBakedModel when using dynamic split models. Models with equivalent cache keys will only be queried
 * and split once
 */
public interface ICacheKeyProvider<K> extends BakedModel
{
	List<BakedQuad> getQuads(K key);

	@Nonnull
	@Override
	default List<BakedQuad> getQuads(
			@Nullable BlockState pState,
			@Nullable Direction pSide,
			@Nonnull RandomSource pRand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	)
	{
		return getQuads(getKey(pState, pSide, pRand, extraData, layer));
	}

	@Nonnull
	@Override
	default List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull RandomSource pRand)
	{
		return getQuads(pState, pSide, pRand, ModelData.EMPTY, null);
	}

	@Nullable
	K getKey(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull ModelData extraData,
			@Nullable RenderType layer
	);
}
