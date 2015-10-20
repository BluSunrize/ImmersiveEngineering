package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;

public class RecipeJerrycan implements IRecipe
{
	@Override
	public boolean matches(InventoryCrafting inv, World world)
	{
		ItemStack jerrycan = null;
		ItemStack container = null;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
				if(jerrycan==null && IEContent.itemJerrycan.equals(stackInSlot.getItem()) && ItemNBTHelper.hasKey(stackInSlot, "fluid"))
					jerrycan = stackInSlot;
				else if(container==null && stackInSlot.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem)stackInSlot.getItem()).getFluid(stackInSlot)==null)
					container = stackInSlot;
				else
					return false;
		}
		return jerrycan!=null&&container!=null;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inv)
	{
		ItemStack jerrycan = null;
		ItemStack container = null;
		FluidStack fs = null;
		for(int i=0;i<inv.getSizeInventory();i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(stackInSlot!=null)
				if(jerrycan==null && IEContent.itemJerrycan.equals(stackInSlot.getItem()) && ItemNBTHelper.hasKey(stackInSlot, "fluid"))
				{
					jerrycan = stackInSlot;
					fs = ((IFluidContainerItem)IEContent.itemJerrycan).getFluid(jerrycan);
				}
				else if(container==null && stackInSlot.getItem() instanceof IFluidContainerItem)
					container = stackInSlot;
		}
		if(fs!=null && container!=null)
		{
			ItemStack newContainer = Utils.copyStackWithAmount(container, 1);
			int accepted = ((IFluidContainerItem)newContainer.getItem()).fill(newContainer, fs, false);
			if(accepted>0)
			{
				((IFluidContainerItem)newContainer.getItem()).fill(newContainer, fs, true);
				ItemNBTHelper.setInt(newContainer, "jerrycanFilling", accepted);
			}
			return newContainer;
		}
		return null;
	}

	@Override
	public int getRecipeSize()
	{
		return 10;
	}
	@Override
	public ItemStack getRecipeOutput()
	{
		return null;
	}

}
