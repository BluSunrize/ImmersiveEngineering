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
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonDyeColor;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity.BatchMode;
import blusunrize.immersiveengineering.common.gui.ItemBatcherContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.DyeColor;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ItemBatcherScreen extends IEContainerScreen<ItemBatcherContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("item_batcher");

	private final ItemBatcherTileEntity tile;
	private GuiButtonState<BatchMode> buttonBatchMode;
	private final GuiButtonDyeColor[] buttonsRedstone = new GuiButtonDyeColor[9];

	public ItemBatcherScreen(ItemBatcherContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
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
				ItemBatcherTileEntity.BatchMode.values(), tile.batchMode.ordinal(), TEXTURE,
				176, 36, 1,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					tile.batchMode = btn.getNextState();
					tag.putByte("batchMode", (byte)tile.batchMode.ordinal());
					handleButtonClick(tag);
				}, ItemBatcherScreen::gatherBatchmodeTooltip);
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
					}, ItemBatcherScreen::gatherRedstoneTooltip);
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
		this.font.drawString(transform, I18n.format("block.immersiveengineering.item_batcher"), 8, 6, 0x190b06);

		this.font.drawString(transform, I18n.format(Lib.GUI_CONFIG+"item_batcher.filter"), 8, 20, 0xE0E0E0);
		this.font.drawString(transform, I18n.format(Lib.GUI_CONFIG+"item_batcher.buffer"), 8, 49, 0xE0E0E0);
	}

	private static void gatherBatchmodeTooltip(List<ITextComponent> out, BatchMode mode) {
		out.add(new TranslationTextComponent(Lib.GUI_CONFIG+"item_batcher.batchmode"));
		out.add(TextUtils.applyFormat(
				new TranslationTextComponent(Lib.GUI_CONFIG+"item_batcher.batchmode."+mode.name()),
				TextFormatting.GRAY
		));
	}

	private static void gatherRedstoneTooltip(List<ITextComponent> out, DyeColor color) {
		out.add(new TranslationTextComponent(Lib.GUI_CONFIG+"item_batcher.redstone_color"));
		out.add(TextUtils.applyFormat(
				new TranslationTextComponent("color.minecraft."+color.getTranslationKey()),
				TextFormatting.GRAY
		));
	}
}