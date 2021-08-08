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

public class GenericEntityBlock<T extends BlockEntity> extends IEEntityBlock
{
	private final RegistryObject<BlockEntityType<T>> tileType;

	public GenericEntityBlock(RegistryObject<BlockEntityType<T>> tileType, Properties blockProps)
	{
		super(blockProps);
		this.tileType = tileType;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		return tileType.get().create(pos, state);
	}
}
