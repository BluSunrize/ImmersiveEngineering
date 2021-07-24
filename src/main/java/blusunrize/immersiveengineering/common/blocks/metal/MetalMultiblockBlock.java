/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IEMultiblockBlock;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class MetalMultiblockBlock<T extends MultiblockPartTileEntity<T>> extends IEMultiblockBlock
{
	private final Supplier<BlockEntityType<T>> tileType;

	public MetalMultiblockBlock(Supplier<BlockEntityType<T>> te, Properties props)
	{
		super(props);
		tileType = te;
		lightOpacity = 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.MIRRORED);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state)
	{
		return tileType.get().create(pos, state);
	}
}
