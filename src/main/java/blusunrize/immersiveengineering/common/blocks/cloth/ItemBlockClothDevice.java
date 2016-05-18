package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemBlockClothDevice extends ItemBlockIEBase
{

	public ItemBlockClothDevice(Block b)
	{
		super(b);
	}
	@Override
	public ItemStack onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn)
	{
		if (itemStackIn.getItemDamage()!=BlockTypes_ClothDevice.BALLOON.getMeta())
			return super.onItemRightClick(itemStackIn, worldIn, playerIn);
		if (playerIn.isSneaking())
			increaseOffset(itemStackIn);
		else
		{
			Vec3 pos = playerIn.getPositionVector().addVector(0, playerIn.getEyeHeight(), 0).add(playerIn.getLookVec());
			BlockPos bPos = new BlockPos(pos);
			NBTTagCompound nbt = itemStackIn.getTagCompound();
			int offset = nbt==null?0:nbt.getByte("offset");
			bPos = bPos.up(offset);
			if (worldIn.isAirBlock(bPos))
			{
				if (!worldIn.isRemote)
					worldIn.setBlockState(bPos, IEContent.blockClothDevice.getStateFromMeta(BlockTypes_ClothDevice.BALLOON.getMeta()));
				return itemStackIn.splitStack(1);
			}
		}
		return itemStackIn;
	}
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (playerIn.isSneaking())
		{
			increaseOffset(stack);
			return true;
		}
		return super.onItemUse(stack, playerIn, worldIn, pos, side, hitX, hitY, hitZ);
	}
	@Override
	public String getItemStackDisplayName(ItemStack stack)
	{
		String ret = super.getItemStackDisplayName(stack);
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt!=null&&nbt.getByte("offset")!=0)
			ret+=" (+"+nbt.getByte("offset")+")";
		return ret;
	}
	private void increaseOffset(ItemStack s)
	{
		if (s.getTagCompound()==null)
			s.setTagCompound(new NBTTagCompound());
		NBTTagCompound tag = s.getTagCompound();
		byte offset = tag.getByte("offset");
		tag.setByte("offset", (byte) ((offset+1)%5));
	
	}
}
