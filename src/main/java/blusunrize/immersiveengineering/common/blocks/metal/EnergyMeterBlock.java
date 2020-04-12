package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

public class EnergyMeterBlock extends MiscConnectorBlock
{
	public static final IProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;
	public static final IProperty<Boolean> DUMMY = IEProperties.MULTIBLOCKSLAVE;

	public EnergyMeterBlock()
	{
		super("current_transformer", () -> EnergyMeterTileEntity.TYPE, DUMMY, FACING);
		setNotNormalBlock();
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot)
	{
		Direction newFacing = rot.rotate(state.get(FACING));
		return state.with(FACING, newFacing);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn)
	{
		if(mirrorIn==Mirror.NONE)
			return state;
		Direction oldFacing = state.get(FACING);
		Direction newFacing = mirrorIn.mirror(oldFacing);
		return state.with(FACING, newFacing);
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
