package blusunrize.immersiveengineering.common.items;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.ForgeDirection;

public class ItemIESeed extends ItemIEBase implements IPlantable
{
    private Block cropBlock;
	public ItemIESeed(Block cropBlock, String... subNames)
	{
		super("seed", 64, subNames);
		this.cropBlock = cropBlock;
	}

	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (side != 1)
			return false;
		else if (player.canPlayerEdit(x, y, z, side, stack) && player.canPlayerEdit(x, y + 1, z, side, stack))
		{
			if(world.getBlock(x, y, z) instanceof BlockFarmland && world.getBlock(x, y, z).canSustainPlant(world, x, y, z, ForgeDirection.UP, this) && world.isAirBlock(x, y + 1, z))
			{
				world.setBlock(x, y + 1, z, this.cropBlock);
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
	public EnumPlantType getPlantType(IBlockAccess world, int x, int y, int z)
	{
		return ((IPlantable)cropBlock).getPlantType(world, x, y, z);
	}

	@Override
	public Block getPlant(IBlockAccess world, int x, int y, int z)
	{
		return cropBlock;
	}

	@Override
	public int getPlantMetadata(IBlockAccess world, int x, int y, int z)
	{
		return 0;
	}
}