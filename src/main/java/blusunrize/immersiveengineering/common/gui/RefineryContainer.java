/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.blocks.metal.RefineryTileEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import java.util.List;
import java.util.Optional;

public class RefineryContainer extends IEBaseContainer<RefineryTileEntity>
{
	public RefineryContainer(int id, Inventory inventoryPlayer, RefineryTileEntity tile)
	{
		super(inventoryPlayer, tile, id);

		final RefineryTileEntity tileF = tile;
		this.addSlot(new IESlot.FluidContainer(this, this.inv, 0, 37, 15, 2)
		{
			@Override
			public boolean mayPlace(ItemStack itemStack)
			{
				return itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
						.map(h -> {
							if(h.getTanks() <= 0)
								return false;
							FluidStack fs = h.getFluidInTank(0);
							if(fs.isEmpty())
								return false;
							if(!RefineryRecipe.findIncompleteRefineryRecipe(fs, FluidStack.EMPTY).isPresent())
								return false;
							if(tileF.tanks[0].getFluidAmount() > 0&&!fs.isFluidEqual(tileF.tanks[0].getFluid()))
								return false;
							if(tileF.tanks[1].getFluidAmount() <= 0)
								return true;
							Optional<RefineryRecipe> incomplete = RefineryRecipe.findIncompleteRefineryRecipe(fs, tileF.tanks[1].getFluid());
							return incomplete.isPresent();
						}).orElse(false);
			}
		});
		this.addSlot(new IESlot.Output(this, this.inv, 1, 37, 54));

		this.addSlot(new IESlot.FluidContainer(this, this.inv, 2, 85, 15, 2)
		{
			@Override
			public boolean mayPlace(ItemStack itemStack)
			{
				return itemStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)
						.map(h -> {
							if(h.getTanks() <= 0)
								return false;
							FluidStack fs = h.getFluidInTank(0);
							if(fs.isEmpty())
								return false;
							if(!RefineryRecipe.findIncompleteRefineryRecipe(fs, FluidStack.EMPTY).isPresent())
								return false;
							if(tileF.tanks[1].getFluidAmount() > 0&&!fs.isFluidEqual(tileF.tanks[1].getFluid()))
								return false;
							if(tileF.tanks[0].getFluidAmount() <= 0)
								return true;
							Optional<RefineryRecipe> incomplete = RefineryRecipe.findIncompleteRefineryRecipe(fs, tileF.tanks[0].getFluid());
							return incomplete.isPresent();
						}).orElse(false);
			}
		});
		this.addSlot(new IESlot.Output(this, this.inv, 3, 85, 54));

		this.addSlot(new IESlot.FluidContainer(this, this.inv, 4, 133, 15, 0));
		this.addSlot(new IESlot.Output(this, this.inv, 5, 133, 54));
		slotCount = 6;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 85+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 143));
	}
}