package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FluidPipeBlock extends IETileProviderBlock<FluidPipeTileEntity>
{
	public FluidPipeBlock(Properties blockProps)
	{
		super("fluid_pipe", IETileTypes.FLUID_PIPE, blockProps);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.WATERLOGGED);
	}
}
