package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.resources.I18n;
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
import net.minecraftforge.fluids.FluidActionResult;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class ItemJerrycan extends ItemIEBase
{
	public ItemJerrycan()
	{
		super("jerrycan", 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		FluidStack fs = FluidUtil.getFluidContained(stack);
		if(fs!=null)
		{
			TextFormatting rarity = fs.getFluid().getRarity()==EnumRarity.COMMON? TextFormatting.GRAY:fs.getFluid().getRarity().rarityColor;
			list.add(rarity+fs.getLocalizedName()+ TextFormatting.GRAY+": "+fs.amount+"/"+10000+"mB");
		}
		else
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.empty"));
	}

	@Override
	public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if(!world.isRemote)
		{
			ItemStack stack = player.getHeldItem(hand);
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity!=null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,null))
			{
				FluidActionResult fluidActionResult = FluidUtil.interactWithFluidHandler(stack, tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null), player);
				if(fluidActionResult.isSuccess()) {
					player.setHeldItem(hand, fluidActionResult.getResult());
					return EnumActionResult.SUCCESS;
				}
				return EnumActionResult.FAIL;
			}
			else
			{
				FluidStack fs = FluidUtil.getFluidContained(stack);
				if(Utils.placeFluidBlock(world, pos.offset(side), fs))
				{
					if(fs.amount<=0)
						fs = null;
					ItemNBTHelper.setFluidStack(stack, "Fluid", fs);
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.PASS;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain") || FluidUtil.getFluidContained(stack)!=null;
	}
	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "jerrycanDrain"))
		{
			ItemStack ret = stack.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret);
			handler.drain(ItemNBTHelper.getInt(ret, "jerrycanDrain"), true);
			ItemNBTHelper.remove(ret, "jerrycanDrain");
			return ret;
		}
		else if(FluidUtil.getFluidContained(stack)!=null)
		{
			ItemStack ret = stack.copy();
			IFluidHandler handler = FluidUtil.getFluidHandler(ret);
			handler.drain(1000, true);
			return ret;
		}
		return stack;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt)
	{
		return new FluidHandlerItemStack(stack, 10000);
	}
}