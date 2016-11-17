package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

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
				if(jerrycan==null && IEContent.itemJerrycan.equals(stackInSlot.getItem()) && FluidUtil.getFluidContained(stackInSlot)!=null)
					jerrycan = stackInSlot;
				else if(container==null && FluidUtil.getFluidHandler(stackInSlot)!=null)
					container = stackInSlot;
				else
					return false;
		}
		if(jerrycan!=null&&container!=null)
		{
			IFluidHandler handler = FluidUtil.getFluidHandler(container);
			FluidStack fs = handler.drain(Integer.MAX_VALUE, false);
			if(fs==null || (fs.amount<handler.getTankProperties()[0].getCapacity() && fs.isFluidEqual(FluidUtil.getFluidContained(jerrycan))))
				return true;
		}
		return false;
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
				if(jerrycan==null && IEContent.itemJerrycan.equals(stackInSlot.getItem()) && FluidUtil.getFluidContained(stackInSlot)!=null)
				{
					jerrycan = stackInSlot;
					fs = FluidUtil.getFluidContained(jerrycan);
				}
				else if(container==null && FluidUtil.getFluidHandler(stackInSlot)!=null)
					container = stackInSlot;
		}
		if(fs!=null && container!=null)
		{
			ItemStack newContainer = Utils.copyStackWithAmount(container, 1);
			IFluidHandler handler = FluidUtil.getFluidHandler(newContainer);
			int accepted = handler.fill(fs, false);
			if(accepted>0)
			{
				handler.fill(fs, true);
//				FluidUtil.getFluidHandler(jerrycan).drain(accepted,true);
				ItemNBTHelper.setInt(jerrycan, "jerrycanDrain", accepted);
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

	@Override
	public ItemStack[] getRemainingItems(InventoryCrafting inv)
	{
		return ForgeHooks.defaultRecipeGetRemainingItems(inv);
//		for(int i=0;i<inv.getSizeInventory();i++)
//		{
//			ItemStack stackInSlot = inv.getStackInSlot(i);
//			if(stackInSlot!=null)
//				if(jerrycan==null && IEContent.itemJerrycan.equals(stackInSlot.getItem()) && ItemNBTHelper.hasKey(stackInSlot, "fluid"))
//
//		return new ItemStack[]
	}
}