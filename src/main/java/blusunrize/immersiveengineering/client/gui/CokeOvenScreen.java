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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;
import java.util.List;

public class CokeOvenScreen extends IEContainerScreen<CokeOvenContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("coke_oven");

	private CokeOvenTileEntity tile;

	public CokeOvenScreen(CokeOvenContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
		clearIntArray(tile.guiData);
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		List<ITextComponent> tooltip = new ArrayList<>();
		GuiHelper.handleGuiTank(transform, tile.tank, guiLeft+129, guiTop+20, 16, 47, 176, 31, 20, 51, mx, my, TEXTURE, tooltip);
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(MatrixStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, guiLeft, guiTop, 0, 0, xSize, ySize);

		if(tile.processMax > 0&&tile.process > 0)
		{
			int h = (int)(12*(tile.process/(float)tile.processMax));
			this.blit(transform, guiLeft+59, guiTop+37+12-h, 179, 1+12-h, 9, h);
		}

		GuiHelper.handleGuiTank(transform, tile.tank, guiLeft+129, guiTop+20, 16, 47, 176, 31, 20, 51, mx, my, TEXTURE, null);

	}
}
