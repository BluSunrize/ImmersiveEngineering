/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MultiblockPartTileEntity;
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

public abstract class IEMultiblockBlock extends IETileProviderBlock
{
	public IEMultiblockBlock(String name, Properties props)
	{
		super(name, props, BlockItemIE::new);
		setMobility(PushReaction.BLOCK);
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
			if(tileEntity instanceof IEBaseTileEntity)
				((IEBaseTileEntity)tileEntity).setOverrideState(state);
			if(tileEntity instanceof MultiblockPartTileEntity)
			{
				// Remove the BE here before disassembling: The block is already gone, so setting the block state here
				// to a block providing a BE will produce strange results otherwise
				super.onRemove(state, world, pos, newState, isMoving);
				((MultiblockPartTileEntity<?>)tileEntity).disassemble();
				return;
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}

	@Override
	public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player)
	{
		BlockEntity te = world.getBlockEntity(pos);
		if(te instanceof MultiblockPartTileEntity)
			return Utils.getPickBlock(((MultiblockPartTileEntity)te).getOriginalBlock(), target, player);
		return ItemStack.EMPTY;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items)
	{
		//Don't add multiblocks to the creative tab/JEI
	}
}