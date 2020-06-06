package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TeslaCoilBlock extends GenericTileBlock
{
	public TeslaCoilBlock()
	{
		super("tesla_coil", () -> TeslaCoilTileEntity.TYPE,
				Properties.create(Material.IRON).hardnessAndResistance(3, 15),
				IEProperties.FACING_ALL, IEProperties.MULTIBLOCKSLAVE);
		setNotNormalBlock();
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		BlockPos start = context.getPos();
		World w = context.getWorld();
		return areAllReplaceable(start, start.up(1), context);
	}
}
