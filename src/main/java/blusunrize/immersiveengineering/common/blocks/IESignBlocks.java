/*
 * BluSunrize
 * Copyright (c) 2025
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import com.mojang.datafixers.util.Function3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;

import javax.annotation.Nullable;
import java.util.Properties;

public class IESignBlocks
{
	public static record Holder(IESignBlocks.Standing standing)
	{
	}

	public static class Standing extends StandingSignBlock
	{
		public Standing(WoodType type, Properties properties)
		{
			super(type, properties);
		}

		@Nullable
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
		{
			return createTickerHelper(blockEntityType, IEBlockEntities.SIGN.get(), SignBlockEntity::tick);
		}

		public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
		{
			return new IESignBlockEntity(pos, state);
		}
	}

	public static class Wall extends WallSignBlock
	{
		public Wall(WoodType type, Properties properties)
		{
			super(type, properties);
		}

		@Nullable
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
		{
			return createTickerHelper(blockEntityType, IEBlockEntities.SIGN.get(), SignBlockEntity::tick);
		}

		public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
		{
			return new IESignBlockEntity(pos, state);
		}
	}

	public static class Hanging extends CeilingHangingSignBlock
	{
		public Hanging(WoodType type, Properties properties)
		{
			super(type, properties);
		}

		@Nullable
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
		{
			return createTickerHelper(blockEntityType, IEBlockEntities.HANGING_SIGN.get(), SignBlockEntity::tick);
		}

		public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
		{
			return new IEHangingSignBlockEntity(pos, state);
		}
	}

	public static class WallHanging extends WallHangingSignBlock
	{
		public WallHanging(WoodType type, Properties properties)
		{
			super(type, properties);
		}

		@Nullable
		public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType)
		{
			return createTickerHelper(blockEntityType, IEBlockEntities.HANGING_SIGN.get(), SignBlockEntity::tick);
		}

		public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
		{
			return new IEHangingSignBlockEntity(pos, state);
		}
	}
}
