/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WatermillBlock extends IETileProviderBlock
{
	public WatermillBlock(String name)
	{
		super(name, Block.Properties.create(Material.WOOD).sound(SoundType.WOOD).hardnessAndResistance(2, 5).notSolid(),
				BlockItemIE::new, IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		return new WatermillTileEntity();
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		BlockPos center = context.getPos();
		World world = context.getWorld();
		Direction facing = context.getPlacementHorizontalFacing();
		PlayerEntity player = context.getPlayer();
		ISelectionContext selectionCtx = player==null?ISelectionContext.dummy(): ISelectionContext.forEntity(player);
		BlockState stateToPlace = getDefaultState();
		for(int hh = -2; hh <= 2; hh++)
			for(int ww = -2; ww <= 2; ww++)
				if(((hh > -2&&hh < 2)||(ww > -2&&ww < 2))&&(hh!=0||ww!=0))
				{
					BlockPos pos2 = center.add(facing.getAxis()==Axis.Z?ww: 0, hh, facing.getAxis()==Axis.Z?0: ww);
					BlockState state = world.getBlockState(pos2);
					if(!state.isReplaceable(BlockItemUseContext.func_221536_a(context, pos2, facing))||
							!world.placedBlockCollides(stateToPlace, pos2, selectionCtx))
						return false;
				}
		return true;
	}
}
