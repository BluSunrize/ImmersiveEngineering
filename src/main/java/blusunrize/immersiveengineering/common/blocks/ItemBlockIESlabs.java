/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockIESlabs extends ItemBlockIEBase
{
	public ItemBlockIESlabs(Block b)
	{
		super(b);
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag tooltipFlag)
	{
		super.addInformation(stack, world, tooltip, tooltipFlag);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		IBlockState iblockstate = world.getBlockState(pos);
		Block localBlock = iblockstate.getBlock();
		BlockPos posThere = pos;
		BlockPos posOffset = pos.offset(side);

		if(localBlock==Blocks.SNOW_LAYER&&localBlock.isReplaceable(world, pos))
			side = EnumFacing.UP;
		else if(!localBlock.isReplaceable(world, pos))
			pos = pos.offset(side);

		TileEntityIESlab stackSlab = null;
		if(side.getAxis().isVertical()&&this.block.equals(world.getBlockState(posThere).getBlock())&&world.getBlockState(posThere).getBlock().getMetaFromState(world.getBlockState(posThere))==stack.getItemDamage())
		{
			TileEntity te = world.getTileEntity(posThere);
			if(te instanceof TileEntityIESlab&&((TileEntityIESlab)te).slabType+side.ordinal()==1)
				stackSlab = ((TileEntityIESlab)te);
		}
		else if(this.block.equals(world.getBlockState(posOffset).getBlock())&&world.getBlockState(posOffset).getBlock().getMetaFromState(world.getBlockState(posOffset))==stack.getItemDamage())
		{
			TileEntity te = world.getTileEntity(posOffset);
			if(te instanceof TileEntityIESlab)
			{
				int type = ((TileEntityIESlab)te).slabType;
				if((type==0&&(side==EnumFacing.DOWN||hitY >= .5))||(type==1&&(side==EnumFacing.UP||hitY <= .5)))
					stackSlab = ((TileEntityIESlab)te);
			}
		}
		else
			return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
		if(stackSlab!=null)
		{
			stackSlab.slabType = 2;
			stackSlab.markContainingBlockForUpdate(null);
			world.playSound(stackSlab.getPos().getX()+.5, stackSlab.getPos().getY()+.5, stackSlab.getPos().getZ()+.5, this.block.getSoundType().getPlaceSound(), SoundCategory.BLOCKS, (this.block.getSoundType().getVolume()+1.0F)/2.0F, this.block.getSoundType().getPitch()*0.8F, false);
			stack.shrink(1);
			return EnumActionResult.SUCCESS;
		}
		else
			return super.onItemUse(player, world, pos, hand, side, hitX, hitY, hitZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
		if(ret)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity instanceof TileEntityIESlab)
				((TileEntityIESlab)tileEntity).slabType = (side==EnumFacing.DOWN||(side!=EnumFacing.UP&&hitY >= .5))?1: 0;
		}
		return ret;
	}
}