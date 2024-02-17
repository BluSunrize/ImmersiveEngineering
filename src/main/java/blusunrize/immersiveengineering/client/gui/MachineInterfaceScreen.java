/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.CheckOption;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.IMachineInterfaceConnection;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.MachineCheckImplementation;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonDyeColor;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonSelectBox;
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.common.blocks.wooden.MachineInterfaceBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.MachineInterfaceBlockEntity.MachineInterfaceConfig;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntSupplier;

import static blusunrize.immersiveengineering.client.gui.IEContainerScreen.makeTextureLocation;

public class MachineInterfaceScreen extends ClientBlockEntityScreen<MachineInterfaceBlockEntity>
{
	public static final ResourceLocation TEXTURE = makeTextureLocation("machine_interface");
	private static final int GUI_WIDTH_LEFT = 23;
	private static final int GUI_WIDTH_MIDDLE = 16;
	private static final int GUI_WIDTH_RIGHT = 41;
	// "slotted" space available on the left + right segments
	// not pixel accurate, in order to leave some padding on the sides
	private static final int GUI_WIDTH_SLOT = 28;

	public MachineInterfaceScreen(MachineInterfaceBlockEntity blockEntity, Component title)
	{
		super(blockEntity, title);
		this.xSize = 208;
		this.ySize = 186;
	}

	private MachineCheckImplementation<?>[] availableChecks;
	private List<MachineInterfaceConfig<?>> configList;

	private final List<ConfigurationRow> rows = new ArrayList<>();
	private int scrollIndex = 0;
	private static final int MAX_SCROLL = 6;
	private static final int ROW_HEIGHT = 24;

	private int middleSegmentCount;

	@Override
	public void init()
	{
		super.init();
		clearWidgets();

		IMachineInterfaceConnection attachedMachine = blockEntity.machine.getCapability();
		if(attachedMachine!=null)
		{
			availableChecks = attachedMachine.getAvailableChecks();

			configList = new ArrayList<>(blockEntity.configurations);

			int rowIndex = 0;
			for(; rowIndex < configList.size(); rowIndex++)
			{
				ConfigurationRow row = addConfigurationRow(rowIndex);
				if(rowIndex >= MAX_SCROLL)
					row.hide();
			}

			final int guiTotalWidth;
			// if no conditions exist yet, we have to work with a dummy
			if(rows.isEmpty())
			{
				configList.add(new MachineInterfaceConfig<>(0, 0, DyeColor.WHITE));
				guiTotalWidth = addConfigurationRow(0).rowWidth();
				rows.clear();
				configList.clear();
				clearWidgets();
			}
			else
				guiTotalWidth = rows.get(0).rowWidth();

			// Update width of GUI, based on collective width of buttons
			this.middleSegmentCount = (int)Math.ceil((guiTotalWidth-GUI_WIDTH_SLOT)/(float)GUI_WIDTH_MIDDLE);
			this.xSize = GUI_WIDTH_LEFT+GUI_WIDTH_MIDDLE*middleSegmentCount+GUI_WIDTH_RIGHT;
			// recalculate guiLeft, shift existing buttons
			int newGuiLeft = (this.width-this.xSize)/2;
			int dist = newGuiLeft-this.guiLeft;
			this.guiLeft = newGuiLeft;
			this.children().forEach((Consumer<GuiEventListener>)guiEventListener -> {
				if(guiEventListener instanceof GuiButtonIE button)
					button.setX(button.getX()+dist);
			});

			this.addRenderableWidget(new GuiButtonIE(
					guiLeft+6, guiTop+162,
					72, 20, Component.translatable(Lib.GUI_CONFIG+"machine_interface.add"),
					TEXTURE, 0, 186,
					(IIEPressable<Button>)btn -> {
						final MachineInterfaceConfig<?> newConfig = new MachineInterfaceConfig<>(0, 0, DyeColor.WHITE);
						final int idx = configList.size();
						configList.add(newConfig);
						// send new config to server
						sendConfig(idx, newConfig);
						// add buttons for it
						addConfigurationRow(idx);
						int scrollsNeeded = idx-scrollIndex-MAX_SCROLL;
						for(int i = 0; i <= scrollsNeeded; i++)
							scrollDown();
					}
			));

			this.addRenderableWidget(new GuiButtonIE(
					guiLeft+xSize-20, guiTop+12,
					16, 12, Component.nullToEmpty("^"),
					TEXTURE, 72, 202,
					(IIEPressable<Button>)btn -> scrollUp()
			));

			this.addRenderableWidget(new GuiButtonIE(
					guiLeft+xSize-20, guiTop+147,
					16, 12, Component.nullToEmpty("V"),
					TEXTURE, 72, 202,
					(IIEPressable<Button>)btn -> scrollDown()
			));
		}
	}

