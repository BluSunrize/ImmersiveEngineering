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
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class IEItemStackHandler extends ItemStackHandler
{
	public IEItemStackHandler(ItemStack stack)
	{
		super();
		int idealSize = ((InternalStorageItem)stack.getItem()).getSlotCount();
		NonNullList<ItemStack> newList = NonNullList.withSize(idealSize, ItemStack.EMPTY);
		for(int i = 0; i < Math.min(stacks.size(), idealSize); i++)
			newList.set(i, stacks.get(i));
		stacks = newList;
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

	@Override
	public void deserializeNBT(CompoundTag nbt)
	{
		// overridden because the Forge handler pulls size from NBT, thus making them forever the same size
		ListTag tagList = nbt.getList("Items", Tag.TAG_COMPOUND);
		stacks.clear();
		for(int i = 0; i < tagList.size(); i++)
		{
			CompoundTag itemTags = tagList.getCompound(i);
			int slot = itemTags.getInt("Slot");
			if(slot >= 0&&slot < stacks.size())
				stacks.set(slot, ItemStack.of(itemTags));
		}
		onLoad();
	}

	public NonNullList<ItemStack> getContainedItems()
	{
		return stacks;
	}
}
