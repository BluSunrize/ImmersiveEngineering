/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.common.blocks.IEEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fmllegacy.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.BiFunction;

public class GenericEntityBlock<T extends BlockEntity> extends IEEntityBlock
{
	private final BiFunction<BlockPos, BlockState, T> makeTile;

	public GenericEntityBlock(BiFunction<BlockPos, BlockState, T> makeTile, Properties blockProps)
	{
		super(blockProps);
		this.makeTile = makeTile;
	}

	public GenericEntityBlock(RegistryObject<BlockEntityType<T>> tileType, Properties blockProps)
	{
		this((bp, state) -> tileType.get().create(bp, state), blockProps);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		return makeTile.apply(pos, state);
	}
}
