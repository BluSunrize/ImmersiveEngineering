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
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public abstract class ToolModificationScreen<C extends Container> extends IEContainerScreen<C>
{
	public ToolModificationScreen(
			C inventorySlotsIn, PlayerInventory inv, ITextComponent title, ResourceLocation background
	)
	{
		super(inventorySlotsIn, inv, title, background);
	}


	@Override
	public void init()
	{
		this.buttons.clear();
		super.init();
		Slot s = container.getSlot(0);
		if(s.getHasStack()&&s.getStack().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getStack();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			if(boolArray!=null)
				for(ToolConfigBoolean b : boolArray)
					this.addButton(new GuiButtonCheckbox(guiLeft+b.x, guiTop+b.y, tool.fomatConfigName(stack, b), b.value,
							btn -> dataChanged(btn, b.name)));
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			if(floatArray!=null)
				for(ToolConfigFloat f : floatArray)
					this.addButton(new GuiSliderIE(guiLeft+f.x, guiTop+f.y, 80, tool.fomatConfigName(stack, f), f.value,
							btn -> dataChanged(btn, f.name)));
		}
	}

	CompoundNBT lastMessage;

	private void dataChanged(Widget changed, String name)
	{
		//TODO this changed to only send diffs, does that work or does MessageMaintenanceKit need to be changed?
		Slot s = container.getSlot(0);
		if(s.getStack().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getStack();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			CompoundNBT message = new CompoundNBT();
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

	protected abstract void sendMessage(CompoundNBT data);
}
