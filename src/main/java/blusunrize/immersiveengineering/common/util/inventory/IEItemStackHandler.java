/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.items.InternalStorageItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IEItemStackHandler extends ItemStackHandler implements ICapabilityProvider
{
	private boolean first = true;
	private ItemStack stack;

	public IEItemStackHandler(ItemStack stack)
	{
		super();
		this.stack = stack;
	}

	@Nonnull
	private Runnable onChange = () -> {
	};

	public void setTile(TileEntity tile)
	{
		if(tile!=null)
		{
			onChange = tile::markDirty;
		}
		else
		{
			onChange = () -> {
			};
		}
	}

	public void setInventoryForUpdate(IInventory inv)
	{
		if(inv!=null)
		{
			onChange = inv::markDirty;
		}
		else
		{
			onChange = () -> {
			};
		}
	}

	@Override
	protected void onContentsChanged(int slot)
	{
		super.onContentsChanged(slot);
		onChange.run();
	}

	//TODO invalidate the LazyOptional objects after use?
	private LazyOptional<IItemHandler> thisOpt = CapabilityUtils.constantOptional(this);

	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
	{
		if(first)
		{
			int idealSize = ((InternalStorageItem)stack.getItem()).getSlotCount(stack);
			NonNullList<ItemStack> newList = NonNullList.withSize(idealSize, ItemStack.EMPTY);
			for(int i = 0; i < Math.min(stacks.size(), idealSize); i++)
				newList.set(i, stacks.get(i));
			stacks = newList;
			stack = ItemStack.EMPTY;
			first = false;
		}
		if(capability==CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return thisOpt.cast();
		return LazyOptional.empty();
	}

	public NonNullList<ItemStack> getContainedItems()
	{
		return stacks;
	}
}
