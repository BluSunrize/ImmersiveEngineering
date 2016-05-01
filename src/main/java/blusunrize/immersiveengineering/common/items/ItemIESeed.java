package blusunrize.immersiveengineering.common.items;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public class ItemIESeed extends ItemIEBase implements IPlantable
{
    private Block cropBlock;
	public ItemIESeed(Block cropBlock, String... subNames)
	{
		super("seed", 64, subNames);
		this.cropBlock = cropBlock;
	}

	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(side != EnumFacing.UP)
			return false;
		else if (player.canPlayerEdit(pos, side, stack) && player.canPlayerEdit(pos.add(0,1,0), side, stack))
		{
			if(world.getBlockState(pos).getBlock().canSustainPlant(world, pos, EnumFacing.UP, this) && world.isAirBlock(pos.add(0,1,0)))
			{
				world.setBlockState(pos.add(0,1,0), this.cropBlock.getDefaultState());
				--stack.stackSize;
				return true;
			}
			else
				return false;
		}
		else
			return false;
	}

	@Override
	public EnumPlantType getPlantType(IBlockAccess world, BlockPos pos)
	{
		return ((IPlantable)cropBlock).getPlantType(world, pos);
	}

	@Override
	public IBlockState getPlant(IBlockAccess world, BlockPos pos)
	{
		return cropBlock.getDefaultState();
	}
}