/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.inventory;

import blusunrize.immersiveengineering.common.items.InternalStorageItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IEItemStackHandler extends ItemStackHandler
{
	public IEItemStackHandler(InternalStorageItem item, @Nullable IEItemStackHandler other)
	{
		super(item.getSlotCount());
		if(other!=null)
			for(int i = 0; i < Math.min(getSlots(), other.getSlots()); ++i)
				setStackInSlot(i, other.getStackInSlot(i));
	}

	public IEItemStackHandler()
	{
		super();
	}

	@Nonnull
	private Runnable onChange = () -> {
	};

	public void setTile(BlockEntity tile)
	{
		if(tile!=null)
		{
			onChange = tile::setChanged;
		}
		else
		{
			onChange = () -> {
			};
		}
	}

	public void setInventoryForUpdate(Container inv)
	{
		if(inv!=null)
		{
			onChange = inv::setChanged;
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

	public NonNullList<ItemStack> getContainedItems()
	{
		return stacks;
	}
}
