package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.api.IUpgrade;
import blusunrize.immersiveengineering.common.items.ItemDrill;

public class ContainerDrill extends Container
{
	private World worldObj;
	private int blockedSlot;
	public IInventory input;
	ItemStack drill = null;
	EntityPlayer player = null;
	public final int drillSlots;

	public ContainerDrill(InventoryPlayer iinventory, World world)
	{
		this.worldObj = world;
		this.player = iinventory.player;
		this.drill = iinventory.getCurrentItem();
		this.drillSlots = ((ItemDrill)this.drill.getItem()).getInternalSlots(drill);
		this.input = new InventoryStorageItem(this, drill);
		this.blockedSlot = (iinventory.currentItem + 27 + drillSlots);

		int i=0;
		this.addSlotToContainer(new IESlot.DrillHead(this, this.input,i++, 80,12));
		
		this.addSlotToContainer(new IESlot.Upgrades(this, this.input,i++, 60,44, IUpgrade.UpgradeType.DRILL, true));
		this.addSlotToContainer(new IESlot.Upgrades(this, this.input,i++, 80,50, IUpgrade.UpgradeType.DRILL, true));
		this.addSlotToContainer(new IESlot.Upgrades(this, this.input,i++,100,44, IUpgrade.UpgradeType.DRILL, true));

		bindPlayerInventory(iinventory);

		if (!world.isRemote)
			try {
				((InventoryStorageItem)this.input).stackList = ((ItemDrill)this.drill.getItem()).getContainedItems(this.drill);
			}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		this.onCraftMatrixChanged(this.input);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));

		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot)this.inventorySlots.get(slot);

		if ((slotObject != null) && (slotObject.getHasStack()))
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if (slot < drillSlots) {
				if (!this.mergeItemStack(stackInSlot, drillSlots, this.inventorySlots.size(), true))
					return null;
			}
			else
			{
//				boolean b = true;
//				for(int i=0;i<revolverSlots;i++)
//					if(this.getSlot(i).isItemValid(stackInSlot))
//						if(this.mergeItemStack(Utils.copyStackWithAmount(stackInSlot,1), i,i+1, false))
//						{
//							stackInSlot.stackSize--;
//							stack.stackSize--;
//							b=false;
//							break;
//						}
//				if(b)
					return null;
			}

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();
		}

		return stack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public ItemStack slotClick(int par1, int par2, int par3, EntityPlayer par4EntityPlayer)
	{
		if (par1 == this.blockedSlot)
			return null;		
		((ItemDrill)this.drill.getItem()).setContainedItems(this.drill, ((InventoryStorageItem)this.input).stackList);

		return super.slotClick(par1, par2, par3, par4EntityPlayer);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if (!this.worldObj.isRemote)
		{
			((ItemDrill)this.drill.getItem()).setContainedItems(this.drill, ((InventoryStorageItem)this.input).stackList);
			((ItemDrill)this.drill.getItem()).recalculateUpgrades(this.drill);
			
			
			if (!this.player.getCurrentEquippedItem().equals(this.drill))
				this.player.setCurrentItemOrArmor(0, this.drill);
			this.player.inventory.markDirty();
		}
	}

}