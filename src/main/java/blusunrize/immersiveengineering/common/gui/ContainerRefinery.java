/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityRefinery;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

public class ContainerRefinery extends ContainerIEBase<TileEntityRefinery>
{
	public ContainerRefinery(InventoryPlayer inventoryPlayer, TileEntityRefinery tile)
	{
		super(inventoryPlayer, tile);

		final TileEntityRefinery tileF = tile;
		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 0, 37, 15, 2)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				IFluidHandler h = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
				if(h==null||h.getTankProperties().length==0)
					return false;
				FluidStack fs = h.getTankProperties()[0].getContents();
				if(fs==null)
					return false;
				if(RefineryRecipe.findIncompleteRefineryRecipe(fs, null)==null)
					return false;
				if(tileF.tanks[0].getFluidAmount() > 0&&!fs.isFluidEqual(tileF.tanks[0].getFluid()))
					return false;
				if(tileF.tanks[1].getFluidAmount() <= 0)
					return true;
				List<RefineryRecipe> incomplete = RefineryRecipe.findIncompleteRefineryRecipe(fs, tileF.tanks[1].getFluid());
				return incomplete!=null&&!incomplete.isEmpty();
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 1, 37, 54));

		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 2, 85, 15, 2)
		{
			@Override
			public boolean isItemValid(ItemStack itemStack)
			{
				IFluidHandler h = itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
				if(h==null||h.getTankProperties().length==0)
					return false;
				FluidStack fs = h.getTankProperties()[0].getContents();
				if(fs==null)
					return false;
				if(RefineryRecipe.findIncompleteRefineryRecipe(fs, null)==null)
					return false;
				if(tileF.tanks[1].getFluidAmount() > 0&&!fs.isFluidEqual(tileF.tanks[1].getFluid()))
					return false;
				if(tileF.tanks[0].getFluidAmount() <= 0)
					return true;
				List<RefineryRecipe> incomplete = RefineryRecipe.findIncompleteRefineryRecipe(fs, tileF.tanks[0].getFluid());
				return incomplete!=null&&!incomplete.isEmpty();
			}
		});
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 3, 85, 54));

		this.addSlotToContainer(new IESlot.FluidContainer(this, this.inv, 4, 133, 15, 0));
		this.addSlotToContainer(new IESlot.Output(this, this.inv, 5, 133, 54));
		slotCount = 6;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlotToContainer(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlotToContainer(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}