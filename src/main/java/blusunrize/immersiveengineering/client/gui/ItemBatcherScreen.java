/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonDyeColor;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherBlockEntity.BatchMode;
import blusunrize.immersiveengineering.common.gui.ItemBatcherMenu;
import blusunrize.immersiveengineering.common.gui.sync.GetterAndSetter;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

import java.util.List;

public class ItemBatcherScreen extends IEContainerScreen<ItemBatcherMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("item_batcher");

	public ItemBatcherScreen(ItemBatcherMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageHeight = 199;
	}

	@Override
	public void init()
	{
		super.init();

		this.clearWidgets();
		this.addRenderableWidget(new GuiButtonState<>(leftPos+7, topPos+92, 18, 18, Component.empty(),
				BatchMode.values(), menu.batchMode::get, TEXTURE,
				176, 36, 1,
				btn -> {
					CompoundTag tag = new CompoundTag();
					final int newMode = btn.getNextState().ordinal();
					tag.putByte("batchMode", (byte)newMode);
					handleButtonClick(tag);
				}, ItemBatcherScreen::gatherBatchmodeTooltip));

		for(int slot = 0; slot < 9; slot++)
		{
			final int finalSlot = slot;
			final GetterAndSetter<Integer> color = menu.colors.get(slot);
			this.addRenderableWidget(new GuiButtonDyeColor(
					leftPos+12+slot*18, topPos+77, "", color::get, btn -> {
				CompoundTag tag = new CompoundTag();
				final int newState = btn.getNextState().getId();
				tag.putInt("redstoneColor_slot", finalSlot);
				tag.putInt("redstoneColor_val", newState);
				handleButtonClick(tag);
			}, ItemBatcherScreen::gatherRedstoneTooltip));
		}
	}

	protected void handleButtonClick(CompoundTag nbt)
	{
		if(!nbt.isEmpty())
		{
			sendUpdateToServer(nbt);
			this.init();
		}
	}

	@Override
	protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
	{
		graphics.drawString(this.font, I18n.get("block.immersiveengineering.item_batcher"), 8, 6,  Lib.COLOUR_I_ImmersiveOrange, true);

		graphics.drawString(this.font, I18n.get(Lib.GUI_CONFIG+"item_batcher.filter"), 8, 20, 0xE0E0E0);
		graphics.drawString(this.font, I18n.get(Lib.GUI_CONFIG+"item_batcher.buffer"), 8, 49, 0xE0E0E0);
	}

	private static void gatherBatchmodeTooltip(List<Component> out, BatchMode mode) {
		out.add(Component.translatable(Lib.GUI_CONFIG+"item_batcher.batchmode"));
		out.add(TextUtils.applyFormat(
				Component.translatable(Lib.GUI_CONFIG+"item_batcher.batchmode."+mode.name()),
				ChatFormatting.GRAY
		));
	}

	private static void gatherRedstoneTooltip(List<Component> out, DyeColor color) {
		out.add(Component.translatable(Lib.GUI_CONFIG+"item_batcher.redstone_color"));
		out.add(TextUtils.applyFormat(
				Component.translatable("color.minecraft."+color.getName()),
				ChatFormatting.GRAY
		));
	}
}