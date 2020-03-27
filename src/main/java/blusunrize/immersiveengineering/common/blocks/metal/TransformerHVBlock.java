package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;

public class TransformerHVBlock extends MiscConnectorBlock
{
	public TransformerHVBlock()
	{
		super("transformer_hv", () -> TransformerHVTileEntity.TYPE,
				IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE, IEProperties.MIRRORED);
		setNotNormalBlock();
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