	private void scrollDown()
	{
		if(scrollIndex+1 < rows.size())
		{
			rows.get(scrollIndex).hide();
			rows.forEach(ConfigurationRow::shiftUp);
			if(rows.size() > MAX_SCROLL+scrollIndex)
				rows.get(MAX_SCROLL+scrollIndex).show();
			scrollIndex++;
		}
	}

	private void scrollUp()
	{
		if(scrollIndex > 0)
		{
			rows.get(--scrollIndex).show();
			rows.forEach(ConfigurationRow::shiftDown);
			if(rows.size() > MAX_SCROLL+scrollIndex)
				rows.get(MAX_SCROLL+scrollIndex).hide();
		}
	}

	private ConfigurationRow addConfigurationRow(final int idx)
	{
		int yPos = guiTop+10;
		if(!rows.isEmpty())
			yPos = rows.get(rows.size()-1).buttons[0].getY()+ROW_HEIGHT;
		ConfigurationRow row = new ConfigurationRow(idx, guiLeft+10, yPos, this);
		rows.add(row);
		return row;
	}

	private void removeConfigurationRow(final int idx)
	{
		// remove from collections
		for(GuiButtonIE b : this.rows.remove(idx).buttons)
			this.removeWidget(b);
		this.configList.remove(idx);
		// shift index of remaining ones
		this.rows.forEach(row -> row.shiftIndex(idx));
		// unhide potential offscreen button
		int nextOffscreen = scrollIndex+MAX_SCROLL-1;
		if(nextOffscreen < this.rows.size())
			this.rows.get(nextOffscreen).show();
		// finally, send to server
		this.sendConfig(idx, null);
	}

	private void sendConfig(int idx, @Nullable MachineInterfaceConfig<?> config)
	{
		//update client
		if(config!=null)
			configList.set(idx, config);
		//update server
		CompoundTag message = new CompoundTag();
		message.putInt("idx", idx);
		if(config!=null)
			message.put("configuration", config.writeToNBT());
		else
			message.putBoolean("delete", true);
		PacketDistributor.SERVER.noArg().send(new MessageBlockEntitySync(blockEntity, message));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		graphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, GUI_WIDTH_LEFT, ySize);
		int offset = GUI_WIDTH_LEFT;
		for(int i = 0; i < middleSegmentCount; i++, offset += GUI_WIDTH_MIDDLE)
			graphics.blit(TEXTURE, guiLeft+offset, guiTop, GUI_WIDTH_LEFT+2, 0, GUI_WIDTH_MIDDLE, ySize);
		graphics.blit(TEXTURE, guiLeft+offset, guiTop, GUI_WIDTH_LEFT+GUI_WIDTH_MIDDLE+4, 0, GUI_WIDTH_RIGHT, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<Component> tooltip = new ArrayList<>();
		for(GuiEventListener w : children())
			if(w.isMouseOver(mouseX, mouseY)&&w instanceof ITooltipWidget ttw)
				ttw.gatherTooltip(mouseX, mouseY, tooltip);
//		for(int i = 0; i < colorButtonsSend.length; i++)
//			if(colorButtonsSend[i].isHoveredOrFocused()||colorButtonsReceive[i].isHoveredOrFocused())
//			{
//				tooltip.add(Component.translatable(Lib.GUI_CONFIG+"redstone_color"));
//				tooltip.add(TextUtils.applyFormat(
//						Component.translatable("color.minecraft."+DyeColor.byId(i).getName()),
//						ChatFormatting.GRAY
//				));
//			}

		if(!tooltip.isEmpty())
			graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
	}

