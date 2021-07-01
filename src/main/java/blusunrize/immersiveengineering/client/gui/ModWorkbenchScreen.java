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
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.gui.IESlot;
import blusunrize.immersiveengineering.common.gui.IESlot.BlueprintOutput;
import blusunrize.immersiveengineering.common.gui.ModWorkbenchContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ModWorkbenchScreen extends ToolModificationScreen<ModWorkbenchContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("workbench");

	private final ModWorkbenchTileEntity workbench;

	public ModWorkbenchScreen(ModWorkbenchContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		workbench = container.tile;
		this.ySize = 168;
	}

	@Override
	protected void sendMessage(CompoundNBT data)
	{
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(this.workbench, data));
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		List<InfoArea> areas = new ArrayList<>();
		for(int i = 0; i < container.slotCount; i++)
		{
			Slot s = container.getSlot(i);
			if(s instanceof IESlot.BlueprintOutput)
				areas.add(new BlueprintOutputArea((BlueprintOutput)s, guiLeft, guiTop));
		}
		return areas;
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull MatrixStack transform, float f, int mx, int my)
	{
		for(int i = 0; i < container.slotCount; i++)
		{
			Slot s = container.getSlot(i);
			GuiHelper.drawSlot(transform, guiLeft+s.xPos, guiTop+s.yPos, 16, 16, 0x77222222, 0x77444444, 0x77999999);
		}
	}
}