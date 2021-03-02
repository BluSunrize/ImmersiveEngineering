package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.BiFunction;

public class HorizontalFacingBlock<T extends TileEntity> extends GenericTileBlock<T>
{
	public HorizontalFacingBlock(String name, RegistryObject<TileEntityType<T>> tileType, Properties blockProps)
	{
		super(name, tileType, blockProps);
	}

	public HorizontalFacingBlock(String name, RegistryObject<TileEntityType<T>> tileType, Properties blockProps, BiFunction<Block, Item.Properties, Item> itemBlock)
	{
		super(name, tileType, blockProps, itemBlock);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder)
	{
		super.fillStateContainer(builder);
		builder.add(IEProperties.FACING_HORIZONTAL);
	}
}
