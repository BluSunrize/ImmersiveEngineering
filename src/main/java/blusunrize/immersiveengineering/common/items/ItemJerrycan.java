package blusunrize.immersiveengineering.common.items;

import java.util.HashSet;
import java.util.List;

import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;

public class ItemJerrycan extends ItemIEBase implements IFluidContainerItem
{
	public static HashSet<String> blacklist = new HashSet();
	
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
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if(!world.isRemote)
		{
			TileEntity tileEntity = world.getTileEntity(x, y, z);
			if(tileEntity instanceof IFluidHandler)
				return Utils.fillFluidHandlerWithPlayerItem(world, (IFluidHandler)tileEntity, player);
			else
			{
				x += side==4?-1:side==5?1:0;
				y += side==0?-1:side==1?1:0;
				z += side==2?-1:side==3?1:0;
				FluidStack fs = this.getFluid(stack);
				if(Utils.placeFluidBlock(world, x, y, z, fs))
				{
					if(fs.amount<=0)
						fs = null;
					ItemNBTHelper.setFluidStack(stack, "fluid", fs);
					return true;
				}
			}
		}
		return false;
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
		if(resource!=null && resource.getFluid()!=null && !blacklist.contains(resource.getFluid().getName()))
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