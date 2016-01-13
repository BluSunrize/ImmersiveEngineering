package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.energy.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.energy.IWireCoil;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.gui.IESlot.ICallbackContainer;
import blusunrize.immersiveengineering.common.items.ItemToolbox;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;

public class ContainerToolbox extends Container implements ICallbackContainer
{
	private World worldObj;
	private int blockedSlot;
	public IInventory input;
	ItemStack toolbox = null;
	EntityPlayer player = null;
	public final int internalSlots;

	public ContainerToolbox(InventoryPlayer iinventory, World world)
	{
		this.worldObj = world;
		this.player = iinventory.player;
		this.toolbox = iinventory.getCurrentItem();
		this.internalSlots = ((ItemToolbox)this.toolbox.getItem()).getInternalSlots(toolbox);
		this.input = new InventoryStorageItem(this, toolbox);
		this.blockedSlot = (iinventory.currentItem + 27 + internalSlots);

		int i=0;
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 48, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 30, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 48, 42));

		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 75, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 93, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++,111, 24));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 75, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 93, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++,111, 42));
		this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++,129, 42));

		for(int j=0; j<6; j++)
			this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 35+j*18, 77));
		for(int j=0; j<7; j++)
			this.addSlotToContainer(new IESlot.ContainerCallback(this, this.input, i++, 26+j*18, 112));

		bindPlayerInventory(iinventory);

		if (!world.isRemote)
			try {
				((InventoryStorageItem)this.input).stackList = ((ItemToolbox)this.toolbox.getItem()).getContainedItems(this.toolbox);
			}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		this.onCraftMatrixChanged(this.input);
	}

	@Override
	public boolean canInsert(ItemStack stack, int slotNumer, Slot slotObject)
	{
		if(stack==null)
			return true;
		if(IEContent.itemToolbox.equals(stack.getItem()))
			return false;
		if(slotNumer<3)
			return stack.getItem() instanceof ItemFood;
		else if(slotNumer<10)
		{
			if(stack.getItem() instanceof ItemTool || stack.getItem().equals(IEContent.itemTool))
				return true;
			if(stack.getItem().equals(IEContent.itemDrill) || stack.getItem().equals(IEContent.itemRevolver) || stack.getItem().equals(IEContent.itemChemthrower) || stack.getItem().equals(IEContent.itemRailgun))
				return true;
		}
		else if(slotNumer<16)
		{
			if(stack.getItem() instanceof IWireCoil)
				return true;
			if(Block.getBlockFromItem(stack.getItem())!=null && Block.getBlockFromItem(stack.getItem()).hasTileEntity(stack.getItemDamage()))
				return Block.getBlockFromItem(stack.getItem()).createTileEntity(worldObj, stack.getItemDamage()) instanceof IImmersiveConnectable;
		}
		else
		{
			return true;
		}
		return false;
	}
	@Override
	public boolean canTake(ItemStack stack, int slotNumer, Slot slotObject)
	{
		return true;
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				this.addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 157+i*18));

		for (int i = 0; i < 9; i++)
			this.addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 215));
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		if(slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if(slot < internalSlots)
			{
				if(!this.mergeItemStack(stackInSlot, internalSlots, (internalSlots + 36), true))
					return null;
			}
			else if(stackInSlot!=null)
			{
				boolean b = true;
				for(int i=0; i<internalSlots; i++)
				{
					Slot s = (Slot)inventorySlots.get(i);
					if(s!=null && s.isItemValid(stackInSlot))
						if(this.mergeItemStack(stackInSlot, i, i+1, true))
						{
							b = false;
							break;
						}
						else
							continue;
				}
				if(b)
					return null;
			}

			if (stackInSlot.stackSize == 0)
				slotObject.putStack(null);
			else
				slotObject.onSlotChanged();

			if (stackInSlot.stackSize == stack.stackSize)
				return null;
			slotObject.onPickupFromSlot(player, stack);
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
		if(par1 == this.blockedSlot || (par3!=0&&par2==par4EntityPlayer.inventory.currentItem))
			return null;		
		((ItemToolbox)this.toolbox.getItem()).setContainedItems(this.toolbox, ((InventoryStorageItem)this.input).stackList);

		return super.slotClick(par1, par2, par3, par4EntityPlayer);
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed(par1EntityPlayer);
		if (!this.worldObj.isRemote)
		{
			((ItemToolbox)this.toolbox.getItem()).setContainedItems(this.toolbox, ((InventoryStorageItem)this.input).stackList);
			if(!this.toolbox.equals(this.player.getCurrentEquippedItem()))
				this.player.setCurrentItemOrArmor(0, this.toolbox);
			this.player.inventory.markDirty();
		}
	}
}