	static final class ConfigurationRow
	{
		private int idx;
		private final GuiButtonIE[] buttons;

		ConfigurationRow(int i, int xPos, int yPos, MachineInterfaceScreen gui)
		{
			this.idx = i;
			IntSupplier idxGetter = () -> idx;

			GuiButtonIE trashButton = gui.addRenderableWidget(new GuiButtonIE(
					xPos, yPos, 16, 16, Component.empty(), TEXTURE, 72, 214,
					(IIEPressable<Button>)btn -> gui.removeConfigurationRow(idxGetter.getAsInt())
			));

			GuiButtonSelectBox<MachineCheckImplementation<?>> checkButton = gui.addRenderableWidget(new GuiButtonSelectBox<>(
					getFollowIngButtonX(trashButton), yPos, "checktype", gui.availableChecks, () -> gui.configList.get(idxGetter.getAsInt()).getSelectedCheck(),
					MachineCheckImplementation::getName,
					btn -> gui.sendConfig(idxGetter.getAsInt(), gui.configList.get(idxGetter.getAsInt())
							.setSelectedCheck(btn.getClickedState())
							.setSelectedOption(0) // we can't assume the number of options on the check, so reset it
					)
			));

			GuiButtonSelectBox<CheckOption<?>> optionButton = gui.addRenderableWidget(new GuiButtonSelectBox<>(
					getFollowIngButtonX(checkButton), yPos, "option", checkButton.getState().options(), () -> gui.configList.get(idxGetter.getAsInt()).getSelectedOption(),
					CheckOption::getName,
					btn -> gui.sendConfig(idxGetter.getAsInt(), gui.configList.get(idxGetter.getAsInt())
							.setSelectedOption(btn.getClickedState())
					)
			));

			GuiButtonDyeColor colorButton = gui.addRenderableWidget(new GuiButtonDyeColor(
					getFollowIngButtonX(optionButton), yPos, 16, 16,
					() -> gui.configList.get(idxGetter.getAsInt()).getOutputColor().getId(), TEXTURE, 72, 186,
					btn -> gui.sendConfig(idxGetter.getAsInt(), gui.configList.get(idxGetter.getAsInt())
							.setOutputColor(btn.getNextState())
					),
					ItemBatcherScreen::gatherRedstoneTooltip
			));

			this.buttons = new GuiButtonIE[]{
					trashButton, checkButton, optionButton, colorButton
			};
		}

		private void shiftIndex(int removed)
		{
			if(this.idx > removed)
			{
				this.idx--;
				shiftUp();
			}
		}

		private int getFollowIngButtonX(GuiButtonIE button)
		{
			return button.getX()+button.getWidth()+4;
		}

		public void shiftDown()
		{
			for(GuiButtonIE button : buttons)
				button.setY(button.getY()+ROW_HEIGHT);
		}

		public void shiftUp()
		{
			for(GuiButtonIE button : buttons)
				button.setY(button.getY()-ROW_HEIGHT);
		}

		public void hide()
		{
			for(GuiButtonIE button : buttons)
				button.visible = false;
		}

		public void show()
		{
			for(GuiButtonIE button : buttons)
				button.visible = true;
		}

		public int rowStart()
		{
			return buttons[0].getX();
		}

		public int rowEnd()
		{
			GuiButtonIE lastButton = buttons[buttons.length-1];
			return lastButton.getX()+lastButton.getWidth();
		}

		public int rowWidth()
		{
			return rowEnd()-rowStart();
		}
	}
}