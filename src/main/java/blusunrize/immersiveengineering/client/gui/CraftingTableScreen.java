/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.common.gui.CraftingTableContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class CraftingTableScreen extends IEContainerScreen<CraftingTableContainer>
{
	public CraftingTableScreen(CraftingTableContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("craftingtable"));
		this.imageHeight = 210;
		this.inventoryLabelY = this.imageHeight-91;
	}

	@Override
	protected void renderLabels(PoseStack transform, int mouseX, int mouseY)
	{
		super.renderLabels(transform, mouseX, mouseY);
		this.font.draw(transform, menu.tile.getDisplayName().getString(), 8, 6, 0x190b06);
	}
}