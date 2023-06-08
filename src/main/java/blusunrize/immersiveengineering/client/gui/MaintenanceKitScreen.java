/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.gui.IESlot.AlwaysEmptySlot;
import blusunrize.immersiveengineering.common.gui.MaintenanceKitContainer;
import blusunrize.immersiveengineering.common.network.MessageMaintenanceKit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;

public class MaintenanceKitScreen extends ToolModificationScreen<MaintenanceKitContainer>
{
	public MaintenanceKitScreen(MaintenanceKitContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("maintenance_kit"));
		this.imageWidth = 195;
	}

	@Override
	protected void sendMessage(CompoundTag data)
	{
		ImmersiveEngineering.packetHandler.sendToServer(new MessageMaintenanceKit(menu.getEquipmentSlot(), data));
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		for(int i = 0; i < menu.internalSlots; i++)
		{
			Slot s = menu.getSlot(i);
			if(!(s instanceof AlwaysEmptySlot))
				GuiHelper.drawSlot(leftPos+s.x, topPos+s.y, 16, 16, 0x44, graphics);
		}
	}
}