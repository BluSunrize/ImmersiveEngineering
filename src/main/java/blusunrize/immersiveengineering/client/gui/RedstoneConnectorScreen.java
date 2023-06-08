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
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorRedstoneBlockEntity;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class RedstoneConnectorScreen extends ClientBlockEntityScreen<ConnectorRedstoneBlockEntity>
{
	private static final ResourceLocation TEXTURE = IEContainerScreen.makeTextureLocation("redstone_configuration");

	public RedstoneConnectorScreen(ConnectorRedstoneBlockEntity tileEntity, Component title)
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

		clearWidgets();

		buttonInOut = new GuiButtonState<>(guiLeft+41, guiTop+20, 18, 18, Component.empty(), new IOSideConfig[]{IOSideConfig.INPUT, IOSideConfig.OUTPUT},
				() -> blockEntity.ioMode.ordinal()-1, TEXTURE, 176, 0, 1,
				btn -> sendConfig("ioMode", btn.getNextState().ordinal())
		);
		this.addRenderableWidget(buttonInOut);

		colorButtons = new GuiButtonBoolean[16];
		for(int i = 0; i < colorButtons.length; i++)
		{
			final DyeColor color = DyeColor.byId(i);
			colorButtons[i] = buildColorButton(colorButtons, guiLeft+22+(i%4*14), guiTop+44+(i/4*14),
					() -> blockEntity.redstoneChannel==color, color, btn -> sendConfig("redstoneChannel", color.getId()));
			this.addRenderableWidget(colorButtons[i]);
		}
	}

	public void sendConfig(String key, int value)
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
		ArrayList<Component> tooltip = new ArrayList<>();

		if(buttonInOut.isHoveredOrFocused())
		{
			tooltip.add(Component.translatable(Lib.GUI_CONFIG+"redstone_iomode"));
			tooltip.add(TextUtils.applyFormat(
					buttonInOut.getState().getTextComponent(),
					ChatFormatting.GRAY
			));
		}

		for(int i = 0; i < colorButtons.length; i++)
			if(colorButtons[i].isHoveredOrFocused())
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

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
		if(mc().options.keyInventory.isActiveAndMatches(mouseKey))
		{
			this.onClose();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public static GuiButtonBoolean buildColorButton(
			GuiButtonBoolean[] buttons, int posX, int posY, Supplier<Boolean> active, DyeColor color, Consumer<GuiButtonBoolean> onClick
	)
	{
		return new GuiButtonBoolean(posX, posY, 12, 12, "", active,
				TEXTURE, 194, 0, 1,
				btn -> {
					if(btn.getNextState())
						onClick.accept((GuiButtonBoolean)btn);
					for(int j = 0; j < buttons.length; j++)
						if(j!=color.ordinal()&&buttons[j].getState())
							buttons[j].onClick(buttons[j].getX(), buttons[j].getY());
				})
		{
			@Override
			protected boolean isValidClickButton(int button)
			{
				return button==0&&!getState();
			}

			@Override
			public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks)
			{
				super.render(graphics, mouseX, mouseY, partialTicks);
				if(this.visible)
				{
					int col = color.getTextColor();
					if(!getState())
						col = ClientUtils.getDarkenedTextColour(col);
					col = 0xff000000|col;
					graphics.fillGradient(getX()+3, getY()+3, getX()+9, getY()+9, col, col);
				}
			}
		};
	}
}