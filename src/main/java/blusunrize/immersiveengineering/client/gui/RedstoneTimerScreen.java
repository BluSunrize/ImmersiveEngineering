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
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonCheckbox;
import blusunrize.immersiveengineering.client.gui.elements_old.GuiButtonBooleanOld;
import blusunrize.immersiveengineering.client.gui.elements_old.GuiSliderIEOld;
import blusunrize.immersiveengineering.common.blocks.metal.RedstoneTimerBlockEntity;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Optional;

public class RedstoneTimerScreen extends ClientBlockEntityScreen<RedstoneTimerBlockEntity>
{
	public RedstoneTimerScreen(RedstoneTimerBlockEntity tileEntity, Component title)
	{
		super(tileEntity, title);
		this.xSize = 216;
		this.ySize = 80;
	}

	private GuiButtonBooleanOld[] colorButtonsOutput;
	private GuiButtonBooleanOld[] colorButtonsControl;

	@Override
	public void init()
	{
		super.init();

		clearWidgets();

		colorButtonsOutput = new GuiButtonBooleanOld[16];
		colorButtonsControl = new GuiButtonBooleanOld[16];
		for(int i = 0; i < colorButtonsOutput.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtonsOutput[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsOutput, guiLeft+20+(i%4*14), guiTop+28+(i/4*14),
					() -> blockEntity.redstoneChannel==color, color, btn -> {
						sendConfig("redstoneChannel", color.getId());
					});
			this.addRenderableWidget(colorButtonsOutput[i]);

			colorButtonsControl[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsControl, guiLeft+136+(i%4*14), guiTop+28+(i/4*14),
					() -> blockEntity.redstoneChannelControl==color, color, btn -> {
						sendConfig("redstoneChannelControl", color.getId());
					});
			this.addRenderableWidget(colorButtonsControl[i]);
		}

		this.addRenderableWidget(new GuiButtonCheckbox(guiLeft+106, guiTop+84, Component.translatable(Lib.GUI_CONFIG+"redstone_require_control_signal"),
				() -> blockEntity.requireControlSignal,
				btn -> sendConfig("requireControlSignal", btn.getNextState())));

		this.addRenderableWidget(new TimerSlider(
				guiLeft+15, guiTop, 176,
				RedstoneTimerBlockEntity.TIMER_MIN, RedstoneTimerBlockEntity.TIMER_MAX, this.blockEntity.timerSetting,
				value -> sendConfig(
						"timerSetting",
						RedstoneTimerBlockEntity.TIMER_MIN+Math.round(value*(RedstoneTimerBlockEntity.TIMER_MAX-RedstoneTimerBlockEntity.TIMER_MIN))
				)
		));
	}

	private void sendConfig(String key, int value)
	{
		CompoundTag message = new CompoundTag();
		message.putInt(key, value);
		PacketDistributor.sendToServer(new MessageBlockEntitySync(blockEntity, message));
	}

	private void sendConfig(String key, boolean value)
	{
		CompoundTag message = new CompoundTag();
		message.putBoolean(key, value);
		PacketDistributor.sendToServer(new MessageBlockEntitySync(blockEntity, message));
	}

	@Override
	protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		graphics.drawString(this.font, Component.translatable(Lib.GUI_CONFIG+"redstone_color_output").getString(), guiLeft, guiTop+18, DyeColor.WHITE.getTextColor());
		graphics.drawString(this.font, Component.translatable(Lib.GUI_CONFIG+"redstone_color_control").getString(), guiLeft+116, guiTop+18, DyeColor.WHITE.getTextColor());
		ArrayList<Component> tooltip = new ArrayList<>();
		for(int i = 0; i < colorButtonsOutput.length; i++)
			if(colorButtonsOutput[i].isHovered()||colorButtonsControl[i].isHovered())
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

	// TODO update
	private static class TimerSlider extends GuiSliderIEOld
	{
		public TimerSlider(int x, int y, int width, int min, int max, int value, FloatConsumer handler)
		{
			super(x, y, width, Component.empty(), min, max, value, handler);
		}

		@Override
		protected void updateMessage()
		{
			this.setMessage(RedstoneTimerBlockEntity.getTimeFormatted(this.getValueInt()));
		}
	}
}