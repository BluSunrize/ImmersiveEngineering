/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import javax.annotation.Nullable;

public abstract class ItemInternalStorage extends ItemIEBase
{

	public ItemInternalStorage(String name, int stackSize, String... subNames)
	{
		super(name, stackSize, subNames);
	}

	public abstract int getSlotCount(ItemStack stack);

	@Nullable
	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
	{
		if(!stack.isEmpty())
			return new IEItemStackHandler(stack);
		return null;
	}

	public void setContainedItems(ItemStack stack, NonNullList<ItemStack> inventory)
	{
		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if(handler instanceof IItemHandlerModifiable)
		{
			if(inventory.size()!=handler.getSlots())
				throw new IllegalArgumentException("Parameter inventory has "+inventory.size()+" slots, capability inventory has "+handler.getSlots());
			for(int i = 0; i < handler.getSlots(); i++)
				((IItemHandlerModifiable)handler).setStackInSlot(i, inventory.get(i));
		}
		else
			IELogger.warn("No valid inventory handler found for "+stack);
	}

	public NonNullList<ItemStack> getContainedItems(ItemStack stack)
	{
		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if(handler instanceof IEItemStackHandler)
			return ((IEItemStackHandler)handler).getContainedItems();
		else if(handler!=null)
		{
			IELogger.warn("Inefficiently getting contained items. Why does "+stack+" have a non-IE IItemHandler?");
			NonNullList<ItemStack> inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			for(int i = 0; i < handler.getSlots(); i++)
				inv.set(i, handler.getStackInSlot(i));
			return inv;
		}
		else
			IELogger.info("No valid inventory handler found for "+stack);
		return NonNullList.create();
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
		//Update old inventories to caps
		if(ItemNBTHelper.hasKey(stack, "Inv"))
		{
			NBTTagList list = ItemNBTHelper.getTag(stack).getTagList("Inv", 10);
			setContainedItems(stack, Utils.readInventory(list, getSlotCount(stack)));
			ItemNBTHelper.remove(stack, "Inv");
			//Sync the changes
			if(entityIn instanceof EntityPlayerMP&&!worldIn.isRemote)
				((EntityPlayerMP)entityIn).connection.sendPacket(new SPacketSetSlot(-2, itemSlot, stack));
		}
	}
}
