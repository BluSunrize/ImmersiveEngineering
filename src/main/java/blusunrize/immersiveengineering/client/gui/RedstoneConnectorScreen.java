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
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonBoolean;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorRedstoneTileEntity;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.function.Consumer;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class RedstoneConnectorScreen extends ClientTileScreen<ConnectorRedstoneTileEntity>
{
	private static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("redstone_configuration");

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

		buttonInOut = new GuiButtonState<>(guiLeft+41, guiTop+20, 18, 18, StringTextComponent.EMPTY, new IOSideConfig[]{IOSideConfig.INPUT, IOSideConfig.OUTPUT},
				tileEntity.ioMode.ordinal()-1, TEXTURE, 176, 0, 1,
				btn -> sendConfig("ioMode", btn.getNextState().ordinal())
		);
		this.addButton(buttonInOut);

		colorButtons = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtons.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtons[i] = buildColorButton(colorButtons, guiLeft+22+(i%4*14), guiTop+44+(i/4*14),
					tileEntity.redstoneChannel.ordinal()==i, color, btn -> sendConfig("redstoneChannel", color.getId()));
			this.addButton(colorButtons[i]);
		}
	}

	public void sendConfig(String key, int value)
	{
		CompoundNBT message = new CompoundNBT();
		message.putInt(key, value);
		ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tileEntity, message));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{

	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY, float partialTick)
	{
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonInOut.isHovered())
		{
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_iomode"));
			tooltip.add(TextUtils.applyFormat(
					buttonInOut.getState().getTextComponent(),
					TextFormatting.GRAY
			));
		}

		for(int i = 0; i < colorButtons.length; i++)
			if(colorButtons[i].isHovered())
			{
				tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"redstone_color"));
				tooltip.add(TextUtils.applyFormat(
						new TranslationTextComponent("color.minecraft."+DyeColor.byId(i).getTranslationKey()),
						TextFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mouseX, mouseY, width, height, -1, font);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		InputMappings.Input mouseKey = InputMappings.getInputByCode(keyCode, scanCode);
		if(mc().gameSettings.keyBindInventory.isActiveAndMatches(mouseKey))
		{
			this.closeScreen();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public static GuiButtonBoolean buildColorButton(GuiButtonBoolean[] buttons, int posX, int posY, boolean active, DyeColor color, Consumer<GuiButtonBoolean> onClick)
	{
		return new GuiButtonBoolean(posX, posY, 12, 12, "", active,
				TEXTURE, 194, 0, 1,
				btn -> {
					if(btn.getNextState())
						onClick.accept((GuiButtonBoolean)btn);
					for(int j = 0; j < buttons.length; j++)
						if(j!=color.ordinal())
							buttons[j].setStateByInt(0);
				})
		{
			@Override
			protected boolean isValidClickButton(int button)
			{
				return button==0&&!getState();
			}

			@Override
			public void render(MatrixStack transform, int mouseX, int mouseY, float partialTicks)
			{
				super.render(transform, mouseX, mouseY, partialTicks);
				if(this.visible)
				{
					int col = color.getColorValue();
					if(!getState())
						col = ClientUtils.getDarkenedTextColour(col);
					col = 0xff000000|col;
					this.fillGradient(transform, x+3, y+3, x+9, y+9, col, col);
				}
			}
		};
	}
}