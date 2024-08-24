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
import blusunrize.immersiveengineering.client.gui.elements_old.GuiButtonBooleanOld;
import blusunrize.immersiveengineering.common.blocks.metal.RedstoneStateCellBlockEntity;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Optional;

public class RedstoneStateCellScreen extends ClientBlockEntityScreen<RedstoneStateCellBlockEntity>
{
	public RedstoneStateCellScreen(RedstoneStateCellBlockEntity tileEntity, Component title)
	{
		super(tileEntity, title);
		this.xSize = 216;
		this.ySize = 80;
	}

	private GuiButtonBooleanOld[] colorButtonsSet;
	private GuiButtonBooleanOld[] colorButtonsReset;
	private GuiButtonBooleanOld[] colorButtonsOut;

	@Override
	public void init()
	{
		super.init();

		clearWidgets();

		colorButtonsSet = new GuiButtonBooleanOld[16];
		colorButtonsReset = new GuiButtonBooleanOld[16];
		colorButtonsOut = new GuiButtonBooleanOld[16];
		for(int i = 0; i < colorButtonsSet.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtonsSet[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsSet, guiLeft+(i%4*14), guiTop+12+(i/4*14),
					() -> blockEntity.redstoneChannelSet==color, color, btn -> {
						sendConfig("redstoneChannelSet", color.getId());
					});
			this.addRenderableWidget(colorButtonsSet[i]);

			colorButtonsReset[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsReset, guiLeft+120+(i%4*14), guiTop+12+(i/4*14),
					() -> blockEntity.redstoneChannelReset==color, color, btn -> {
						sendConfig("redstoneChannelReset", color.getId());
					});
			this.addRenderableWidget(colorButtonsReset[i]);

			colorButtonsOut[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsOut, guiLeft+60+(i%4*14), guiTop+88+(i/4*14),
					() -> blockEntity.redstoneChannel==color, color, btn -> {
						sendConfig("redstoneChannel", color.getId());
					});
			this.addRenderableWidget(colorButtonsOut[i]);
		}
	}

	private void sendConfig(String key, int value)
	{
		CompoundTag message = new CompoundTag();
		message.putInt(key, value);
		PacketDistributor.sendToServer(new MessageBlockEntitySync(blockEntity, message));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		graphics.drawCenteredString(this.font, Component.translatable(Lib.GUI_CONFIG+"redstone_color_set").getString(), guiLeft+20, guiTop, DyeColor.WHITE.getTextColor());
		graphics.drawCenteredString(this.font, Component.translatable(Lib.GUI_CONFIG+"redstone_color_reset").getString(), guiLeft+156, guiTop, DyeColor.WHITE.getTextColor());
		graphics.drawCenteredString(this.font, Component.translatable(Lib.GUI_CONFIG+"redstone_color_output").getString(), guiLeft+88, guiTop+76, DyeColor.WHITE.getTextColor());

		ArrayList<Component> tooltip = new ArrayList<>();
		for(int i = 0; i < colorButtonsSet.length; i++)
			if(colorButtonsSet[i].isHovered()||colorButtonsReset[i].isHovered()||colorButtonsOut[i].isHovered())
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