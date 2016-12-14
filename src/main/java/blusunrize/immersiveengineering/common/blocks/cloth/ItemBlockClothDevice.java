package blusunrize.immersiveengineering.common.blocks.cloth;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.ItemBlockIEBase;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemBlockClothDevice extends ItemBlockIEBase
{

	public ItemBlockClothDevice(Block b)
	{
		super(b);
	}
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
	{
		if(itemStackIn.getMetadata()!=BlockTypes_ClothDevice.BALLOON.getMeta())
			return super.onItemRightClick(itemStackIn, worldIn, playerIn, hand);
		if(playerIn.isSneaking())
			increaseOffset(itemStackIn);
		else
		{
			Vec3d pos = playerIn.getPositionVector().addVector(0, playerIn.getEyeHeight(), 0).add(playerIn.getLookVec());
			BlockPos bPos = new BlockPos(pos);
			NBTTagCompound nbt = itemStackIn.getTagCompound();
			int offset = nbt==null?0:nbt.getByte("offset");
			bPos = bPos.up(offset);
			if (worldIn.isAirBlock(bPos))
			{
				if(!worldIn.isRemote)
				{
					worldIn.setBlockState(bPos, IEContent.blockClothDevice.getStateFromMeta(BlockTypes_ClothDevice.BALLOON.getMeta()));
					itemStackIn.stackSize--;
					if(itemStackIn.stackSize <= 0)
						playerIn.setHeldItem(hand, null);
				}
				return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
			}
		}
		return new ActionResult(EnumActionResult.PASS, itemStackIn);
	}
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if(stack.getMetadata()==BlockTypes_ClothDevice.BALLOON.getMeta() && playerIn.isSneaking())
		{
			increaseOffset(stack);
			return EnumActionResult.SUCCESS;
		}
		return super.onItemUse(stack, playerIn, worldIn, pos, hand, side, hitX, hitY, hitZ);
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
