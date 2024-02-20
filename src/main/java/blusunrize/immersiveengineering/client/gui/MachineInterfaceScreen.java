/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.CheckOption;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.IMachineInterfaceConnection;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.MachineCheckImplementation;
import blusunrize.immersiveengineering.client.gui.elements.*;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import blusunrize.immersiveengineering.common.blocks.wooden.MachineInterfaceBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.MachineInterfaceBlockEntity.MachineInterfaceConfig;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.IntSupplier;
import java.util.function.ToIntFunction;

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
		this.xSize = 144;
		this.ySize = 186;
	}

	private MachineCheckImplementation<?>[] availableChecks;
	private List<MachineInterfaceConfig<?>> configList;
	private final static MachineInterfaceConfig<?> FALLBACK_CONFIG = new MachineInterfaceConfig<>(0, 0, DyeColor.WHITE);

	private WidgetRowList<?> rowList;

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
			// collect checks
			this.availableChecks = attachedMachine.getAvailableChecks();

			// make sure the fallback is the widest row it can be
			int recentMax = 0;
			int highestIndex = 0;
			for(int j = 0; j < this.availableChecks.length; j++)
			{
				int length = this.availableChecks[j].getName().getString().length()
						+Arrays.stream(this.availableChecks[j].options()).mapToInt((ToIntFunction<CheckOption<?>>)value -> value.getName().getString().length()).max().orElse(0);
				if(length > recentMax)
				{
					recentMax = length;
					highestIndex = j;
				}
			}
			FALLBACK_CONFIG.setSelectedCheck(highestIndex);

			// initialize list of rows
			this.rowList = new WidgetRowList<>(guiLeft+10, guiTop+10, ROW_HEIGHT, MAX_SCROLL,
					(x, y, idx) -> new GuiButtonDelete(
							x, y, btn -> removeConfigurationRow(idx.getAsInt())
					),
					(x, y, idx) -> new GuiButtonDyeColor(
							x+4, y, 16, 16,
							() -> getConfigSafe(idx).getOutputColor().getId(), TEXTURE, 192, 18,
							btn -> sendConfig(idx.getAsInt(), getConfigSafe(idx)
									.setOutputColor(btn.getNextState())
							),
							ItemBatcherScreen::gatherRedstoneTooltip
					),
					(x, y, idx) -> new GuiSelectBox<>(
							x+4, y, 100, () -> availableChecks, () -> getConfigSafe(idx).getSelectedCheck(),
							MachineCheckImplementation::getName,
							btn -> {
								sendConfig(idx.getAsInt(), getConfigSafe(idx)
										.setSelectedCheck(btn.getClickedState())
										.setSelectedOption(0) // we can't assume the number of options on the check, so reset it
								);
								if(this.renderables.get(this.renderables.indexOf(btn)+1) instanceof GuiSelectBox<?> optionButton)
									optionButton.recalculateOptionsAndSize();
							}
					),
					(x, y, idx) -> new GuiSelectBox<>(
							x+4, y, 110, () -> availableChecks[getConfigSafe(idx).getSelectedCheck()].options(),
							() -> getConfigSafe(idx).getSelectedOption(),
							CheckOption::getName,
							btn -> sendConfig(idx.getAsInt(), getConfigSafe(idx)
									.setSelectedOption(btn.getClickedState())
							)
					)
			);

			// fetch data from block entity & populate rows
			this.configList = new ArrayList<>(blockEntity.configurations);
			this.configList.forEach(c -> this.rowList.addRow(this::addRenderableWidget));

			// update width of GUI, based on collective width of buttons
			this.middleSegmentCount = (int)Math.ceil((rowList.getRowWidth()-GUI_WIDTH_SLOT)/(float)GUI_WIDTH_MIDDLE);
			this.xSize = GUI_WIDTH_LEFT+GUI_WIDTH_MIDDLE*middleSegmentCount+GUI_WIDTH_RIGHT;
			// recalculate guiLeft, shift existing buttons
			int newGuiLeft = (this.width-this.xSize)/2;
			int dist = newGuiLeft-this.guiLeft;
			this.guiLeft = newGuiLeft;
			this.rowList.setXPos(guiLeft+10);
			this.renderables.forEach(elem -> {
				if(elem instanceof AbstractWidget widget)
					widget.setX(widget.getX()+dist);
			});

			// add button
			this.addRenderableWidget(new GuiButtonIE(
					guiLeft+6, guiTop+162,
					72, 18, Component.translatable(Lib.GUI_CONFIG+"machine_interface.add"),
					TEXTURE, 184, 0,
					(IIEPressable<Button>)btn -> {
						final MachineInterfaceConfig<?> newConfig = new MachineInterfaceConfig<>(0, 0, DyeColor.WHITE);
						final int idx = configList.size();
						configList.add(newConfig);
						// send new config to server
						sendConfig(idx, newConfig);
						// add buttons for it and scoll down
						this.rowList.scrollTo(this.rowList.addRow(this::addRenderableWidget).getRowIndex());
					}
			));

			this.addRenderableWidget(new GuiButtonIE(
					guiLeft+xSize-20, guiTop+12, 16, 12, Component.empty(), TEXTURE, 224, 18,
					(IIEPressable<Button>)btn -> this.rowList.scrollUp()
			).setHoverOffset(16, 0));

			this.addRenderableWidget(new GuiButtonIE(
					guiLeft+xSize-20, guiTop+147, 16, 12, Component.empty(), TEXTURE, 224, 30,
					(IIEPressable<Button>)btn -> this.rowList.scrollDown()
			).setHoverOffset(16, 0));
		}
	}

	private MachineInterfaceConfig<?> getConfigSafe(IntSupplier supp)
	{
		int i = supp.getAsInt();
		if(this.configList==null||i >= this.configList.size())
			return FALLBACK_CONFIG;
		return this.configList.get(i);
	}


	private void removeConfigurationRow(final int idx)
	{
		// remove from collections
		for(AbstractWidget widget : this.rowList.removeRow(idx))
			this.removeWidget(widget);
		this.configList.remove(idx);
		// send to server
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
		if(availableChecks==null)
			graphics.blit(TEXTURE, guiLeft, guiTop+(ySize-74)/2, 112, 80, xSize, 106);
		else
		{
			graphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, GUI_WIDTH_LEFT, ySize);
			int offset = GUI_WIDTH_LEFT;
			for(int i = 0; i < middleSegmentCount; i++, offset += GUI_WIDTH_MIDDLE)
				graphics.blit(TEXTURE, guiLeft+offset, guiTop, GUI_WIDTH_LEFT+2, 0, GUI_WIDTH_MIDDLE, ySize);
			graphics.blit(TEXTURE, guiLeft+offset, guiTop, GUI_WIDTH_LEFT+GUI_WIDTH_MIDDLE+4, 0, GUI_WIDTH_RIGHT, ySize);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		if(availableChecks==null)
		{
			Component c = Component.translatable(Lib.GUI_CONFIG+"machine_interface.not_connected");
			List<FormattedCharSequence> text = font.split(c, 122);
			int yPos = guiTop+(ySize-74)/2+14;
			for(int i = 0; i < text.size(); i++)
				graphics.drawString(font, text.get(i),
						guiLeft+12, yPos+i*font.lineHeight, 0x555555, false
				);
		}

		ArrayList<Component> tooltip = new ArrayList<>();
		for(GuiEventListener w : children())
			if(w.isMouseOver(mouseX, mouseY)&&w instanceof ITooltipWidget ttw)
				ttw.gatherTooltip(mouseX, mouseY, tooltip);

		if(!tooltip.isEmpty())
			graphics.renderTooltip(font, tooltip, Optional.empty(), mouseX, mouseY);
	}

	private static class GuiButtonDelete extends GuiButtonIE implements ITooltipWidget
	{
		public GuiButtonDelete(int x, int y, IIEPressable<?> handler)
		{
			super(x, y, 16, 16, Component.empty(), TEXTURE, 208, 18, handler);
			this.setHoverOffset(0, 16);
		}

		@Override
		public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
		{
			tooltip.add(Component.translatable(Lib.GUI_CONFIG+"machine_interface.remove"));
		}
	}
}