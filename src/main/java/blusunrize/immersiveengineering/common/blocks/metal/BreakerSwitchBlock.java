package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.fmllegacy.RegistryObject;

public class BreakerSwitchBlock<T extends BreakerSwitchBlockEntity> extends ConnectorBlock<T>
{
	public BreakerSwitchBlock(Properties props, RegistryObject<BlockEntityType<T>> tileType)
	{
		super(props, tileType);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.ACTIVE, IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
	}
}
