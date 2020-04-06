package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;

public class TransformerBlock extends MiscConnectorBlock
{
	public TransformerBlock()
	{
		super("transformer", () -> TransformerTileEntity.TYPE,
				ImmutableList.of(IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE, IEProperties.MIRRORED),
				ImmutableList.of(), TransformerItemBlock.class);
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
}
