package blusunrize.immersiveengineering.common.gui;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.IBullet;
import blusunrize.immersiveengineering.api.IDrillHead;
import blusunrize.immersiveengineering.api.IUpgrade;
import blusunrize.immersiveengineering.common.items.ItemUpgradeableTool;

public abstract class IESlot extends Slot
{
	final Container container;
	public IESlot(Container container, IInventory inv, int id, int x, int y)
	{
		super(inv, id, x, y);
		this.container=container;
	}

	@Override
	public boolean isItemValid(ItemStack itemStack)
	{
		return true;
	}

	public static class Output extends IESlot
	{
		public Output(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return false;
		}
	}
	public static class FluidContainer extends IESlot
	{
		boolean empty;
		public FluidContainer(Container container, IInventory inv, int id, int x, int y, boolean empty)
		{
			super(container, inv, id, x, y);
			this.empty=empty;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(empty)
				return FluidContainerRegistry.isEmptyContainer(itemStack);
			else
				return FluidContainerRegistry.isFilledContainer(itemStack);
		}
	}
	public static class BlastFuel extends IESlot
	{
		public BlastFuel(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return BlastFurnaceRecipe.isValidBlastFuel(itemStack);
		}
	}
	public static class Bullet extends IESlot
	{
		int limit;
		public Bullet(Container container, IInventory inv, int id, int x, int y, int limit)
		{
			super(container, inv, id, x, y);
			this.limit=limit;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			//			return true;
			return itemStack!=null && itemStack.getItem() instanceof IBullet;
			//itemStack!=null && itemStack.getItem().equals(IEContent.itemBullet);
		}
		@Override
		public int getSlotStackLimit()
		{
			return limit;
		}
	}
	public static class DrillHead extends IESlot
	{
		public DrillHead(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return itemStack!=null && itemStack.getItem() instanceof IDrillHead;
		}
		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
	}
	public static class Upgrades extends IESlot
	{
		ItemStack upgradeableTool;
		IUpgrade.UpgradeType type;
		boolean preventDoubles;
		public Upgrades(Container container, IInventory inv, int id, int x, int y, IUpgrade.UpgradeType type, ItemStack upgradeableTool, boolean preventDoubles)
		{
			super(container, inv, id, x, y);
			this.type = type;
			this.upgradeableTool = upgradeableTool;
			this.preventDoubles = preventDoubles;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(preventDoubles)
				for(Slot slot : (List<Slot>)container.inventorySlots)
					if(slot instanceof IESlot.Upgrades && ((IESlot.Upgrades)slot).preventDoubles && OreDictionary.itemMatches(slot.getStack(), itemStack, true))
						return false;
			return itemStack!=null && itemStack.getItem() instanceof IUpgrade && ((IUpgrade)itemStack.getItem()).getUpgradeTypes(itemStack).contains(type) && ((IUpgrade)itemStack.getItem()).canApplyUpgrades(upgradeableTool, itemStack);
		}
		@Override
		public int getSlotStackLimit()
		{
			return 64;
		}
	}
	public static class UpgradeableItem extends IESlot
	{
		int size;
		public UpgradeableItem(Container container, IInventory inv, int id, int x, int y, int size)
		{
			super(container, inv, id, x, y);
			this.size = size;
		}
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return itemStack!=null && itemStack.getItem() instanceof ItemUpgradeableTool && ((ItemUpgradeableTool)itemStack.getItem()).canModify(itemStack);
		}
		@Override
		public int getSlotStackLimit()
		{
			return size;
		}
		@Override
	    public void onSlotChanged()
	    {
			super.onSlotChanged();
			if(container instanceof ContainerModWorkbench)
				((ContainerModWorkbench)container).rebindSlots();
	    }
	}
	public static class Ghost extends IESlot
	{
		public Ghost(Container container, IInventory inv, int id, int x, int y)
		{
			super(container, inv, id, x, y);
		}

		@Override
		public void putStack(ItemStack itemStack)
		{
			super.putStack(itemStack);
		}
		@Override
		public boolean canTakeStack(EntityPlayer player)
		{
			return false;
		}
		@Override
		public int getSlotStackLimit()
		{
			return 1;
		}
	}
}