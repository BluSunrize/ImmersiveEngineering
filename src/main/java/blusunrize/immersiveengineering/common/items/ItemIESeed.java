/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
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
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		if(side!=EnumFacing.UP)
			return EnumActionResult.PASS;
		else if(player.canPlayerEdit(pos, side, stack)&&player.canPlayerEdit(pos.add(0, 1, 0), side, stack))
		{
			IBlockState state = world.getBlockState(pos);
			if(state.getBlock().canSustainPlant(state, world, pos, EnumFacing.UP, this)&&world.isAirBlock(pos.add(0, 1, 0)))
			{
				world.setBlockState(pos.add(0, 1, 0), this.cropBlock.getDefaultState());
				stack.shrink(1);
				return EnumActionResult.SUCCESS;
			}
			else
				return EnumActionResult.PASS;
		}
		else
			return EnumActionResult.PASS;
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