/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonState;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityAssembler;
import blusunrize.immersiveengineering.common.gui.ContainerAssembler;
import blusunrize.immersiveengineering.common.network.MessageTileSync;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.*;

import java.util.ArrayList;
import java.util.List;

public class GuiAssembler extends GuiIEContainerBase
{
	static final String texture = "immersiveengineering:textures/gui/assembler.png";
	public TileEntityAssembler tile;

	public GuiAssembler(InventoryPlayer inventoryPlayer, TileEntityAssembler tile)
	{
		super(new ContainerAssembler(inventoryPlayer, tile));
		this.tile = tile;
		this.xSize = 230;
		this.ySize = 218;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		this.buttons.clear();
		for(int i = 0; i < 3; ++i)
		{
			this.buttons.add(new GuiButtonIE(i, guiLeft+11+i*59, guiTop+67, 10, 10, null, texture, 230, 50)
			{
				@Override
				public void onClick(double mX, double mY)
				{
					super.onClick(mX, mY);
					NBTTagCompound tag = new NBTTagCompound();
					tag.setInt("buttonID", id);
					ImmersiveEngineering.packetHandler.sendToServer(new MessageTileSync(tile, tag));
				}
			}.setHoverOffset(0, 10));
		}
		this.buttons.add(new GuiButtonState(3, guiLeft+162, guiTop+69, 16, 16, null, tile.recursiveIngredients, texture, 240, 66, 3)
		{
			@Override
			public void onClick(double mX, double mY)
			{
				super.onClick(mX, mY);
				tile.recursiveIngredients = !tile.recursiveIngredients;
				initGui();
			}
		});
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		List<ITextComponent> tooltip = new ArrayList<>();
		if(mx >= guiLeft+187&&mx < guiLeft+194&&my >= guiTop+12&&my < guiTop+59)
			tooltip.add(new TextComponentString(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));

		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+204, guiTop+13, 16, 46, 250, 0, 20, 50, mx, my, texture, tooltip);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+182, guiTop+70, 16, 46, 250, 0, 20, 50, mx, my, texture, tooltip);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft+204, guiTop+70, 16, 46, 250, 0, 20, 50, mx, my, texture, tooltip);

		for(int i = 0; i < tile.patterns.length; i++)
			if(tile.inventory.get(18+i).isEmpty()&&!tile.patterns[i].inv.get(9).isEmpty())
				if(mx >= guiLeft+27+i*58&&mx < guiLeft+43+i*58&&my >= guiTop+64&&my < guiTop+80)
				{
					tooltip.add(tile.patterns[i].inv.get(9).getDisplayName());
					tile.patterns[i].inv.get(9).getItem().addInformation(tile.patterns[i].inv.get(9), ClientUtils.mc().world, tooltip, TooltipFlags.NORMAL);
					for(int j = 0; j < tooltip.size(); j++)
						tooltip.get(j).setStyle(new Style().setColor(j==0?tile.patterns[i].inv.get(9).getRarity().color: TextFormatting.GRAY));
				}

		if(((mx >= guiLeft+11&&mx < guiLeft+21)||(mx >= guiLeft+69&&mx < guiLeft+79)||(mx >= guiLeft+127&&mx < guiLeft+137))&&my > guiTop+67&&my < guiTop+77)
			tooltip.add(new TextComponentTranslation(Lib.GUI_CONFIG+"assembler.clearRecipe"));
		if(mx >= guiLeft+162&&mx < guiLeft+178&&my > guiTop+69&&my < guiTop+85)
			tooltip.add(new TextComponentTranslation(Lib.GUI_CONFIG+"assembler."+(tile.recursiveIngredients?"recursiveIngredients": "nonRecursiveIngredients")));

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}

	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture(texture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+187, guiTop+13+(46-stored), guiLeft+194, guiTop+59, 0xffb51500, 0xff600b00);

		ClientUtils.handleGuiTank(tile.tanks[0], guiLeft+204, guiTop+13, 16, 46, 230, 0, 20, 50, mx, my, texture, null);
		ClientUtils.handleGuiTank(tile.tanks[1], guiLeft+182, guiTop+70, 16, 46, 230, 0, 20, 50, mx, my, texture, null);
		ClientUtils.handleGuiTank(tile.tanks[2], guiLeft+204, guiTop+70, 16, 46, 230, 0, 20, 50, mx, my, texture, null);

		for(int i = 0; i < tile.patterns.length; i++)
			if(tile.inventory.get(18+i).isEmpty()&&!tile.patterns[i].inv.get(9).isEmpty())
			{
				ItemStack stack = tile.patterns[i].inv.get(9);
				GlStateManager.pushMatrix();
				GlStateManager.translatef(0.0F, 0.0F, 32.0F);
				GlStateManager.color3f(1.0F, 1.0F, 1.0F);
				RenderHelper.disableStandardItemLighting();
				this.zLevel = 200.0F;
				itemRender.zLevel = 200.0F;
				FontRenderer font = null;
				if(!stack.isEmpty())
					font = stack.getItem().getFontRenderer(stack);
				if(font==null)
					font = fontRenderer;
				itemRender.renderItemAndEffectIntoGUI(stack, guiLeft+27+i*58, guiTop+64);
				itemRender.renderItemOverlayIntoGUI(font, stack, guiLeft+27+i*58, guiTop+64, TextFormatting.GRAY.toString()+stack.getCount());
				this.zLevel = 0.0F;
				itemRender.zLevel = 0.0F;


				GlStateManager.disableLighting();
				GlStateManager.disableDepthTest();
				ClientUtils.drawColouredRect(guiLeft+27+i*58, guiTop+64, 16, 16, 0x77444444);
				GlStateManager.enableLighting();
				GlStateManager.enableDepthTest();

				GlStateManager.popMatrix();
			}
	}
}