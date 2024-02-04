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
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public abstract class ToolModificationScreen<C extends AbstractContainerMenu> extends IEContainerScreen<C>
{
	public ToolModificationScreen(
			C inventorySlotsIn, Inventory inv, Component title, ResourceLocation background
	)
	{
		super(inventorySlotsIn, inv, title, background);
	}


	@Override
	public void init()
	{
		this.clearWidgets();
		super.init();
		Slot s = menu.getSlot(0);
		if(s.hasItem()&&s.getItem().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getItem();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			if(boolArray!=null)
				for(ToolConfigBoolean b : boolArray)
					this.addRenderableWidget(new GuiButtonCheckbox(leftPos+b.x, topPos+b.y, tool.fomatConfigName(stack, b), () -> b.value,
							btn -> sendChange(!btn.getState(), b.name, ByteTag::valueOf)));
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			if(floatArray!=null)
				for(ToolConfigFloat f : floatArray)
					this.addRenderableWidget(new GuiSliderIE(
							leftPos+f.x, topPos+f.y, 80,
							tool.fomatConfigName(stack, f), f.min, f.max,
							f.value, value -> sendChange(value, f.name, FloatTag::valueOf)
					));
		}
	}

	CompoundTag lastMessage;

	private <T> void sendChange(T value, String optionName, Function<T, Tag> makeTag)
	{
		Slot s = menu.getSlot(0);
		if(s.getItem().getItem() instanceof IConfigurableTool tool)
		{
			ItemStack stack = s.getItem();
			CompoundTag message = new CompoundTag();
			message.put(optionName, makeTag.apply(value));
			tool.applyConfigOption(stack, optionName, value);
			if(!message.equals(lastMessage))//Only send packets when values have changed
				sendMessage(message);
			lastMessage = message;
		}
	}

	protected abstract void sendMessage(CompoundTag data);
}
