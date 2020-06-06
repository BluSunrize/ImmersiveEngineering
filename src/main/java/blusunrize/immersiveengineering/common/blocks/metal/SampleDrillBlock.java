package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.common.blocks.generic.GenericTileBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SampleDrillBlock extends GenericTileBlock
{
	public SampleDrillBlock()
	{
		super("sample_drill", () -> SampleDrillTileEntity.TYPE,
				Properties.create(Material.IRON).hardnessAndResistance(3, 15),
				IEProperties.FACING_HORIZONTAL, IEProperties.MULTIBLOCKSLAVE);
		setNotNormalBlock();
		setBlockLayer(BlockRenderLayer.CUTOUT);
	}

	@Override
	public boolean canIEBlockBePlaced(BlockState newState, BlockItemUseContext context)
	{
		BlockPos start = context.getPos();
		World w = context.getWorld();
		return areAllReplaceable(start, start.up(2), context);
	}
}
