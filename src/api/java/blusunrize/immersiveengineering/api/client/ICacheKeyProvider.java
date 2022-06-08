/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.client.model.data.IModelData;

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
	default List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull RandomSource pRand, @Nonnull IModelData extraData)
	{
		return getQuads(getKey(pState, pSide, pRand, extraData));
	}

	@Nonnull
	@Override
	default List<BakedQuad> getQuads(@Nullable BlockState pState, @Nullable Direction pSide, @Nonnull RandomSource pRand)
	{
		return getQuads(pState, pSide, pRand, EmptyModelData.INSTANCE);
	}

	@Nullable
	K getKey(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull RandomSource rand,
			@Nonnull IModelData extraData
	);
}
