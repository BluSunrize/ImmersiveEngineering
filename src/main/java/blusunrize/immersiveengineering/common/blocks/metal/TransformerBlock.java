/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TransformerBlock extends MiscConnectorBlock
{
	public TransformerBlock()
	{
		super("transformer", () -> TransformerTileEntity.TYPE,
				ImmutableList.of(IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE, IEProperties.MIRRORED, BlockStateProperties.WATERLOGGED),
				TransformerBlockItem::new);
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		return areAllReplaceable(
				context.getPos(),
				context.getPos().up(2),
				context
		);
	}

	@Nullable
	@Override
	public TileEntity createTileEntity(@Nonnull BlockState state, @Nonnull IBlockReader world)
	{
		TransformerTileEntity te = new TransformerTileEntity();
		te.dummy = state.get(IEProperties.MULTIBLOCKSLAVE)?1: 0;
		return te;
	}
}
