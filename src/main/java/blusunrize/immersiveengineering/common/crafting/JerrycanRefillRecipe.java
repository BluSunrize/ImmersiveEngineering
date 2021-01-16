/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class JerrycanRefillRecipe extends SpecialRecipe
{
	private final int jerrycanIndex = 0;
	private final int containerIndex = 1;

	public JerrycanRefillRecipe(ResourceLocation resourceLocation)
	{
		super(resourceLocation);
	}

	@Override
	public boolean matches(@Nonnull CraftingInventory inv, World world)
	{
		ItemStack[] components = getComponents(inv);
		if(!components[jerrycanIndex].isEmpty()&&!components[containerIndex].isEmpty()&&countOccupiedSlots(inv) == 2)
		{
			FluidStack jerrycanFluid = FluidUtil.getFluidContained(components[jerrycanIndex]).orElseThrow(RuntimeException::new);
			if(!jerrycanFluid.isEmpty())
			{
				IFluidHandler handler = FluidUtil.getFluidHandler(components[containerIndex])
						.orElseThrow(RuntimeException::new);
				FluidStack containerFluid = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
				return (containerFluid.getAmount() < handler.getTankCapacity(0)&&handler.isFluidValid(0, jerrycanFluid));
			}
		}
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull CraftingInventory inv)
	{
		ItemStack[] components = getComponents(inv);
		ItemStack newContainer = Utils.copyStackWithAmount(components[containerIndex], 1);
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(newContainer).orElseThrow(RuntimeException::new);
		ItemNBTHelper.putInt(components[jerrycanIndex], "jerrycanDrain", handler.fill(FluidUtil.getFluidContained(components[jerrycanIndex]).orElseThrow(RuntimeException::new), FluidAction.EXECUTE));
		newContainer = handler.getContainer();// Because buckets are silly
		return newContainer;
	}

	private ItemStack[] getComponents(IInventory inv)
	{
		ItemStack[] ret = {ItemStack.EMPTY, ItemStack.EMPTY};
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				if(ret[0].isEmpty()&&Misc.jerrycan.equals(stackInSlot.getItem())&&FluidUtil.getFluidContained(stackInSlot)!=null)
					ret[0] = stackInSlot;
				else if(ret[1].isEmpty()&&FluidUtil.getFluidHandler(stackInSlot).isPresent())
					ret[1] = stackInSlot;
				else
					return ret;
			}
		}
		return ret;
	}

	private int countOccupiedSlots(IInventory inv) {
		int c = 0;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			if(!inv.getStackInSlot(i).isEmpty())
				c++;
		}
		return c;
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width*height >= 2;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
	{
		NonNullList<ItemStack> remaining = super.getRemainingItems(inv);
		boolean foundJerrycan = false;
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
			{
				if(Misc.jerrycan.equals(stackInSlot.getItem())&&!foundJerrycan)
				{
					foundJerrycan = true;
					continue;
				}
				remaining.set(i, ItemStack.EMPTY);
			}
		}
		return remaining;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.JERRYCAN_REFILL.get();
	}
}