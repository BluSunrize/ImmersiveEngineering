/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class RecipeJerrycan extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<IRecipe> implements IRecipe
{
	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, World world)
	{

		ItemStack jerrycan = ItemStack.EMPTY;
		ItemStack container = ItemStack.EMPTY;
		int[] slots = getRelevantSlots(inv);
		if(slots[0] >= 0)
			jerrycan = inv.getStackInSlot(slots[0]);
		if(slots[1] >= 0)
			container = inv.getStackInSlot(slots[1]);
		if(!jerrycan.isEmpty()&&!container.isEmpty())
		{
			IFluidHandler handler = FluidUtil.getFluidHandler(container);
			FluidStack fs = handler.drain(Integer.MAX_VALUE, false);
			return fs==null||(fs.amount < handler.getTankProperties()[0].getCapacity()&&fs.isFluidEqual(FluidUtil.getFluidContained(jerrycan)));
		}
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull InventoryCrafting inv)
	{
		ItemStack jerrycan = ItemStack.EMPTY;
		ItemStack container = ItemStack.EMPTY;
		FluidStack fs = null;
		int[] slots = getRelevantSlots(inv);
		if(slots[0] >= 0)
		{
			jerrycan = inv.getStackInSlot(slots[0]);
			fs = FluidUtil.getFluidContained(jerrycan);
		}
		if(slots[1] >= 0)
			container = inv.getStackInSlot(slots[1]);
		if(fs!=null&&!container.isEmpty())
		{
			ItemStack newContainer = Utils.copyStackWithAmount(container, 1);
			IFluidHandlerItem handler = FluidUtil.getFluidHandler(newContainer);
			int accepted = handler.fill(fs, false);
			if(accepted > 0)
			{
				handler.fill(fs, true);
				newContainer = handler.getContainer();// Because buckets are silly
//				FluidUtil.getFluidHandler(jerrycan).drain(accepted,true);
				ItemNBTHelper.setInt(jerrycan, "jerrycanDrain", accepted);
			}
			return newContainer;
		}
		return ItemStack.EMPTY;
	}

	private int[] getRelevantSlots(InventoryCrafting inv)
	{
		int[] ret = {-1, -1};
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(ret[0] < 0&&IEContent.itemJerrycan.equals(stackInSlot.getItem())&&FluidUtil.getFluidContained(stackInSlot)!=null)
					ret[0] = i;
				else if(ret[1] < 0&&FluidUtil.getFluidHandler(stackInSlot)!=null)
					ret[1] = i;
				else
				{
					ret[0] = ret[1] = -1;
					return ret;
				}
		}
		return ret;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width >= 2&&height >= 2;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput()
	{
		return ItemStack.EMPTY;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv)
	{
		NonNullList<ItemStack> remaining = ForgeHooks.defaultRecipeGetRemainingItems(inv);
		int[] inputs = getRelevantSlots(inv);
		if(inputs[1] >= 0)
			remaining.set(inputs[1], ItemStack.EMPTY);
		return remaining;
	}
}