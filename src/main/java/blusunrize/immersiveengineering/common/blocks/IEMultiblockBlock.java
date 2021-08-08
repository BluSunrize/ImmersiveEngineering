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
import blusunrize.immersiveengineering.common.temp.IETickableBlockEntity;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public abstract class IEMultiblockBlock<T extends MultiblockPartBlockEntity<? super T>> extends IEEntityBlock
{
	private final MultiblockBEType<T> entityType;

	public IEMultiblockBlock(Properties props, MultiblockBEType<T> entityType)
	{
		super(props);
		this.entityType = entityType;
		setMobility(PushReaction.BLOCK);
		lightOpacity = 0;
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
	{
		return entityType.create(pos, state);
	}

	@Nullable
	@Override
	public <T2 extends BlockEntity> BlockEntityTicker<T2> getTicker(Level world, BlockState state, BlockEntityType<T2> type)
	{
		return entityType.getTicker(type, IETickableBlockEntity::tickStatic);
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
			BlockEntity tileEntity = world.getBlockEntity(pos);
			if(tileEntity instanceof IEBaseBlockEntity)
				((IEBaseBlockEntity)tileEntity).setOverrideState(state);
			if(tileEntity instanceof MultiblockPartBlockEntity)
			{
				// Remove the BE here before disassembling: The block is already gone, so setting the block state here
				// to a block providing a BE will produce strange results otherwise
				world.removeBlockEntity(pos);
				((MultiblockPartBlockEntity<?>)tileEntity).disassemble();
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
	{
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof MultiblockPartBlockEntity)
			return Utils.getPickBlock(((MultiblockPartBlockEntity<?>)te).getOriginalBlock(), target, player);
		return ItemStack.EMPTY;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
	{
		//Don't add multiblocks to the creative tab/JEI
	}
}