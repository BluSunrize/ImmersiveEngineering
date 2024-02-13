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
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.ConditionOption;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.IMachineInterfaceConnection;
import blusunrize.immersiveengineering.api.tool.MachineInterfaceHandler.MachineCheckImplementation;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonDyeColor;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonSelectBox;
import blusunrize.immersiveengineering.common.blocks.wooden.MachineInterfaceBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.MachineInterfaceBlockEntity.MachineInterfaceConfig;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static blusunrize.immersiveengineering.client.gui.IEContainerScreen.makeTextureLocation;

public class MachineInterfaceScreen extends ClientBlockEntityScreen<MachineInterfaceBlockEntity>
{
	public static final ResourceLocation TEXTURE = makeTextureLocation("machine_interface");

	public MachineInterfaceScreen(MachineInterfaceBlockEntity blockEntity, Component title)
	{
		super(blockEntity, title);
		this.xSize = 208;
		this.ySize = 186;
	}

	private MachineCheckImplementation<?>[] availableChecks;

	private List<MachineInterfaceConfig<?>> configList = new ArrayList<>();
	//	private List<GuiButtonCheckOption> optionButtons;
	private GuiButtonIE addButton;

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
				addConfigurationRow(rowIndex);

			addButton = new GuiButtonIE(
					guiLeft+6, guiTop+162,
					56, 20, Component.translatable(Lib.GUI_CONFIG+"machine_interface.add"),
					TEXTURE, 0, 186,
					(IIEPressable<Button>)btn -> {
						final MachineInterfaceConfig<?> newConfig = new MachineInterfaceConfig<>(0, 0, DyeColor.WHITE);
						final int idx = configList.size();
						configList.add(newConfig);
						// send new config to server
						sendConfig(idx, newConfig);
						// add buttons for it
						addConfigurationRow(idx);
						// shift this button down
						addButton.setY(addButton.getY()+24);
					}
			);
			this.addRenderableWidget(addButton);
		}
	}

	private void addConfigurationRow(final int idx)
	{
		GuiButtonSelectBox<MachineCheckImplementation<?>> checkButton = new GuiButtonSelectBox<>(
				guiLeft+10, guiTop+8+idx*24, "checktype", availableChecks, () -> configList.get(idx).selectedCheck(),
				MachineCheckImplementation::getName,
				btn -> sendConfig(idx, new MachineInterfaceConfig<>(btn.getClickedState(), 0, configList.get(idx).outputColor()))
		);
		this.addRenderableWidget(checkButton);

		GuiButtonSelectBox<ConditionOption<?>> optionButton = new GuiButtonSelectBox<>(
				guiLeft+14+checkButton.getWidth(), guiTop+8+idx*24, "option", checkButton.getState().options(), () -> configList.get(idx).selectedOption(),
				ConditionOption::getName,
				btn -> sendConfig(idx, new MachineInterfaceConfig<>(configList.get(idx).selectedCheck(), btn.getClickedState(), configList.get(idx).outputColor()))
		);
		this.addRenderableWidget(optionButton);

		GuiButtonDyeColor dyeButton = new GuiButtonDyeColor(
				guiLeft+18+checkButton.getWidth()+optionButton.getWidth(), guiTop+8+idx*24, null, () -> configList.get(idx).outputColor().getId(),
				btn -> sendConfig(idx, new MachineInterfaceConfig<>(configList.get(idx).selectedCheck(), configList.get(idx).selectedOption(), btn.getNextState())),
				(components, dyeColor) -> {
				}
		);
		this.addRenderableWidget(dyeButton);
	}

	private void sendConfig(int idx, MachineInterfaceConfig<?> config)
	{
		//update client
		configList.set(idx, config);
		//update server
		CompoundTag message = new CompoundTag();
		message.putInt("idx", idx);
		message.put("configuration", config.writeToNBT());
		PacketDistributor.SERVER.noArg().send(new MessageBlockEntitySync(blockEntity, message));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		graphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<Component> tooltip = new ArrayList<>();
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

	record ConfigurationRow()
	{

	}
}