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
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonDyeColor;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.client.utils.FakeGuiUtils;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity.BatchMode;
import blusunrize.immersiveengineering.common.gui.ItemBatcherContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ItemBatcherScreen extends IEContainerScreen<ItemBatcherContainer>
{
	ItemBatcherTileEntity tile;
	GuiButtonState<BatchMode> buttonBatchMode;
	GuiButtonDyeColor[] buttonsRedstone = new GuiButtonDyeColor[9];

	public ItemBatcherScreen(ItemBatcherContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.ySize = 199;
	}

	@Override
	public void init()
	{
		super.init();
		mc().keyboardListener.enableRepeatEvents(true);

		this.buttons.clear();
		buttonBatchMode = new GuiButtonState<>(guiLeft+7, guiTop+92, 18, 18, StringTextComponent.EMPTY,
				ItemBatcherTileEntity.BatchMode.values(), tile.batchMode.ordinal(), "immersiveengineering:textures/gui/item_batcher.png",
				176, 36, 1,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					tile.batchMode = btn.getNextState();
					tag.putByte("batchMode", (byte)tile.batchMode.ordinal());
					handleButtonClick(tag);
				});
		this.addButton(buttonBatchMode);

		for(int slot = 0; slot < 9; slot++)
		{
			int finalSlot = slot;
			buttonsRedstone[slot] = new GuiButtonDyeColor(guiLeft+12+slot*18, guiTop+77, "", tile.redstoneColors.get(slot),
					btn -> {
						CompoundNBT tag = new CompoundNBT();
						tile.redstoneColors.set(finalSlot, btn.getNextState());
						tag.putInt("redstoneColor_slot", finalSlot);
						tag.putInt("redstoneColor_val", tile.redstoneColors.get(finalSlot).getId());
						handleButtonClick(tag);
					});
			this.addButton(buttonsRedstone[slot]);
		}

	}

	protected void handleButtonClick(CompoundNBT nbt)
	{
		if(!nbt.isEmpty())
		{
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, nbt));
			this.init();
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(MatrixStack transform, int mouseX, int mouseY)
	{
		TileEntity te = container.tile;
		this.font.drawString(transform, I18n.format("block.immersiveengineering.item_batcher"), 8, 6, 0x190b06);

		this.font.drawString(transform, I18n.format(Lib.GUI_CONFIG+"item_batcher.filter"), 8, 20, 0xE0E0E0);
		this.font.drawString(transform, I18n.format(Lib.GUI_CONFIG+"item_batcher.buffer"), 8, 49, 0xE0E0E0);
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		if(buttonBatchMode.isHovered())
		{
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"item_batcher.batchmode"));
			tooltip.add(ClientUtils.applyFormat(
					new TranslationTextComponent(Lib.GUI_CONFIG+"item_batcher.batchmode."+buttonBatchMode.getState().name()),
					TextFormatting.GRAY
			));
		}

		for(GuiButtonDyeColor b : buttonsRedstone)
			if(b.isHovered())
			{
				tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"item_batcher.redstone_color"));
				tooltip.add(ClientUtils.applyFormat(
						new TranslationTextComponent("color.minecraft."+b.getState().getTranslationKey()),
						TextFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			FakeGuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture("immersiveengineering:textures/gui/item_batcher.png");
		// Background
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}