/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class JerrycanRecipe implements ICraftingRecipe
{
	public static final IRecipeSerializer<JerrycanRecipe> SERIALIZER = IRecipeSerializer.register(
			ImmersiveEngineering.MODID+":jerrycan", new SpecialRecipeSerializer<>(JerrycanRecipe::new)
	);

	private final ResourceLocation id;

	public JerrycanRecipe(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean matches(@Nonnull CraftingInventory inv, World world)
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
			IFluidHandler handler = FluidUtil.getFluidHandler(container)
					.orElseThrow(RuntimeException::new);
			FluidStack fs = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
			return fs.getAmount() < handler.getTankCapacity(0)&&
					fs.isFluidEqual(FluidUtil.getFluidContained(jerrycan).orElseThrow(RuntimeException::new));
		}
		return false;
	}

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull CraftingInventory inv)
	{
		ItemStack jerrycan = ItemStack.EMPTY;
		ItemStack container = ItemStack.EMPTY;
		FluidStack fs = null;
		int[] slots = getRelevantSlots(inv);
		if(slots[0] >= 0)
		{
			jerrycan = inv.getStackInSlot(slots[0]);
			fs = FluidUtil.getFluidContained(jerrycan).orElseThrow(RuntimeException::new);
		}
		if(slots[1] >= 0)
			container = inv.getStackInSlot(slots[1]);
		if(fs!=null&&!container.isEmpty())
		{
			ItemStack newContainer = Utils.copyStackWithAmount(container, 1);
			IFluidHandlerItem handler = FluidUtil.getFluidHandler(newContainer).orElseThrow(RuntimeException::new);
			int accepted = handler.fill(fs, FluidAction.SIMULATE);
			if(accepted > 0)
			{
				handler.fill(fs, FluidAction.EXECUTE);
				newContainer = handler.getContainer();// Because buckets are silly
//				FluidUtil.getFluidHandler(jerrycan).drain(accepted,true);
				ItemNBTHelper.putInt(jerrycan, "jerrycanDrain", accepted);
			}
			return newContainer;
		}
		return ItemStack.EMPTY;
	}

	private int[] getRelevantSlots(IInventory inv)
	{
		int[] ret = {-1, -1};
		for(int i = 0; i < inv.getSizeInventory(); i++)
		{
			ItemStack stackInSlot = inv.getStackInSlot(i);
			if(!stackInSlot.isEmpty())
				if(ret[0] < 0&&Misc.jerrycan.equals(stackInSlot.getItem())&&FluidUtil.getFluidContained(stackInSlot)!=null)
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
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv)
	{
		NonNullList<ItemStack> remaining = ICraftingRecipe.super.getRemainingItems(inv);
		int[] inputs = getRelevantSlots(inv);
		if(inputs[1] >= 0)
			remaining.set(inputs[1], ItemStack.EMPTY);
		return remaining;
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
	{
		return SERIALIZER;
	}

	@Nonnull
	@Override
	public ResourceLocation getId()
	{
		return id;
	}
}