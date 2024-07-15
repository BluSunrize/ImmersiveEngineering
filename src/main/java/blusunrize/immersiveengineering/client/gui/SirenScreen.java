/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiSelectBox;
import blusunrize.immersiveengineering.common.blocks.metal.SirenBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.SirenBlockEntity.SirenSound;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Optional;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class SirenScreen extends ClientBlockEntityScreen<SirenBlockEntity>
{
	public SirenScreen(SirenBlockEntity tileEntity, Component title)
	{
		super(tileEntity, title);
		this.xSize = 118;
		this.ySize = 120;
	}

	private GuiButtonBoolean[] colorButtons;

	@Override
	public void init()
	{
		super.init();

		clearWidgets();

		colorButtons = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtons.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtons[i] = RedstoneConnectorScreen.buildColorButton(colorButtons, guiLeft+4+(i%4*14), guiTop+22+(i/4*14),
					() -> blockEntity.redstoneChannel==color, color, btn -> sendConfig("redstoneChannel", color.getId()));
			this.addRenderableWidget(colorButtons[i]);
		}

		this.addRenderableWidget(new GuiSelectBox<>(
				guiLeft+64, guiTop+22, 50, SirenSound::values, () -> blockEntity.sound.ordinal(),
				SirenSound::getComponent, box -> sendConfig("sound", box.getClickedState())
		));
	}

	public void sendConfig(String key, int value)
	{
		CompoundTag message = new CompoundTag();
		message.putInt(key, value);
		// clientside
		blockEntity.receiveMessageFromClient(message);
		// serverside
		PacketDistributor.SERVER.noArg().send(new MessageBlockEntitySync(blockEntity, message));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<Component> tooltip = new ArrayList<>();

		for(int i = 0; i < colorButtons.length; i++)
			if(colorButtons[i].isHovered())
			{
				tooltip.add(Component.translatable(Lib.GUI_CONFIG+"redstone_color"));
				tooltip.add(TextUtils.applyFormat(
						Component.translatable("color.minecraft."+DyeColor.byId(i).getName()),
						ChatFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if(mc().options.keyInventory.isActiveAndMatches(mouseKey))
		{
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
}