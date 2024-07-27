/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

public class IETrapDoorBlock extends TrapDoorBlock
{
	private boolean lockedByRedstone = false;

	public IETrapDoorBlock(BlockSetType blockSetType, Properties properties)
	{
		super(blockSetType, properties);
	}

	public IETrapDoorBlock setLockedByRedstone()
	{
		this.lockedByRedstone = true;
		return this;
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context)
	{
		BlockState state = super.getStateForPlacement(context);
		if(state==null)
			return null;
		//doors locked by redstone can never start open
		if(this.lockedByRedstone)
			return state.setValue(OPEN, false);
		return state;
	}

	@Override
	public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
	{
		if(this.lockedByRedstone&&blockState.getValue(POWERED))
		{
			level.playSound(player, pos, SoundEvents.ZOMBIE_ATTACK_IRON_DOOR, SoundSource.BLOCKS, 0.25F, level.getRandom().nextFloat()*0.1F+0.9F);
			return InteractionResult.CONSUME_PARTIAL;
		}
		return super.use(blockState, level, pos, player, hand, hitResult);
	}

	@Override
	public void neighborChanged(BlockState blockState, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving)
	{
		if(this.lockedByRedstone)
		{
			boolean flag = level.hasNeighborSignal(pos);
			if(!this.defaultBlockState().is(block)&&flag!=blockState.getValue(POWERED))
				level.setBlock(pos, blockState.setValue(POWERED, flag), 2);
		}
		else
			super.neighborChanged(blockState, level, pos, block, fromPos, isMoving);
	}
}
