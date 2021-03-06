package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;

public class FluidPipeBlock extends GenericTileBlock<FluidPipeTileEntity>
{
	public FluidPipeBlock(Properties blockProps)
	{
		super("fluid_pipe", IETileTypes.FLUID_PIPE, blockProps);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(BlockStateProperties.WATERLOGGED);
	}
}
