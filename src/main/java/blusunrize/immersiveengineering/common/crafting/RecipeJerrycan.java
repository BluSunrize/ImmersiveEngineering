/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.RecipeSerializers;
import net.minecraft.item.crafting.RecipeSerializers.SimpleSerializer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;

public class RecipeJerrycan implements IRecipe
{
	public static final IRecipeSerializer<RecipeJerrycan> SERIALIZER = RecipeSerializers.register(
			new SimpleSerializer<>(ImmersiveEngineering.MODID+":jerrycan", RecipeJerrycan::new)
	);

	private final ResourceLocation id;

	public RecipeJerrycan(ResourceLocation id)
	{
		this.id = id;
	}

	@Override
	public boolean matches(@Nonnull IInventory inv, World world)
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
	public ItemStack getCraftingResult(@Nonnull IInventory inv)
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

	private int[] getRelevantSlots(IInventory inv)
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
	public NonNullList<ItemStack> getRemainingItems(IInventory inv)
	{
		NonNullList<ItemStack> remaining = IRecipe.super.getRemainingItems(inv);
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