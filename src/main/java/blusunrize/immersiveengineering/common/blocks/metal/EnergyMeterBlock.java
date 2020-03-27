package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;

public class EnergyMeterBlock extends MiscConnectorBlock
{
	public EnergyMeterBlock()
	{
		super("current_transformer", () -> EnergyMeterTileEntity.TYPE,
				IEProperties.MULTIBLOCKSLAVE, IEProperties.FACING_HORIZONTAL);
		setNotNormalBlock();
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		return areAllReplaceable(
				context.getPos(),
				context.getPos().up(1),
				context
		);
	}
}
