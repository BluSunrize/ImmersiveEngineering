/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.tool.upgrade.IUpgradeableTool;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.common.MutableDataComponentHolder;
import net.neoforged.neoforge.items.ComponentItemHandler;

public class UpgradeableToolItemHandler extends ComponentItemHandler
{
	public UpgradeableToolItemHandler(MutableDataComponentHolder parent, DataComponentType<ItemContainerContents> component, int size)
	{
		super(parent, component, size);
	}

	@Override
	protected void onContentsChanged(int slot, ItemStack oldStack, ItemStack newStack)
	{
		super.onContentsChanged(slot, oldStack, newStack);
		if(this.parent instanceof ItemStack toolStack&&toolStack.getItem() instanceof IUpgradeableTool upgradeableTool)
		{
			upgradeableTool.recalculateUpgrades(toolStack, null, null);
			if(component instanceof ModWorkbenchContainer modWorkbenchContainer)
				modWorkbenchContainer.rebindSlots();
			else if(component instanceof MaintenanceKitContainer maintenanceKitContainer)
				maintenanceKitContainer.updateSlots();
		}
	}
}
