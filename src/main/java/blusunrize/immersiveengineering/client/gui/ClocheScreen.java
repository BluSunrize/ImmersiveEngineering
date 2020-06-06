/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.blocks.metal.ClocheTileEntity;
import blusunrize.immersiveengineering.common.gui.ClocheContainer;
import blusunrize.immersiveengineering.common.util.Utils;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

public class ClocheScreen extends IEContainerScreen<ClocheContainer>
{
	private ClocheTileEntity tile;

	public ClocheScreen(ClocheContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.tile = container.tile;
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		ClientUtils.handleGuiTank(tile.tank, guiLeft+8, guiTop+8, 16, 47, 176, 30, 20, 51, mx, my, "immersiveengineering:textures/gui/cloche.png", tooltip);
		if(mx > guiLeft+30&&mx < guiLeft+37&&my > guiTop+22&&my < guiTop+68)
		{
			tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"fertFill", Utils.formatDouble(tile.fertilizerAmount/(float)IEConfig.MACHINES.cloche_fertilizer.get(), "0.00")));
			tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"fertMod", Utils.formatDouble(tile.fertilizerMod, "0.00")));

		}
		if(mx > guiLeft+158&&mx < guiLeft+165&&my > guiTop+22&&my < guiTop+68)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));
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
		GlStateManager.enableBlend();
		ClientUtils.bindTexture("immersiveengineering:textures/gui/cloche.png");
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);
		GlStateManager.disableBlend();

		ClientUtils.handleGuiTank(tile.tank, guiLeft+8, guiTop+8, 16, 47, 176, 30, 20, 51, mx, my, "immersiveengineering:textures/gui/cloche.png", null);
		int stored = (int)(46*(tile.fertilizerAmount/(float)IEConfig.MACHINES.cloche_fertilizer.get()));
		ClientUtils.drawGradientRect(guiLeft+30, guiTop+22+(46-stored), guiLeft+37, guiTop+68, 0xff95ed00, 0xff8a5a00);

		stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+158, guiTop+22+(46-stored), guiLeft+165, guiTop+68, 0xffb51500, 0xff600b00);
	}
}