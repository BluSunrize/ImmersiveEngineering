/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class JerrycanRefillRecipe extends CustomRecipe
{
	private final int jerrycanIndex = 0;
	private final int containerIndex = 1;

	public JerrycanRefillRecipe(CraftingBookCategory category)
	{
		super(category);
	}

	@Override
	public boolean matches(@Nonnull CraftingInput inv, Level world)
	{
		ItemStack[] components = getComponents(inv);
		if(!components[jerrycanIndex].isEmpty()&&!components[containerIndex].isEmpty()&&countOccupiedSlots(inv)==2)
		{
			return FluidUtil.getFluidContained(components[jerrycanIndex]).map(fs -> {
				IFluidHandler handler = FluidUtil.getFluidHandler(components[containerIndex])
						.orElseThrow(RuntimeException::new);
				FluidStack containerFluid = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
				return (containerFluid.getAmount() < handler.getTankCapacity(0)&&handler.isFluidValid(0, fs));
			}).orElse(false);
		}
		return false;
	}

	@Nonnull
	@Override
	public ItemStack assemble(@Nonnull CraftingInput inv, Provider access)
	{
		ItemStack[] components = getComponents(inv);
		ItemStack newContainer = components[containerIndex].copyWithCount(1);
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(newContainer).orElseThrow(RuntimeException::new);
		FluidUtil.getFluidContained(components[jerrycanIndex]).ifPresent(fs -> {
			components[jerrycanIndex].set(IEDataComponents.JERRYCAN_DRAIN, handler.fill(fs, FluidAction.EXECUTE));
		});
		newContainer = handler.getContainer();// Because buckets are silly
		return newContainer;
	}

	private ItemStack[] getComponents(RecipeInput inv)
	{
		ItemStack[] ret = {ItemStack.EMPTY, ItemStack.EMPTY};
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				if(ret[0].isEmpty()&&stackInSlot.is(Misc.JERRYCAN.asItem())
						&&FluidUtil.getFluidContained(stackInSlot).map(fs -> !fs.isEmpty()).orElse(false))
					ret[0] = stackInSlot;
				else if(ret[1].isEmpty()&&FluidUtil.getFluidHandler(stackInSlot).isPresent())
					ret[1] = stackInSlot;
				else
					return ret;
			}
		}
		return ret;
	}

	private int countOccupiedSlots(CraftingInput inv)
	{
		int c = 0;
		for(int i = 0; i < inv.size(); i++)
			if(!inv.getItem(i).isEmpty())
				c++;
		return c;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width*height >= 2;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInput inv)
	{
		NonNullList<ItemStack> remaining = super.getRemainingItems(inv);
		boolean foundJerrycan = false;
		for(int i = 0; i < inv.size(); i++)
		{
			ItemStack stackInSlot = inv.getItem(i);
			if(!stackInSlot.isEmpty())
			{
				if(stackInSlot.is(Misc.JERRYCAN.asItem())&&!foundJerrycan)
					foundJerrycan = true;
				else
					remaining.set(i, ItemStack.EMPTY);
			}
		}
		return remaining;
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.JERRYCAN_REFILL.get();
	}
}