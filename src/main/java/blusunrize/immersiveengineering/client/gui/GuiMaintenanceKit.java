/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigBoolean;
import blusunrize.immersiveengineering.api.tool.IConfigurableTool.ToolConfig.ToolConfigFloat;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements.GuiSliderIE;
import blusunrize.immersiveengineering.common.gui.ContainerMaintenanceKit;
import blusunrize.immersiveengineering.common.util.network.MessageMaintenanceKit;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class GuiMaintenanceKit extends GuiIEContainerBase
{
	public GuiMaintenanceKit(InventoryPlayer inventoryPlayer, World world, EntityEquipmentSlot slot, ItemStack item)
	{
		super(new ContainerMaintenanceKit(inventoryPlayer, world, slot, item));
		this.xSize = 195;
	}

	@Override
	public void initGui()
	{
		this.buttonList.clear();
		super.initGui();
		Slot s = inventorySlots.getSlot(0);
		if(s!=null&&s.getHasStack()&&s.getStack().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getStack();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			int buttonid = 0;
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			if(boolArray!=null)
				for(ToolConfigBoolean b : boolArray)
					this.buttonList.add(new GuiButtonCheckbox(buttonid++, guiLeft+b.x, guiTop+b.y, tool.fomatConfigName(stack, b), b.value));
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			if(floatArray!=null)
				for(ToolConfigFloat f : floatArray)
					this.buttonList.add(new GuiSliderIE(buttonid++, guiLeft+f.x, guiTop+f.y, 80, tool.fomatConfigName(stack, f), f.value));
		}
	}

	NBTTagCompound lastMessage;

	@Override
	protected void mouseReleased(int mouseX, int mouseY, int state)
	{
		super.mouseReleased(mouseX, mouseY, state);
		Slot s = inventorySlots.getSlot(0);
		if(s!=null&&s.getHasStack()&&s.getStack().getItem() instanceof IConfigurableTool)
		{
			ItemStack stack = s.getStack();
			IConfigurableTool tool = ((IConfigurableTool)stack.getItem());
			NBTTagCompound message = new NBTTagCompound();
			ToolConfigBoolean[] boolArray = tool.getBooleanOptions(stack);
			int iBool = 0;
			ToolConfigFloat[] floatArray = tool.getFloatOptions(stack);
			int iFloat = 0;
			for(GuiButton button : this.buttonList)
			{
				if(button instanceof GuiButtonCheckbox&&boolArray!=null)
				{
					message.setBoolean("b_"+boolArray[iBool].name, ((GuiButtonCheckbox)button).state);
					tool.applyConfigOption(stack, boolArray[iBool++].name, ((GuiButtonCheckbox)button).state);
				}
				if(button instanceof GuiSliderIE&&floatArray!=null)
				{
					message.setFloat("f_"+floatArray[iFloat].name, (float)((GuiSliderIE)button).sliderValue);
					tool.applyConfigOption(stack, floatArray[iFloat++].name, (float)((GuiSliderIE)button).sliderValue);
				}
			}
			if(!message.equals(lastMessage))//Only send packets when values have changed
				ImmersiveEngineering.packetHandler.sendToServer(new MessageMaintenanceKit(((ContainerMaintenanceKit)this.inventorySlots).getEquipmentSlot(), message));
			lastMessage = message;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/maintenance_kit.png");
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(int i = 0; i < ((ContainerMaintenanceKit)inventorySlots).internalSlots; i++)
		{
			Slot s = inventorySlots.getSlot(i);
			ClientUtils.drawSlot(guiLeft+s.xPos, guiTop+s.yPos, 16, 16, 0x44);
		}
	}
}