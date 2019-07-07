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
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPoweredMultiblock.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.TileEntityPoweredMultiblock.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.metal.TileEntityArcFurnace;
import blusunrize.immersiveengineering.common.gui.ContainerArcFurnace;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

public class GuiArcFurnace extends GuiIEContainerBase
{
	static final String texture = "immersiveengineering:textures/gui/arc_furnace.png";
	TileEntityArcFurnace tile;
	private GuiButtonIE distributeButton;

	public GuiArcFurnace(PlayerInventory inventoryPlayer, TileEntityArcFurnace tile)
	{
		super(new ContainerArcFurnace(inventoryPlayer, tile));
		this.ySize = 207;
		this.tile = tile;
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		if(mx > guiLeft+157&&mx < guiLeft+164&&my > guiTop+22&&my < guiTop+68)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));
		if(distributeButton.canFocus())
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"arcfurnace.distribute"));

		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		GlStateManager.color3f(1, 1, 1);
		ClientUtils.bindTexture(texture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(MultiblockProcess process : tile.processQueue)
			if(process instanceof MultiblockProcessInMachine)
			{
				float mod = process.processTick/(float)process.maxTicks;
				int slot = ((MultiblockProcessInMachine)process).getInputSlots()[0];
				int h = (int)Math.max(1, mod*16);
				this.drawTexturedModalRect(guiLeft+27+slot%3*21, guiTop+34+slot/3*18+(16-h), 176, 16-h, 2, h);
			}

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+157, guiTop+22+(46-stored), guiLeft+164, guiTop+68, 0xffb51500, 0xff600b00);
	}

	@Override
	public void initGui()
	{
		super.initGui();
		distributeButton = new GuiButtonIE(0, guiLeft+10, guiTop+10, 16, 16, null, texture, 179, 0)
		{
			@Override
			public boolean canFocus()
			{
				return super.canFocus()&&mc.player!=null&&mc.player.inventory.getItemStack().isEmpty();
			}

			@Override
			public void onClick(double mX, double mY)
			{
				if(mc.player!=null&&mc.player.inventory.getItemStack().isEmpty())
					autoSplitStacks();
			}
		}.setHoverOffset(0, 16);
		this.buttons.add(distributeButton);
	}

	private void autoSplitStacks()
	{
		int emptySlot;
		int largestSlot;
		int largestCount;
		for(int j = 0; j < 12; j++)
		{
			emptySlot = -1;
			largestSlot = -1;
			largestCount = -1;
			for(int i = 0; i < 12; i++)
				if(this.inventorySlots.getSlot(i).getHasStack())
				{
					int count = this.inventorySlots.getSlot(i).getStack().getCount();
					if(count > 1&&count > largestCount)
					{
						largestSlot = i;
						largestCount = count;
					}
				}
				else if(emptySlot < 0)
					emptySlot = i;
			if(emptySlot >= 0&&largestSlot >= 0)
			{
				this.handleMouseClick(this.inventorySlots.getSlot(largestSlot), largestSlot, 1, ClickType.PICKUP);
				this.handleMouseClick(this.inventorySlots.getSlot(emptySlot), emptySlot, 0, ClickType.PICKUP);
			}
			else
				break;
		}
	}
}
