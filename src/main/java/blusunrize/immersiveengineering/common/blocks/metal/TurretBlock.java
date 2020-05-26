package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.tileentity.TileEntityType;

import java.util.function.Supplier;

public class TurretBlock extends GenericTileBlock
{
	public TurretBlock(String name, Supplier<TileEntityType<?>> tileType)
	{
		super(name, tileType, Block.Properties.create(Material.IRON).hardnessAndResistance(3, 15).notSolid(),
				IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		return areAllReplaceable(
				context.getPos(),
				context.getPos().up(),
				context
		);
	}
}
