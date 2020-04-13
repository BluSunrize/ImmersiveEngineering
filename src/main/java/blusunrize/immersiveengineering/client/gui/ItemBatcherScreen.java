/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ItemBatcherTileEntity.BatchMode;
import blusunrize.immersiveengineering.common.gui.ItemBatcherContainer;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import java.util.ArrayList;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ItemBatcherScreen extends IEContainerScreen<ItemBatcherContainer>
{
	ItemBatcherTileEntity tile;
	GuiButtonState<BatchMode> buttonBatchMode;

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
		buttonBatchMode = new GuiButtonState<>(guiLeft+7, guiTop+82, 18, 18, "", ItemBatcherTileEntity.BatchMode.values(),
				tile.batchMode.ordinal(), "immersiveengineering:textures/gui/item_batcher.png", 176, 36, 1,
				btn -> {
					CompoundNBT tag = new CompoundNBT();
					tile.batchMode = btn.getNextState();
					tag.putByte("batchMode", (byte)tile.batchMode.ordinal());
					handleButtonClick(tag);
				});
		this.addButton(buttonBatchMode);
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
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		TileEntity te = container.tile;
		this.font.drawString(I18n.format("block.immersiveengineering.item_batcher"), 8, 6, 0x190b06);

		//todo translations
		this.font.drawString("Filter", 8, 20, 0x190b06);
		this.font.drawString("Buffer", 8, 53, 0x190b06);
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		ArrayList<ITextComponent> tooltip = new ArrayList<>();

		//todo translations
		if(buttonBatchMode.isHovered())
			tooltip.add(new StringTextComponent(buttonBatchMode.getState().name()));

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, font, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/item_batcher.png");
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);
	}
}