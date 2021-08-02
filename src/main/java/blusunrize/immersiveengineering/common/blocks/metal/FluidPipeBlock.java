package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class FluidPipeBlock extends GenericTileBlock<FluidPipeTileEntity>
{
	public FluidPipeBlock(Properties blockProps)
	{
		super(IETileTypes.FLUID_PIPE, blockProps);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(BlockStateProperties.WATERLOGGED);
	}
}
