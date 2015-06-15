package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.items.ItemRevolver;

public class ContainerRevolver extends Container
{
	private World worldObj;
	private int blockedSlot;
	public IInventory input;
	ItemStack revolver = null;
	EntityPlayer player = null;
	public final int revolverSlots;

	public ContainerRevolver(InventoryPlayer iinventory, World world)
	{
		this.worldObj = world;
		this.player = iinventory.player;
		this.revolver = iinventory.getCurrentItem();
		this.revolverSlots = ((ItemRevolver)revolver.getItem()).getInternalSlots(revolver);
		this.input = new InventoryStorageItem(this, revolver);
		this.blockedSlot = (iinventory.currentItem + 27 + revolverSlots);

		int bulletSlots = ((ItemRevolver)revolver.getItem()).getBulletSlotAmount(revolver);
		int i=0;
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, 80,31, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,101,37, 1));
		if(bulletSlots>8)
		{
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,120,37, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,141,31, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,162,37, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,168,58, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,162,79, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,141,85, 1));
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,120,79, 1));
		}
		else
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,107,58, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++,101,79, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, 80,85, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, 59,79, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, 53,58, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, 59,37, 1));

		
//		this.addSlotToContainer(new IESlot.Bullet(this.input,0, 80,31, 1));
//		this.addSlotToContainer(new IESlot.Bullet(this.input,1,101,37, 1));
//		this.addSlotToContainer(new IESlot.Bullet(this.input,2,107,58, 1));
//		this.addSlotToContainer(new IESlot.Bullet(this.input,3,101,79, 1));
//		this.addSlotToContainer(new IESlot.Bullet(this.input,4, 80,85, 1));
//		this.addSlotToContainer(new IESlot.Bullet(this.input,5, 59,79, 1));
//		this.addSlotToContainer(new IESlot.Bullet(this.input,6, 53,58, 1));
//		this.addSlotToContainer(new IESlot.Bullet(this.input,7, 59,37, 1));
		

		bindPlayerInventory(iinventory);

		if (!world.isRemote)
			try {
				((InventoryStorageItem)this.input).stackList = ((ItemRevolver)this.revolver.getItem()).getBullets(this.revolver);
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
				this.addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 133+i*18));

		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 191));
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

			if (slot < revolverSlots) {
				if (!this.mergeItemStack(stackInSlot, revolverSlots, this.inventorySlots.size(), true))
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
		((ItemRevolver)this.revolver.getItem()).setBullets(this.revolver, ((InventoryStorageItem)this.input).stackList);

		return super.slotClick(par1, par2, par3, par4EntityPlayer);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if (!this.worldObj.isRemote)
		{
			((ItemRevolver)this.revolver.getItem()).setBullets(this.revolver, ((InventoryStorageItem)this.input).stackList);

			if (!this.player.getCurrentEquippedItem().equals(this.revolver))
				this.player.setCurrentItemOrArmor(0, this.revolver);
			this.player.inventory.markDirty();
		}
	}

}