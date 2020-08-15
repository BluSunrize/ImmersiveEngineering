package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectorBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.IProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;

public class EnergyMeterBlock extends MiscConnectorBlock
{
	public static final IProperty<Direction> FACING = IEProperties.FACING_HORIZONTAL;
	public static final IProperty<Boolean> DUMMY = IEProperties.MULTIBLOCKSLAVE;

	public EnergyMeterBlock()
	{
		super("current_transformer", () -> EnergyMeterTileEntity.TYPE, DUMMY, FACING, BlockStateProperties.WATERLOGGED);
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
	public BlockState updatePostPlacement(BlockState stateIn, Direction updateSide, BlockState updatedState,
										  IWorld worldIn, BlockPos currentPos, BlockPos updatedPos)
	{
		Direction facing = stateIn.get(FACING);
		boolean dummy = stateIn.get(DUMMY);
		BlockPos otherHalf = currentPos.up(dummy?-1: 1);
		BlockState otherState = worldIn.getBlockState(otherHalf);
		// Check if current facing is correct, else assume facing of partner
		if(otherState.getBlock()==this)
			if(otherState.get(FACING)==facing&&otherState.get(DUMMY)==!dummy)
				return stateIn;
			else
				return stateIn.with(FACING, otherState.get(FACING));
		return Blocks.AIR.getDefaultState();
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
