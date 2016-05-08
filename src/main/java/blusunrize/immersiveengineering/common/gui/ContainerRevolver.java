package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.common.items.ItemRevolver;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ContainerRevolver extends Container
{
	private World worldObj;
	private int blockedSlot;
	public IInventory input;
	ItemStack revolver = null;
	EntityPlayer player = null;
	public final int revolverSlots;

	public static int[][][] slotPositions = {
			{
				{ 48, 11},
				{ 56, 30}
			},
			{
				{ 48, 11},
				{ 68,  3},
				{ 78, 22},
				{ 88,  3},
				{ 98, 22},
				{108,  3},
				{118, 22},
				{ 56, 30}
			},
			{
				{ 48,  3},
				{ 67,  3},
				{ 86,  3},
				{105,  3},
				{124, 11},
				{132, 30},
				{124, 49},
				{105, 57},
				{ 86, 49},
				{ 86, 30},
				{ 67, 30},
				{ 48, 30},
			}
	};

	public ContainerRevolver(InventoryPlayer iinventory, World world)
	{
		this.worldObj = world;
		this.player = iinventory.player;
		this.revolver = iinventory.getCurrentItem();
		this.revolverSlots = ((ItemRevolver)revolver.getItem()).getBulletSlotAmount(revolver);
		this.input = new InventoryStorageItem(this, revolver);

		int i=0;
		int w = revolverSlots>=18?150: revolverSlots>8?136: 74;
		int off = (176-w)/2;

		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, off+29, 3, 1));
		int slots = revolverSlots>=18?2: revolverSlots>8?1: 0;
		for(int[] slot : slotPositions[slots])
			this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, off+ slot[0],slot[1], 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, off+ 48, 49, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, off+ 29, 57, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, off+ 10, 49, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, off+  2, 30, 1));
		this.addSlotToContainer(new IESlot.Bullet(this, this.input,i++, off+ 10, 11, 1));


		this.blockedSlot = (iinventory.currentItem + 27 + i);


		bindPlayerInventory(iinventory);

		if (!world.isRemote)
			try {
				((InventoryStorageItem)this.input).stackList = ((ItemRevolver)this.revolver.getItem()).getContainedItems(this.revolver);
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

			if (slot < revolverSlots)
			{
				if (!this.mergeItemStack(stackInSlot, revolverSlots, this.inventorySlots.size(), true))
					return null;
			}
			else
			{
				boolean b = false;
				for(int i=0;i<revolverSlots;i++)
					if(this.getSlot(i).isItemValid(stackInSlot) && !this.getSlot(i).getHasStack())
						if(this.mergeItemStack(Utils.copyStackWithAmount(stackInSlot,1), i,i+1, false))
						{
							stackInSlot.stackSize--;
							stack.stackSize--;
							b = true;
							break;
						}
				if(!b)
					return null;
			}

			if(stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if(stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stackInSlot);
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
		if(par1 == this.blockedSlot || (par3==2&&par2==par4EntityPlayer.inventory.currentItem))
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
			ItemStack hand = this.player.getCurrentEquippedItem();
			if (hand!=null&&!hand.equals(this.revolver))
				this.player.setCurrentItemOrArmor(0, this.revolver);
			this.player.inventory.markDirty();
		}
	}

}