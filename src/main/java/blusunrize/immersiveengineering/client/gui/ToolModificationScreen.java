/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiSliderIE;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public abstract class ToolModificationScreen<C extends AbstractContainerMenu> extends IEContainerScreen<C>
{
	public ToolModificationScreen(C inventorySlotsIn, Inventory inv, Component title)
	{
		super(inventorySlotsIn, inv, title);
	}


	@Override
	public void init()
	{
		this.buttons.clear();
		super.init();
		Slot s = menu.getSlot(0);
		if(s.hasItem()&&s.getItem().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getItem();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			if(boolArray!=null)
				for(ToolConfigBoolean b : boolArray)
					this.addButton(new GuiButtonCheckbox(leftPos+b.x, topPos+b.y, tool.fomatConfigName(stack, b), b.value,
							btn -> dataChanged(btn, b.name)));
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			if(floatArray!=null)
				for(ToolConfigFloat f : floatArray)
					this.addButton(new GuiSliderIE(leftPos+f.x, topPos+f.y, 80, tool.fomatConfigName(stack, f), f.value,
							btn -> dataChanged(btn, f.name)));
		}
	}

	CompoundTag lastMessage;

	private void dataChanged(AbstractWidget changed, String name)
	{
		//TODO this changed to only send diffs, does that work or does MessageMaintenanceKit need to be changed?
		Slot s = menu.getSlot(0);
		if(s.getItem().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getItem();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			CompoundTag message = new CompoundTag();
			if(changed instanceof GuiButtonCheckbox)
			{
				message.putBoolean(name, !((GuiButtonCheckbox)changed).getState());
				tool.applyConfigOption(stack, name, !((GuiButtonCheckbox)changed).getState());
			}
			else if(changed instanceof GuiSliderIE)
			{
				message.putFloat(name, (float)((GuiSliderIE)changed).sliderValue);
				tool.applyConfigOption(stack, name, (float)((GuiSliderIE)changed).sliderValue);
			}
			if(!message.equals(lastMessage))//Only send packets when values have changed
				sendMessage(message);
			lastMessage = message;
		}
	}

	protected abstract void sendMessage(CompoundTag data);
}
