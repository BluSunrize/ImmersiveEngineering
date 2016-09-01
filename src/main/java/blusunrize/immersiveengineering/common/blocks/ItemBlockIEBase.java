package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.Locale;

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
		return ((BlockIEBase) this.block).getUnlocalizedName(stack);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List<String> list, boolean advInfo)
	{
		if(((BlockIEBase)block).hasFlavour(stack))
		{
			String subName = ((BlockIEBase)this.block).getStateFromMeta(stack.getItemDamage()).getValue(((BlockIEBase)this.block).property).toString().toLowerCase(Locale.US);
			String flavourKey = Lib.DESC_FLAVOUR+((BlockIEBase)this.block).name+"."+subName;
			list.add(TextFormatting.GRAY.toString()+ I18n.format(flavourKey));
		}
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			list.add(I18n.format("desc.ImmersiveEngineering.info.energyStored", ItemNBTHelper.getInt(stack, "energyStorage")));
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
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		IBlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();
		if (!block.isReplaceable(world, pos))
		{
			pos = pos.offset(side);
			iblockstate = world.getBlockState(pos);
			block = iblockstate.getBlock();
		}
		if (stack.stackSize>0&&player.canPlayerEdit(pos, side, stack))
		{
			if(!world.isRemote && canBlockBePlaced(world, pos, side, stack))
			{
				int i = this.getMetadata(stack.getMetadata());
				IBlockState iblockstate1 = this.block.onBlockPlaced(world, pos, side, hitX, hitY, hitZ, i, player);

				if (placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, iblockstate1))
				{
					SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
					world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					if(!player.capabilities.isCreativeMode)
						--stack.stackSize;
				}
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.FAIL;
	}
	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
	{
        Block block = worldIn.getBlockState(pos).getBlock();

        if (block == Blocks.SNOW_LAYER && block.isReplaceable(worldIn, pos))
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
		AxisAlignedBB axisalignedbb = blockIn.getCollisionBoundingBox( blockIn.getStateFromMeta(stack.getItemDamage()), w, pos);
		if (axisalignedbb != null && !w.checkNoEntityCollision(axisalignedbb, null)) return false;
		return block.isReplaceable(w, pos) && blockIn.canReplace(w, pos, side, stack);
	}
}