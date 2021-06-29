/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.metal.MixerTileEntity;
import blusunrize.immersiveengineering.common.gui.IESlot.ICallbackContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

//TODO custom subclass of ItemStackHandler for markDirty etc
public class MixerContainer extends IEBaseContainer<MixerTileEntity> implements ICallbackContainer
{
	public MixerContainer(ContainerType<?> type, int id, PlayerInventory inventoryPlayer, MixerTileEntity tile)
	{
		super(type, inventoryPlayer, tile, id);

		IItemHandler inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)
				.orElseThrow(RuntimeException::new);
		for(int i = 0; i < 8; i++)
			this.addSlot(new IESlot.ContainerCallback(this, inv, i, 7+(i%2)*21, 7+(i/2)*18));
		slotCount = 8;

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 86+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 144));
	}

	@Override
	public boolean canInsert(ItemStack stack, int slotNumer, Slot slotObject)
	{
		for(MultiblockProcess<MixerRecipe> process : tile.processQueue)
			if(process instanceof MultiblockProcessInMachine)
				for(int s : ((MultiblockProcessInMachine)process).getInputSlots())
					if(s==slotNumer)
						return false;
		return true;
	}

	@Override
	public boolean canTake(ItemStack stack, int slotNumer, Slot slotObject)
	{
		for(MultiblockProcess<MixerRecipe> process : tile.processQueue)
			if(process instanceof MultiblockProcessInMachine)
				for(int s : ((MultiblockProcessInMachine)process).getInputSlots())
					if(s==slotNumer)
						return false;
		return true;
	}
}