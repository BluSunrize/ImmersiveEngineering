/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorRedstoneTileEntity;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class RedstoneConnectorScreen extends ClientTileScreen<ConnectorRedstoneTileEntity>
{
	public RedstoneConnectorScreen(ConnectorRedstoneTileEntity tileEntity, ITextComponent title)
	{
		super(tileEntity, title);
		this.xSize = 100;
		this.ySize = 120;
	}

	private GuiButtonState<IOSideConfig> buttonInOut;
	private GuiButtonBoolean[] colorButtons;

	@Override
	public void init()
	{
		super.init();
		mc().keyboardListener.enableRepeatEvents(true);

		this.buttons.clear();

		buttonInOut = new GuiButtonState<>(guiLeft+41, guiTop+20, 18, 18, "", new IOSideConfig[]{IOSideConfig.INPUT, IOSideConfig.OUTPUT},
				tileEntity.ioMode.ordinal()-1, "immersiveengineering:textures/gui/redstone_configuration.png", 176, 0, 1,
				btn -> {
					tileEntity.ioMode = btn.getNextState();
				});
		this.addButton(buttonInOut);

		colorButtons = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtons.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtons[i] = buildColorButton(colorButtons, guiLeft+22+(i%4*14), guiTop+44+(i/4*14),
					tileEntity.redstoneChannel.ordinal()==i, color, btn -> {
						tileEntity.redstoneChannel = color;
					});
			this.addButton(colorButtons[i]);
		}
	}

	@Override
	public void onClose()
	{
		super.onClose();

		CompoundNBT message = new CompoundNBT();
		message.putInt("ioMode", tileEntity.ioMode.ordinal());
		message.putInt("redstoneChannel", tileEntity.redstoneChannel.getId());
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tileEntity, message));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(int mouseX, int mouseY, float partialTick)
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY, float partialTick)
	{
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonInOut.isHovered())
		{
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_iomode"));
			tooltip.add(buttonInOut.getState().getTextComponent().setStyle(new Style().setColor(TextFormatting.GRAY)));
		}

		for(int i = 0; i < colorButtons.length; i++)
			if(colorButtons[i].isHovered())
			{
				tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_color"));
				tooltip.add(new TranslationTextComponent("color.minecraft."+DyeColor.byId(i).getTranslationKey())
						.setStyle(new Style().setColor(TextFormatting.GRAY)));
			}

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mouseX, mouseY, font, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	public static GuiButtonBoolean buildColorButton(GuiButtonBoolean[] buttons, int posX, int posY, boolean active, DyeColor color, Consumer<GuiButtonBoolean> onClick)
	{
		return new GuiButtonBoolean(posX, posY, 12, 12, "", active,
				"immersiveengineering:textures/gui/redstone_configuration.png", 194, 0, 1,
				btn -> {
					if(btn.getNextState())
						onClick.accept((GuiButtonBoolean)btn);
					for(int j = 0; j < buttons.length; j++)
						if(j!=color.ordinal())
							buttons[j].setStateByInt(0);
				})
		{
			@Override
			public void render(int mouseX, int mouseY, float partialTicks)
			{
				super.render(mouseX, mouseY, partialTicks);
				if(this.visible)
				{
					int col = color.colorValue;
					if(!getState())
						col = ClientUtils.getDarkenedTextColour(col);
					col = 0xff000000|col;
					this.fillGradient(x+3, y+3, x+9, y+9, col, col);
				}
			}
		};
	}
}