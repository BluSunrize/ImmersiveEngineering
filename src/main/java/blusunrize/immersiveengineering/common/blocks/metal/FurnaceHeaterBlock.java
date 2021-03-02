package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer.Builder;

public class FurnaceHeaterBlock extends GenericTileBlock<FurnaceHeaterTileEntity>
{
	public FurnaceHeaterBlock(Properties blockProps)
	{
		super("furnace_heater", IETileTypes.FURNACE_HEATER, blockProps);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.ACTIVE, IEProperties.FACING_ALL);
	}
}
