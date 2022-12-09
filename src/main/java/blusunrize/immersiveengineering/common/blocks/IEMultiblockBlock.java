/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartBlockEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.CreativeModeTabEvent.DisplayItemsAdapter;

public abstract class IEMultiblockBlock<T extends MultiblockPartBlockEntity<? super T>> extends IEEntityBlock<T>
{
	public IEMultiblockBlock(Properties props, MultiblockBEType<T> entityType)
	{
		super(entityType, props);
		setMobility(PushReaction.BLOCK);
		lightOpacity = 0;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving)
	{
		if(state.getBlock()!=newState.getBlock())
		{
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if(blockEntity instanceof IEBaseBlockEntity ieBaseBE)
				ieBaseBE.setOverrideState(state);
			if(blockEntity instanceof MultiblockPartBlockEntity<?> multiblockBE)
			{
				// Remove the BE here before disassembling: The block is already gone, so setting the block state here
				// to a block providing a BE will produce strange results otherwise
				super.onRemove(state, world, pos, newState, isMoving);
				multiblockBE.disassemble();
				return;
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
	{
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof MultiblockPartBlockEntity)
			return Utils.getPickBlock(((MultiblockPartBlockEntity<?>)te).getOriginalBlock(), target, player);
		return ItemStack.EMPTY;
	}

	@Override
	public DisplayItemsAdapter getCreativeTabFiller()
	{
		return (enabledFlags, populator, hasPermissions) -> {};
	}
}