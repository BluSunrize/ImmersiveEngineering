/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.gui.info.BlueprintOutputArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchBlockEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.IESlot.AlwaysEmptySlot;
import blusunrize.immersiveengineering.common.gui.IESlot.BlueprintOutput;
import blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ModWorkbenchScreen extends ToolModificationScreen<ModWorkbenchContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("workbench");

	private final ModWorkbenchBlockEntity workbench;

	public ModWorkbenchScreen(ModWorkbenchContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		workbench = container.tile;
		this.imageHeight = 168;
	}

	@Override
	protected void sendMessage(CompoundTag data)
	{
		ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(this.workbench, data));
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> areas = new ArrayList<>();
		for(int i = 0; i < menu.ownSlotCount; i++)
		{
			Slot s = menu.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput)
				areas.add(new BlueprintOutputArea((BlueprintOutput)s, leftPos, topPos));
		}
		return areas;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		for(int i = 0; i < menu.ownSlotCount; i++)
		{
			Slot s = menu.getSlot(i);
			if(!(s instanceof AlwaysEmptySlot))
				GuiHelper.drawSlot(graphics, leftPos+s.x, topPos+s.y, 16, 16, 0x77222222, 0x77444444, 0x77999999);
		}
	}
}