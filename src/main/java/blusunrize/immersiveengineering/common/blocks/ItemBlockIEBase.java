package blusunrize.immersiveengineering.common.blocks;

import java.util.List;
import java.util.Locale;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class ItemBlockIEBase extends ItemBlock
{
	public ItemBlockIEBase(Block b)
	{
		super(b);
		if(((BlockIEBase)b).enumValues.length>1)
			setHasSubtypes(true);
	}

	@Override
	public int getMetadata (int damageValue)
	{
		return damageValue;
	}
	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> itemList)
	{
		this.block.getSubBlocks(item, tab, itemList);
	}
	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		String subName = ((BlockIEBase)this.block).getStateFromMeta(stack.getItemDamage()).getValue(((BlockIEBase)this.block).property).toString().toLowerCase(Locale.US);
		return super.getUnlocalizedName(stack)+"."+subName;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advInfo)
	{
		if(((BlockIEBase)block).hasFlavour(stack))
		{
			String subName = ((BlockIEBase)this.block).getStateFromMeta(stack.getItemDamage()).getValue(((BlockIEBase)this.block).property).toString().toLowerCase(Locale.US);
			String flavourKey = Lib.DESC_FLAVOUR+((BlockIEBase)this.block).name+"."+subName;
			list.add(EnumChatFormatting.GRAY.toString()+StatCollector.translateToLocal(flavourKey));
		}
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			list.add(StatCollector.translateToLocalFormatted("desc.ImmersiveEngineering.info.energyStored", ItemNBTHelper.getInt(stack, "energyStorage")));
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				list.add(fs.getLocalizedName()+": "+fs.amount+"mB");
		}
		super.addInformation(stack, player, list, advInfo);
	}


	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		if(!((BlockIEBase)this.block).canIEBlockBePlaced(world, pos, newState, side, hitX,hitY,hitZ, player, stack))
			return false;
		boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
		if(ret)
		{
			((BlockIEBase)this.block).onIEBlockPlacedBy(world, pos, newState, side, hitX,hitY,hitZ, player, stack);
		}
		return ret;
	}
	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer playerIn, World world, BlockPos pos, EnumFacing side,
			float hitX, float hitY, float hitZ)
	{
		IBlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();

		if (!block.isReplaceable(world, pos))
		{
			pos = pos.offset(side);
			iblockstate = world.getBlockState(pos);
			block = iblockstate.getBlock();
		}
		if (stack.stackSize>0&&playerIn.canPlayerEdit(pos, side, stack))
		{
			if (!world.isRemote&&canBlockBePlaced(world, pos, side, stack))
			{
				int i = this.getMetadata(stack.getMetadata());
				IBlockState iblockstate1 = this.block.onBlockPlaced(world, pos, side, hitX, hitY, hitZ, i, playerIn);

				if (placeBlockAt(stack, playerIn, world, pos, side, hitX, hitY, hitZ, iblockstate1))
				{
					world.playSoundEffect((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F), this.block.stepSound.getPlaceSound(), (this.block.stepSound.getVolume() + 1.0F) / 2.0F, this.block.stepSound.getFrequency() * 0.8F);
					if (!playerIn.capabilities.isCreativeMode)
						--stack.stackSize;
				}
			}
			return true;
		}
		return false;
	}
	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player,
			ItemStack stack) {
        Block block = worldIn.getBlockState(pos).getBlock();

        if (block == Blocks.snow_layer && block.isReplaceable(worldIn, pos))
        {
            side = EnumFacing.UP;
        }
        else if (!block.isReplaceable(worldIn, pos))
        {
            pos = pos.offset(side);
        }

        return canBlockBePlaced(worldIn, pos, side, stack);
	}
	private boolean canBlockBePlaced(World w, BlockPos pos, EnumFacing side, ItemStack stack)
	{
		BlockIEBase blockIn = (BlockIEBase) this.block;
		Block block = w.getBlockState(pos).getBlock();
		AxisAlignedBB axisalignedbb = blockIn.getCollisionBoundingBox(w, pos, blockIn.getStateFromMeta(stack.getItemDamage()));
		if (axisalignedbb != null && !w.checkNoEntityCollision(axisalignedbb, null)) return false;
		return block.isReplaceable(w, pos) && blockIn.canReplace(w, pos, side, stack);
	}
}