package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.IETileProviderBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraftforge.fml.RegistryObject;

import java.util.function.BiFunction;

public class HorizontalFacingBlock<T extends BlockEntity> extends IETileProviderBlock<T>
{
	public HorizontalFacingBlock(String name, RegistryObject<BlockEntityType<T>> tileType, Properties blockProps)
	{
		super(name, tileType, blockProps);
	}

	public HorizontalFacingBlock(String name, RegistryObject<BlockEntityType<T>> tileType, Properties blockProps, BiFunction<Block, Item.Properties, Item> itemBlock)
	{
		super(name, tileType, blockProps, itemBlock);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_HORIZONTAL);
	}
}
