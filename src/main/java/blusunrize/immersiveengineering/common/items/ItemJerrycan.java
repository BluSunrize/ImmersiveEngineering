package blusunrize.immersiveengineering.common.items;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

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
			EnumChatFormatting rarity = fs.getFluid().getRarity()==EnumRarity.common?EnumChatFormatting.GRAY:fs.getFluid().getRarity().rarityColor;
			list.add(rarity+fs.getLocalizedName()+EnumChatFormatting.GRAY+": "+fs.amount+"/"+getCapacity(stack)+"mB");
		}
		else
			list.add(StatCollector.translateToLocal("desc.ImmersiveEngineering.flavour.drill.empty"));
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