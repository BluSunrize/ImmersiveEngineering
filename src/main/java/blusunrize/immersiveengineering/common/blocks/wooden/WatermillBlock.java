/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.phys.shapes.CollisionContext;

public class WatermillBlock extends GenericTileBlock<WatermillTileEntity>
{
	public WatermillBlock(Properties props)
	{
		super(IETileTypes.WATERMILL, props);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL);
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockPlaceContext context)
	{
		BlockPos center = context.getClickedPos();
		Level world = context.getLevel();
		Direction facing = context.getHorizontalDirection();
		Player player = context.getPlayer();
		CollisionContext selectionCtx = player==null?CollisionContext.empty(): CollisionContext.of(player);
		BlockState stateToPlace = defaultBlockState();
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if(((hh > -2&&hh < 2)||(ww > -2&&ww < 2))&&(hh!=0||ww!=0))
				{
					BlockPos pos2 = center.offset(facing.getAxis()==Axis.Z?ww: 0, hh, facing.getAxis()==Axis.Z?0: ww);
					BlockState state = world.getBlockState(pos2);
					if(!state.canBeReplaced(BlockPlaceContext.at(context, pos2, facing))||
							!world.isUnobstructed(stateToPlace, pos2, selectionCtx))
						return false;
				}
		return true;
	}
}
