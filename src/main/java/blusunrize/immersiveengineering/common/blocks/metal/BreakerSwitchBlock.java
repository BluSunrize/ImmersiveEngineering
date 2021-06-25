package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.MiscConnectableBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class BreakerSwitchBlock<T extends BreakerSwitchTileEntity> extends MiscConnectableBlock<T>
{
	public BreakerSwitchBlock(Properties props, RegistryObject<TileEntityType<T>> tileType)
	{
		super(props, tileType);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.ACTIVE, IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
	}
}
