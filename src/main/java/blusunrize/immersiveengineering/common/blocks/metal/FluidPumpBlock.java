package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluidPumpBlock extends GenericTileBlock
{
	public FluidPumpBlock()
	{
		super("fluid_pump", () -> FluidPumpTileEntity.TYPE,
				Properties.create(Material.IRON).hardnessAndResistance(3, 15).notSolid(),
				IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		BlockPos start = context.getPos();
		World w = context.getWorld();
		return areAllReplaceable(start, start.up(1), context);
	}
}
