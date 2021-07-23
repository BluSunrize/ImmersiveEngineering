/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.client;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Implement in IBakedModel when using dynamic split models. Models with equivalent cache keys will only be queried
 * and split once
 */
public interface ICacheKeyProvider<K> extends BakedModel
{
	@Nullable
	K getKey(
			@Nullable BlockState state,
			@Nullable Direction side,
			@Nonnull Random rand,
			@Nonnull IModelData extraData
	);
}
