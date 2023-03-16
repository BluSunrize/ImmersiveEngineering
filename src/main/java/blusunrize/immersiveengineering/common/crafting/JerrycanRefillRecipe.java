/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;

public class JerrycanRefillRecipe extends CustomRecipe
{
	private final int jerrycanIndex = 0;
	private final int containerIndex = 1;

	public JerrycanRefillRecipe(ResourceLocation resourceLocation)
	{
		super(resourceLocation, CraftingBookCategory.MISC);
	}

	@Override
	public boolean matches(@Nonnull CraftingContainer inv, Level world)
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
	public ItemStack assemble(@Nonnull CraftingContainer inv, RegistryAccess access)
	{
		ItemStack[] components = getComponents(inv);
		ItemStack newContainer = ItemHandlerHelper.copyStackWithSize(components[containerIndex], 1);
		IFluidHandlerItem handler = FluidUtil.getFluidHandler(newContainer).orElseThrow(RuntimeException::new);
		FluidUtil.getFluidContained(components[jerrycanIndex]).ifPresent(fs -> {
			ItemNBTHelper.putInt(components[jerrycanIndex], "jerrycanDrain", handler.fill(fs, FluidAction.EXECUTE));
		});
		newContainer = handler.getContainer();// Because buckets are silly
		return newContainer;
	}

	private ItemStack[] getComponents(Container inv)
	{
		ItemStack[] ret = {ItemStack.EMPTY, ItemStack.EMPTY};
		for(int i = 0; i < inv.getContainerSize(); i++)
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

	private int countOccupiedSlots(Container inv)
	{
		int c = 0;
		for(int i = 0; i < inv.getContainerSize(); i++)
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
	public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv)
	{
		NonNullList<ItemStack> remaining = super.getRemainingItems(inv);
		boolean foundJerrycan = false;
		for(int i = 0; i < inv.getContainerSize(); i++)
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