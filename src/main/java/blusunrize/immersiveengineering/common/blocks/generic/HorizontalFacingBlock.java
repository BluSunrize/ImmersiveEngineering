package blusunrize.immersiveengineering.common.blocks.generic;

import blusunrize.immersiveengineering.api.IEProperties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraftforge.fmllegacy.RegistryObject;

public class HorizontalFacingBlock<T extends BlockEntity> extends GenericTileBlock<T>
{
	public HorizontalFacingBlock(RegistryObject<BlockEntityType<T>> tileType, Properties blockProps)
	{
		super(tileType, blockProps);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder)
	{
		super.createBlockStateDefinition(builder);
		builder.add(IEProperties.FACING_HORIZONTAL);
	}
}
