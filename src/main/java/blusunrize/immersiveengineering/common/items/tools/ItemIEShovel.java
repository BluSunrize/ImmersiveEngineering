package blusunrize.immersiveengineering.common.items.tools;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * @author BluSunrize - 08.07.2018
 */
public class ItemIEShovel extends ItemToolBase
{
	public ItemIEShovel(ToolMaterial materialIn, String name, String toolclass, String oreDict)
	{
		super(materialIn, name, toolclass, oreDict, SHOVEL_EFFECTIVE, 1.5f, -3.0f);
	}

	@Override
	public boolean canHarvestBlock(IBlockState blockIn)
	{
		Block block = blockIn.getBlock();
		return block==Blocks.SNOW_LAYER||block==Blocks.SNOW;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		ItemStack itemstack = player.getHeldItem(hand);

		if(!player.canPlayerEdit(pos.offset(facing), facing, itemstack))
			return EnumActionResult.FAIL;
		else
		{
			IBlockState iblockstate = worldIn.getBlockState(pos);
			Block block = iblockstate.getBlock();

			if(facing!=EnumFacing.DOWN&&worldIn.getBlockState(pos.up()).getMaterial()==Material.AIR&&block==Blocks.GRASS)
			{
				IBlockState iblockstate1 = Blocks.GRASS_PATH.getDefaultState();
				worldIn.playSound(player, pos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0F, 1.0F);

				if(!worldIn.isRemote)
				{
					worldIn.setBlockState(pos, iblockstate1, 11);
					itemstack.damageItem(1, player);
				}

				return EnumActionResult.SUCCESS;
			}
			else
				return EnumActionResult.PASS;
		}
	}
}
