package blusunrize.immersiveengineering.common.items;

import java.util.List;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ItemJerrycan extends ItemIEBase implements IFluidContainerItem
{
	public ItemJerrycan()
	{
		super("jerrycan", 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv)
	{
		FluidStack fs = getFluid(stack);
		if(fs!=null)
		{
			TextFormatting rarity = fs.getFluid().getRarity()==EnumRarity.COMMON? TextFormatting.GRAY:fs.getFluid().getRarity().rarityColor;
			list.add(rarity+fs.getLocalizedName()+ TextFormatting.GRAY+": "+fs.amount+"/"+getCapacity(stack)+"mB");
		}
		else
			list.add(I18n.format(Lib.DESC_FLAVOUR+"drill.empty"));
	}

	@Override
	public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(pos);
			if(tileEntity!=null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,null))
				return FluidUtil.tryEmptyContainerAndStow(stack,tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,null),new InvWrapper(player.inventory),getFluid(stack).amount,player)?EnumActionResult.SUCCESS:EnumActionResult.FAIL;
//						Utils.fillFluidHandlerWithPlayerItem(world, tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,null), player, hand, stack)?EnumActionResult.SUCCESS:EnumActionResult.FAIL;
			else
			{
				FluidStack fs = this.getFluid(stack);
				if(Utils.placeFluidBlock(world, pos.offset(side), fs))
				{
					if(fs.amount<=0)
						fs = null;
					ItemNBTHelper.setFluidStack(stack, "fluid", fs);
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.FAIL;
	}

	@Override
	public boolean hasContainerItem(ItemStack stack)
	{
		return ItemNBTHelper.hasKey(stack, "jerrycanDrain");
	}
	@Override
	public ItemStack getContainerItem(ItemStack stack)
	{
		if(ItemNBTHelper.hasKey(stack, "jerrycanDrain"))
		{
			ItemStack ret = stack.copy();
			this.drain(ret, ItemNBTHelper.getInt(stack, "jerrycanDrain"), true);
			ItemNBTHelper.remove(ret, "jerrycanDrain");
			return ret;
		}
		return stack;
	}

	@Override
	public FluidStack getFluid(ItemStack container)
	{
		return ItemNBTHelper.getFluidStack(container, "fluid");
	}
	@Override
	public int getCapacity(ItemStack container)
	{
		return 10000;
	}
	@Override
	public int fill(ItemStack container, FluidStack resource, boolean doFill)
	{
		if(resource!=null)
		{
			FluidStack fs = getFluid(container);
			if(fs==null || resource.isFluidEqual(fs))
			{
				int space = fs==null?getCapacity(container): getCapacity(container)-fs.amount;
				int accepted = Math.min(space, resource.amount);
				if(fs==null)
					fs = Utils.copyFluidStackWithAmount(resource, accepted, false);
				else
					fs.amount += accepted;
				if(doFill)
					ItemNBTHelper.setFluidStack(container, "fluid", fs);
				return accepted;
			}
		}
		return 0;
	}
	@Override
	public FluidStack drain(ItemStack container, int maxDrain, boolean doDrain)
	{
		FluidStack fs = getFluid(container);
		if(fs == null)
			return null;
		int drained = Math.min(maxDrain, fs.amount);
		FluidStack stack = new FluidStack(fs, drained);
		if(doDrain)
		{
			fs.amount -= drained;
			if(fs.amount <= 0)
				ItemNBTHelper.remove(container, "fluid");
			else
				ItemNBTHelper.setFluidStack(container, "fluid", fs);
		}
		return stack;
	}
}