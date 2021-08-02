package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectableBlock;
import blusunrize.immersiveengineering.common.register.IETileTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RazorWireBlock extends MiscConnectableBlock<RazorWireTileEntity>
{
	public RazorWireBlock(Properties props)
	{
		super(props, IETileTypes.RAZOR_WIRE);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_HORIZONTAL, BlockStateProperties.WATERLOGGED);
	}
}
