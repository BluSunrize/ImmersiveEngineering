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
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonDyeColor;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity.BatchMode;
import blusunrize.immersiveengineering.common.gui.ItemBatcherContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ItemBatcherScreen extends IEContainerScreen<ItemBatcherContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("item_batcher");

	private final ItemBatcherTileEntity tile;
	private GuiButtonState<BatchMode> buttonBatchMode;
	private final GuiButtonDyeColor[] buttonsRedstone = new GuiButtonDyeColor[9];

	public ItemBatcherScreen(ItemBatcherContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		this.imageHeight = 199;
	}

	@Override
	public void init()
	{
		super.init();
		mc().keyboardHandler.setSendRepeatsToGui(true);

		this.buttons.clear();
		buttonBatchMode = new GuiButtonState<>(leftPos+7, topPos+92, 18, 18, TextComponent.EMPTY,
				ItemBatcherTileEntity.BatchMode.values(), tile.batchMode.ordinal(), TEXTURE,
				176, 36, 1,
				btn -> {
					CompoundTag tag = new CompoundTag();
					tile.batchMode = btn.getNextState();
					tag.putByte("batchMode", (byte)tile.batchMode.ordinal());
					handleButtonClick(tag);
				});
		this.addButton(buttonBatchMode);

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
					});
			this.addButton(buttonsRedstone[slot]);
		}

	}

	protected void handleButtonClick(CompoundTag nbt)
	{
		if(!nbt.isEmpty())
		{
			ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, nbt));
			this.init();
		}
	}

	@Override
	protected void renderLabels(PoseStack transform, int mouseX, int mouseY)
	{
		BlockEntity te = menu.tile;
		this.font.draw(transform, I18n.get("block.immersiveengineering.item_batcher"), 8, 6, 0x190b06);

		this.font.draw(transform, I18n.get(Lib.GUI_CONFIG+"item_batcher.filter"), 8, 20, 0xE0E0E0);
		this.font.draw(transform, I18n.get(Lib.GUI_CONFIG+"item_batcher.buffer"), 8, 49, 0xE0E0E0);
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		ArrayList<Component> tooltip = new ArrayList<>();

		if(buttonBatchMode.isHovered())
		{
			tooltip.add(new TranslatableComponent(Lib.GUI_CONFIG+"item_batcher.batchmode"));
			tooltip.add(TextUtils.applyFormat(
					new TranslatableComponent(Lib.GUI_CONFIG+"item_batcher.batchmode."+buttonBatchMode.getState().name()),
					ChatFormatting.GRAY
			));
		}

		for(GuiButtonDyeColor b : buttonsRedstone)
			if(b.isHovered())
			{
				tooltip.add(new TranslatableComponent(Lib.GUI_CONFIG+"item_batcher.redstone_color"));
				tooltip.add(TextUtils.applyFormat(
						new TranslatableComponent("color.minecraft."+b.getState().getName()),
						ChatFormatting.GRAY
				));
			}

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		// Background
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);
	}
}