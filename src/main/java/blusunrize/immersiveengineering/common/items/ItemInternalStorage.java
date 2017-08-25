package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.inventory.IEItemStackHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
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
		return new IEItemStackHandler(stack);
	}

	public void setContainedItems(ItemStack stack, NonNullList<ItemStack> inventory)
	{
		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (handler instanceof IItemHandlerModifiable)
		{
			if (inventory.size()!=handler.getSlots())
				throw new IllegalArgumentException("Parameter inventory has "+inventory.size()+" slots, capability inventory has "+handler.getSlots());
			for (int i = 0; i < handler.getSlots(); i++)
				((IItemHandlerModifiable) handler).setStackInSlot(i, inventory.get(i));
		}
		else
			IELogger.warn("No valid inventory handler found for "+stack);
	}

	public NonNullList<ItemStack> genContainedItems(ItemStack stack)
	{
		IItemHandler handler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		if (handler != null)
		{
			NonNullList<ItemStack> inv = NonNullList.withSize(handler.getSlots(), ItemStack.EMPTY);
			for (int i = 0; i < handler.getSlots(); i++)
				inv.set(i, handler.getStackInSlot(i));
			return inv;
		}
		else
			IELogger.info("No valid inventory handler found for "+stack);
		return NonNullList.create();
	}
}
