package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

public class HorizontalFacingBlock<T extends TileEntity> extends GenericTileBlock<T>
{
	public HorizontalFacingBlock(RegistryObject<TileEntityType<T>> tileType, Properties blockProps)
	{
		super(tileType, blockProps);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.FACING_HORIZONTAL);
	}
}
