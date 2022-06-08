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
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherBlockEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherBlockEntity.BatchMode;
import blusunrize.immersiveengineering.common.gui.ItemBatcherContainer;
import blusunrize.immersiveengineering.common.network.MessageBlockEntitySync;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

import java.util.List;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ItemBatcherScreen extends IEContainerScreen<ItemBatcherContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("item_batcher");

	private final ItemBatcherBlockEntity tile;
	private GuiButtonState<BatchMode> buttonBatchMode;
	private final GuiButtonDyeColor[] buttonsRedstone = new GuiButtonDyeColor[9];

	public ItemBatcherScreen(ItemBatcherContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.tile = container.tile;
		this.imageHeight = 199;
	}

	@Override
	public void init()
	{
		super.init();
		mc().keyboardHandler.setSendRepeatsToGui(true);

		this.clearWidgets();
		buttonBatchMode = new GuiButtonState<>(leftPos+7, topPos+92, 18, 18, Component.empty(),
				ItemBatcherBlockEntity.BatchMode.values(), tile.batchMode.ordinal(), TEXTURE,
				176, 36, 1,
				btn -> {
					CompoundTag tag = new CompoundTag();
					tile.batchMode = btn.getNextState();
					tag.putByte("batchMode", (byte)tile.batchMode.ordinal());
					handleButtonClick(tag);
				}, ItemBatcherScreen::gatherBatchmodeTooltip);
		this.addRenderableWidget(buttonBatchMode);

		for(int slot = 0; slot < 9; slot++)
		{
			int finalSlot = slot;
			buttonsRedstone[slot] = new GuiButtonDyeColor(leftPos+12+slot*18, topPos+77, "", tile.redstoneColors.get(slot),
					btn -> {
						CompoundTag tag = new CompoundTag();
						tile.redstoneColors.set(finalSlot, btn.getNextState());
						tag.putInt("redstoneColor_slot", finalSlot);
						tag.putInt("redstoneColor_val", tile.redstoneColors.get(finalSlot).getId());
						handleButtonClick(tag);
					}, ItemBatcherScreen::gatherRedstoneTooltip);
			this.addRenderableWidget(buttonsRedstone[slot]);
		}

	}

	protected void handleButtonClick(CompoundTag nbt)
	{
		if(!nbt.isEmpty())
		{
			ImmersiveEngineering.packetHandler.sendToServer(new MessageBlockEntitySync(tile, nbt));
			this.init();
		}
	}

	@Override
	protected void renderLabels(PoseStack transform, int mouseX, int mouseY)
	{
		this.font.draw(transform, I18n.get("block.immersiveengineering.item_batcher"), 8, 6, 0x190b06);

		this.font.draw(transform, I18n.get(Lib.GUI_CONFIG+"item_batcher.filter"), 8, 20, 0xE0E0E0);
		this.font.draw(transform, I18n.get(Lib.GUI_CONFIG+"item_batcher.buffer"), 8, 49, 0xE0E0E0);
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