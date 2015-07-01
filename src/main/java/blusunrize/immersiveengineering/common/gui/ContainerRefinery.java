package blusunrize.immersiveengineering.common.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import blusunrize.immersiveengineering.api.DieselHandler;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;

public class ContainerRefinery extends Container
{
	TileEntityRefinery tile;
	int slotCount;
	public ContainerRefinery(InventoryPlayer inventoryPlayer, TileEntityRefinery tile)
	{
		this.tile=tile;
		//		for(int i=0; i<9; i++)
		//			this.addSlotToContainer(new Slot(tile, i, 24+i%3*18, 17+i/3*18));
		//		if(slot==4)
		//			return (tank2.getFluidAmount()<=0?FluidContainerRegistry.isEmptyContainer(stack): FluidContainerRegistry.fillFluidContainer(tank2.getFluid(), Utils.copyStackWithAmount(stack,1))!=null);
		//		FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(stack);
		//		if(fs==null)
		//			return false;
		//		RefineryRecipe partialRecipe = DieselHandler.findIncompleteRefineryRecipe(fs, null);
		//		if(partialRecipe==null)
		//			return false;
		//		if(slot==0)
		//			return (tank0.getFluidAmount()<=0||fs.isFluidEqual(tank0.getFluid())) && (tank1.getFluidAmount()<=0||DieselHandler.findIncompleteRefineryRecipe(fs, tank1.getFluid())!=null);
		//		if(slot==2)
		//			return (tank1.getFluidAmount()<=0||fs.isFluidEqual(tank1.getFluid())) && (tank0.getFluidAmount()<=0||DieselHandler.findIncompleteRefineryRecipe(fs, tank0.getFluid())!=null);

		final TileEntityRefinery tileF = tile;
		this.addSlotToContainer(new IESlot.FluidContainer(this, tile, 0, 37,15, false)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				if(fs==null)
					return false;
				if(DieselHandler.findIncompleteRefineryRecipe(fs, null)==null)
					return false;
				return (tileF.tank0.getFluidAmount()<=0||fs.isFluidEqual(tileF.tank0.getFluid())) && (tileF.tank1.getFluidAmount()<=0||DieselHandler.findIncompleteRefineryRecipe(fs, tileF.tank1.getFluid())!=null);
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, tile, 1, 37,54));

		this.addSlotToContainer(new IESlot.FluidContainer(this, tile, 2, 85,15, false)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				FluidStack fs = FluidContainerRegistry.getFluidForFilledItem(itemStack);
				if(fs==null)
					return false;
				if(DieselHandler.findIncompleteRefineryRecipe(fs, null)==null)
					return false;
				return (tileF.tank1.getFluidAmount()<=0||fs.isFluidEqual(tileF.tank1.getFluid())) && (tileF.tank0.getFluidAmount()<=0||DieselHandler.findIncompleteRefineryRecipe(fs, tileF.tank0.getFluid())!=null);
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, tile, 3, 85,54));

		//		this.addSlotToContainer(new IESlot.FluidContainer(tile, 4, 62,21, true));
		this.addSlotToContainer(new IESlot.FluidContainer(this, tile, 4, 133,15, true)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				return super.isItemValid(itemStack) || (itemStack!=null && itemStack.getItem() instanceof IFluidContainerItem);
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, tile, 5, 133,54));
		slotCount=6;

		for (int i = 0; i < 3; i++)
			for (int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 84+i*18));
		for (int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 142));
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_)
	{
		return true;
	}


	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		if (slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if (slot < slotCount)
			{
				if(!this.mergeItemStack(stackInSlot, slotCount, (slotCount + 36), true))
					return null;
			}
			else
			{
				boolean b = true;
				for(int i=0; i<slotCount; i++)
				{
					Slot s = this.getSlot(i);
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
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
}