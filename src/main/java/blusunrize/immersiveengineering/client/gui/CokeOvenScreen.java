/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.common.blocks.stone.CokeOvenTileEntity;
import blusunrize.immersiveengineering.common.gui.CokeOvenContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class CokeOvenScreen extends IEContainerScreen<CokeOvenContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("coke_oven");

	private CokeOvenTileEntity tile;

	public CokeOvenScreen(CokeOvenContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		clearIntArray(tile.guiData);
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<Component> tooltip = new ArrayList<>();
		GuiHelper.handleGuiTank(transform, tile.tank, leftPos+129, topPos+20, 16, 47, 176, 31, 20, 51, mx, my, TEXTURE, tooltip);
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		if(tile.processMax > 0&&tile.process > 0)
		{
			int h = (int)(12*(tile.process/(float)tile.processMax));
			this.blit(transform, leftPos+59, topPos+37+12-h, 179, 1+12-h, 9, h);
		}

		GuiHelper.handleGuiTank(transform, tile.tank, leftPos+129, topPos+20, 16, 47, 176, 31, 20, 51, mx, my, TEXTURE, null);

	}
}
