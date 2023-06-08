/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiSliderIE;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorProbeBlockEntity;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.Optional;

public class RedstoneProbeScreen extends ClientBlockEntityScreen<ConnectorProbeBlockEntity>
{
	public RedstoneProbeScreen(ConnectorProbeBlockEntity tileEntity, Component title)
	{
		super(tileEntity, title);
		this.xSize = 216;
		this.ySize = 80;
	}

	private GuiButtonBoolean[] colorButtonsSend;
	private GuiButtonBoolean[] colorButtonsReceive;

	@Override
	public void init()
	{
		super.init();

		clearWidgets();

		colorButtonsSend = new GuiButtonBoolean[16];
		colorButtonsReceive = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtonsSend.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtonsSend[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsSend, guiLeft+20+(i%4*14), guiTop+28+(i/4*14),
					() -> blockEntity.redstoneChannelSending==color, color, btn -> {
						sendConfig("redstoneChannelSending", color.getId());
					});
			this.addRenderableWidget(colorButtonsSend[i]);

			colorButtonsReceive[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsReceive, guiLeft+136+(i%4*14), guiTop+28+(i/4*14),
					() -> blockEntity.redstoneChannel==color, color, btn -> {
						sendConfig("redstoneChannel", color.getId());
					});
			this.addRenderableWidget(colorButtonsReceive[i]);
		}

		this.addRenderableWidget(new GuiSliderIE(guiLeft+15, guiTop, 64, Component.translatable(Lib.GUI_CONFIG+"output_threshold"),
				0, 15, this.blockEntity.outputThreshold,
				value -> sendConfig("outputThreshold", Math.round(value*15)))
		);
	}

	private void sendConfig(String key, int value)
	{
		CompoundTag message = new CompoundTag();
		message.putInt(key, value);
		ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(blockEntity, message));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		graphics.drawString(this.font, Component.translatable(Lib.GUI_CONFIG+"redstone_color_sending").getString(), guiLeft, guiTop+18, DyeColor.WHITE.getTextColor());
		graphics.drawString(this.font, Component.translatable(Lib.GUI_CONFIG+"redstone_color_receiving").getString(), guiLeft+116, guiTop+18, DyeColor.WHITE.getTextColor());

		ArrayList<Component> tooltip = new ArrayList<>();
		for(int i = 0; i < colorButtonsSend.length; i++)
			if(colorButtonsSend[i].isHoveredOrFocused()||colorButtonsReceive[i].isHoveredOrFocused())
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
}