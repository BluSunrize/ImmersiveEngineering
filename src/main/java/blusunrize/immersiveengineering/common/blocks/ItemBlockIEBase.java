/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientProxy;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class ItemBlockIEBase extends ItemBlock
{
	private int[] burnTime;

	public ItemBlockIEBase(Block b)
	{
		super(b);
		if(((BlockIEBase)b).enumValues.length > 1)
			setHasSubtypes(true);
		this.burnTime = new int[((BlockIEBase)b).enumValues!=null?((BlockIEBase)b).enumValues.length: 1];
	}

	@Override
	public int getMetadata(int damageValue)
	{
		return damageValue;
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> itemList)
	{
		if(this.isInCreativeTab(tab))
			this.block.getSubBlocks(tab, itemList);
	}

	@Override
	public String getTranslationKey(ItemStack stack)
	{
		return ((BlockIEBase)this.block).getTranslationKey(stack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack)
	{
		return ClientProxy.itemFont;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag tooltipFlag)
	{
		if(((BlockIEBase)block).hasFlavour(stack))
		{
			String subName = ((BlockIEBase)this.block).getStateFromMeta(stack.getItemDamage()).getValue(((BlockIEBase)this.block).property).toString().toLowerCase(Locale.US);
			String flavourKey = Lib.DESC_FLAVOUR+((BlockIEBase)this.block).name+"."+subName;
			tooltip.add(TextFormatting.GRAY.toString()+I18n.format(flavourKey));
		}
		super.addInformation(stack, world, tooltip, tooltipFlag);
		if(ItemNBTHelper.hasKey(stack, "energyStorage"))
			tooltip.add(I18n.format("desc.immersiveengineering.info.energyStored", ItemNBTHelper.getInt(stack, "energyStorage")));
		if(ItemNBTHelper.hasKey(stack, "tank"))
		{
			FluidStack fs = FluidStack.loadFluidStackFromNBT(ItemNBTHelper.getTagCompound(stack, "tank"));
			if(fs!=null)
				tooltip.add(fs.getLocalizedName()+": "+fs.amount+"mB");
		}
	}


	public ItemBlockIEBase setBurnTime(int meta, int burnTime)
	{
		if(meta >= 0&&meta < this.burnTime.length)
			this.burnTime[meta] = burnTime;
		return this;
	}

	@Override
	public int getItemBurnTime(ItemStack itemStack)
	{
		return this.burnTime[Math.max(0, Math.min(itemStack.getMetadata(), this.burnTime.length-1))];
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, IBlockState newState)
	{
		if(!((BlockIEBase)this.block).canIEBlockBePlaced(world, pos, newState, side, hitX, hitY, hitZ, player, stack))
			return false;
		boolean ret = super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
		if(ret)
		{
			((BlockIEBase)this.block).onIEBlockPlacedBy(world, pos, newState, side, hitX, hitY, hitZ, player, stack);
		}
		return ret;
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		IBlockState iblockstate = world.getBlockState(pos);
		Block block = iblockstate.getBlock();
		if(!block.isReplaceable(world, pos))
			pos = pos.offset(side);
		if(stack.getCount() > 0&&player.canPlayerEdit(pos, side, stack)&&canBlockBePlaced(world, pos, side, stack))
		{
			int i = this.getMetadata(stack.getMetadata());
			IBlockState iblockstate1 = this.block.getStateForPlacement(world, pos, side, hitX, hitY, hitZ, i, player);
			if(placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, iblockstate1))
			{
				SoundType soundtype = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
				world.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume()+1.0F)/2.0F, soundtype.getPitch()*0.8F);
				if(!player.capabilities.isCreativeMode)
					stack.shrink(1);
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.FAIL;
	}

	@Override
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack)
	{
		Block block = worldIn.getBlockState(pos).getBlock();

		if(block==Blocks.SNOW_LAYER&&block.isReplaceable(worldIn, pos))
		{
			side = EnumFacing.UP;
		}
		else if(!block.isReplaceable(worldIn, pos))
		{
			pos = pos.offset(side);
		}

		return canBlockBePlaced(worldIn, pos, side, stack);
	}

	private boolean canBlockBePlaced(World w, BlockPos pos, EnumFacing side, ItemStack stack)
	{
		BlockIEBase blockIn = (BlockIEBase)this.block;
		Block block = w.getBlockState(pos).getBlock();
		AxisAlignedBB axisalignedbb = blockIn.getCollisionBoundingBox(blockIn.getStateFromMeta(stack.getItemDamage()), w, pos);
		if(axisalignedbb!=null&&!w.checkNoEntityCollision(axisalignedbb.offset(pos), null)) return false;
		return block.isReplaceable(w, pos)&&blockIn.canPlaceBlockOnSide(w, pos, side);
	}

	public static class ItemBlockIENoInventory extends ItemBlockIEBase
	{
		public ItemBlockIENoInventory(Block b)
		{
			super(b);
		}

		@Nullable
		@Override
		public NBTTagCompound getNBTShareTag(ItemStack stack)
		{
			NBTTagCompound ret = super.getNBTShareTag(stack);
			if(ret!=null)
			{
				ret = ret.copy();
				ret.removeTag("inventory");
			}
			return ret;
		}

		@Override
		public boolean getShareTag()
		{
			return super.getShareTag();
		}
	}
}