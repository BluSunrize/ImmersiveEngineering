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
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.utils.FakeGuiUtils;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorProbeTileEntity;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class RedstoneProbeScreen extends ClientTileScreen<ConnectorProbeTileEntity>
{
	public RedstoneProbeScreen(ConnectorProbeTileEntity tileEntity, ITextComponent title)
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
		mc().keyboardListener.enableRepeatEvents(true);

		this.buttons.clear();

		colorButtonsSend = new GuiButtonBoolean[16];
		colorButtonsReceive = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtonsSend.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtonsSend[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsSend, guiLeft+20+(i%4*14), guiTop+10+(i/4*14),
					tileEntity.redstoneChannelSending.ordinal()==i, color, btn -> {
						sendConfig("redstoneChannelSending", color);
					});
			this.addButton(colorButtonsSend[i]);

			colorButtonsReceive[i] = RedstoneConnectorScreen.buildColorButton(colorButtonsReceive, guiLeft+136+(i%4*14), guiTop+10+(i/4*14),
					tileEntity.redstoneChannel.ordinal()==i, color, btn -> {
						sendConfig("redstoneChannel", color);
					});
			this.addButton(colorButtonsReceive[i]);
		}
	}

	private void sendConfig(String key, DyeColor color)
	{
		CompoundNBT message = new CompoundNBT();
		message.putInt(key, color.getId());
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tileEntity, message));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{
		this.font.drawString(transform, new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_color_sending").getString(), guiLeft, guiTop, DyeColor.WHITE.getTextColor());
		this.font.drawString(transform, new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_color_receiving").getString(), guiLeft+116, guiTop, DyeColor.WHITE.getTextColor());

		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		for(int i = 0; i < colorButtonsSend.length; i++)
			if(colorButtonsSend[i].isHovered()||colorButtonsReceive[i].isHovered())
			{
				tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_color"));
				tooltip.add(ClientUtils.applyFormat(
						new TranslationTextComponent("color.minecraft."+DyeColor.byId(i).getTranslationKey()),
						TextFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			FakeGuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
	}
}