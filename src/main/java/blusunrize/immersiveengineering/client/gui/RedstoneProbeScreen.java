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
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorProbeBlockEntity;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.Optional;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

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
		mc().keyboardHandler.setSendRepeatsToGui(true);

		clearWidgets();

		colorButtonsSend = new GuiButtonBoolean[16];
		colorButtonsReceive = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtonsSend.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtonsSend[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsSend, guiLeft+20+(i%4*14), guiTop+10+(i/4*14),
					blockEntity.redstoneChannelSending.ordinal()==i, color, btn -> {
						sendConfig("redstoneChannelSending", color);
					});
			this.addRenderableWidget(colorButtonsSend[i]);

			colorButtonsReceive[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsReceive, guiLeft+136+(i%4*14), guiTop+10+(i/4*14),
					blockEntity.redstoneChannel.ordinal()==i, color, btn -> {
						sendConfig("redstoneChannel", color);
					});
			this.addRenderableWidget(colorButtonsReceive[i]);
		}
	}

	private void sendConfig(String key, DyeColor color)
	{
		CompoundTag message = new CompoundTag();
		message.putInt(key, color.getId());
		ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(blockEntity, message));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(PoseStack transform, int mouseX, int mouseY, float partialTick)
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(PoseStack transform, int mouseX, int mouseY, float partialTick)
	{
		this.font.draw(transform, new TranslatableComponent(Lib.GUI_CONFIG+"redstone_color_sending").getString(), guiLeft, guiTop, DyeColor.WHITE.getTextColor());
		this.font.draw(transform, new TranslatableComponent(Lib.GUI_CONFIG+"redstone_color_receiving").getString(), guiLeft+116, guiTop, DyeColor.WHITE.getTextColor());

		ArrayList<Component> tooltip = new ArrayList<>();
		for(int i = 0; i < colorButtonsSend.length; i++)
			if(colorButtonsSend[i].isHoveredOrFocused()||colorButtonsReceive[i].isHoveredOrFocused())
			{
				tooltip.add(new TranslatableComponent(Lib.GUI_CONFIG+"redstone_color"));
				tooltip.add(TextUtils.applyFormat(
						new TranslatableComponent("color.minecraft."+DyeColor.byId(i).getName()),
						ChatFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			renderTooltip(transform, tooltip, Optional.empty(), mouseX, mouseY);
	}
}