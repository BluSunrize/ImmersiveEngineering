package blusunrize.immersiveengineering.common.blocks.wooden;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.IETileTypes;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer.Builder;

public class ItemBatcherBlock extends GenericTileBlock<ItemBatcherTileEntity>
{
	public ItemBatcherBlock(Properties blockProps)
	{
		super("item_batcher", IETileTypes.ITEM_BATCHER, blockProps);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.FACING_ALL);
	}
}
