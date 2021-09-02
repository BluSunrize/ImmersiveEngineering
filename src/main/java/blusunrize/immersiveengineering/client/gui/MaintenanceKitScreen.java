/*
 * BluSunrize
 * Copyright (c) 2018
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.gui.IESlot.AlwaysEmptySlot;
import blusunrize.immersiveengineering.common.gui.MaintenanceKitContainer;
import blusunrize.immersiveengineering.common.network.MessageMaintenanceKit;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class MaintenanceKitScreen extends ToolModificationScreen<MaintenanceKitContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("maintenance_kit");

	public MaintenanceKitScreen(MaintenanceKitContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.imageWidth = 195;
	}

	@Override
	protected void sendMessage(CompoundTag data)
	{
		ImmersiveEngineering.packetHandler.sendToServer(new MessageMaintenanceKit(menu.getEquipmentSlot(), data));
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		for(int i = 0; i < menu.internalSlots; i++)
		{
			Slot s = menu.getSlot(i);
			if(!(s instanceof AlwaysEmptySlot))
				GuiHelper.drawSlot(leftPos+s.x, topPos+s.y, 16, 16, 0x44, transform);
		}
	}
}