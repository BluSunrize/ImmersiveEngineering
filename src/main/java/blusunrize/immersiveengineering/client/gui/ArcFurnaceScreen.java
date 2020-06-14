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
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcess;
import blusunrize.immersiveengineering.common.blocks.generic.PoweredMultiblockTileEntity.MultiblockProcessInMachine;
import blusunrize.immersiveengineering.common.blocks.metal.ArcFurnaceTileEntity;
import blusunrize.immersiveengineering.common.gui.ArcFurnaceContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.ArrayList;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ArcFurnaceScreen extends IEContainerScreen<ArcFurnaceContainer>
{
	static final String texture = "immersiveengineering:textures/gui/arc_furnace.png";
	private ArcFurnaceTileEntity tile;
	private GuiButtonIE distributeButton;

	public ArcFurnaceScreen(ArcFurnaceContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.ySize = 207;
		this.tile = container.tile;
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		if(mx > guiLeft+157&&mx < guiLeft+164&&my > guiTop+22&&my < guiTop+68)
			tooltip.add(new StringTextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));
		if(distributeButton.isHovered())
			tooltip.add(new TranslationTextComponent(Lib.GUI_CONFIG+"arcfurnace.distribute"));

		if(!tooltip.isEmpty())
			ClientUtils.drawHoveringText(tooltip, mx, my, font, width, height);
	}


	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mx, int my)
	{
		ClientUtils.bindTexture(texture);
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);

		for(MultiblockProcess process : tile.processQueue)
			if(process instanceof MultiblockProcessInMachine)
			{
				float mod = process.processTick/(float)process.maxTicks;
				int slot = ((MultiblockProcessInMachine)process).getInputSlots()[0];
				int h = (int)Math.max(1, mod*16);
				this.blit(guiLeft+27+slot%3*21, guiTop+34+slot/3*18+(16-h), 176, 16-h, 2, h);
			}

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		ClientUtils.drawGradientRect(guiLeft+157, guiTop+22+(46-stored), guiLeft+164, guiTop+68, 0xffb51500, 0xff600b00);
	}

	@Override
	public void init()
	{
		super.init();
		Minecraft mc = mc();
		distributeButton = new GuiButtonIE(guiLeft+10, guiTop+10, 16, 16, "", texture, 179, 0,
				btn -> {
					if(mc.player!=null&&mc.player.inventory.getItemStack().isEmpty())
						autoSplitStacks();
				})
		{
			@Override
			public boolean isHovered()
			{
				return super.isHovered()&&mc.player!=null&&mc.player.inventory.getItemStack().isEmpty();
			}
		}.setHoverOffset(0, 16);
		this.addButton(distributeButton);
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
				if(container.getSlot(i).getHasStack())
				{
					int count = container.getSlot(i).getStack().getCount();
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
				this.handleMouseClick(container.getSlot(largestSlot), largestSlot, 1, ClickType.PICKUP);
				this.handleMouseClick(container.getSlot(emptySlot), emptySlot, 0, ClickType.PICKUP);
			}
			else
				break;
		}
	}
}
