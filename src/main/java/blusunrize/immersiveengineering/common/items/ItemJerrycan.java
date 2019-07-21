/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class ItemJerrycan extends ItemIEBase
{
	public ItemJerrycan()
	{
		super("jerrycan", 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
	{
		FluidStack fs = FluidUtil.getFluidContained(stack);
		if(fs!=null)
		{
			TextFormatting rarity = fs.getFluid().getRarity()==EnumRarity.COMMON?TextFormatting.GRAY: fs.getFluid().getRarity().color;
			list.add(rarity+fs.getLocalizedName()+TextFormatting.GRAY+": "+fs.amount+"/"+10000+"mB");
		}
		else
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.empty"));
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack stack = player.getHeldItem(hand);
		TileEntity tileEntity = world.getTileEntity(pos);
		if(tileEntity==null||!tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null))
		{
			FluidStack fs = FluidUtil.getFluidContained(stack);
			if(Utils.placeFluidBlock(world, pos.offset(side), fs))
			{
				if(fs.amount <= 0)
					fs = null;
				ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
				return EnumActionResult.SUCCESS;
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain")||FluidUtil.getFluidContained(stack)!=null;
	}

	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		ItemStack compare = stack;
		if(stack.isEmpty())
		{
			stack.grow(1);
			compare = stack.copy();
			stack.shrink(1);
		}
		if(ItemNBTHelper.hasKey(compare, "jerrycanDrain"))
		{
			ItemStack ret = compare.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret);
			handler.drain(ItemNBTHelper.getInt(ret, "jerrycanDrain"), true);
			ItemNBTHelper.remove(ret, "jerrycanDrain");
			return ret;
		}
		else if(FluidUtil.getFluidContained(compare)!=null)
		{
			ItemStack ret = compare.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret);
			handler.drain(1000, true);
			return ret;
		}
		return stack;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		if(!stack.isEmpty())
			return new FluidHandlerItemStack(stack, 10000);
		return null;
	}
}