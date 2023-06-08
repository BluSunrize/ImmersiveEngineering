/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE;
import blusunrize.immersiveengineering.client.gui.info.EnergyInfoArea;
import blusunrize.immersiveengineering.client.gui.info.InfoArea;
import blusunrize.immersiveengineering.common.gui.ArcFurnaceMenu;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class ArcFurnaceScreen extends IEContainerScreen<ArcFurnaceMenu>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("arc_furnace");
	private GuiButtonIE distributeButton;

	public ArcFurnaceScreen(ArcFurnaceMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, TEXTURE);
		this.imageHeight = 207;
		this.inventoryLabelY = 116;
	}

	@Nonnull
	@Override
	protected List<InfoArea> makeInfoAreas()
	{
		return ImmutableList.of(new EnergyInfoArea(leftPos+157, topPos+22, menu.energy));
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<Component> addLine, Consumer<Component> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		if(distributeButton.isHoveredOrFocused())
			addLine.accept(Component.translatable(Lib.GUI_CONFIG+"arcfurnace.distribute"));
	}

	@Override
	protected void drawContainerBackgroundPre(@Nonnull GuiGraphics graphics, float f, int mx, int my)
	{
		for(var process : menu.processes.get())
		{
			int slot = process.slot();
			int h = process.processStep();
			graphics.blit(background, leftPos+27+slot%3*21, topPos+34+slot/3*18+(16-h), 176, 16-h, 2, h);
		}
	}

	@Override
	public void init()
	{
		super.init();
		distributeButton = new GuiButtonIE(leftPos+10, topPos+10, 16, 16, Component.empty(), TEXTURE, 179, 0,
				btn -> {
					if(menu.getCarried().isEmpty())
						autoSplitStacks();
				})
		{
			@Override
			public boolean isHoveredOrFocused()
			{
				return super.isHoveredOrFocused()&&menu.getCarried().isEmpty();
			}
		}.setHoverOffset(0, 16);
		this.addRenderableWidget(distributeButton);
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
				if(menu.getSlot(i).hasItem())
				{
					int count = menu.getSlot(i).getItem().getCount();
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
				this.slotClicked(menu.getSlot(largestSlot), largestSlot, 1, ClickType.PICKUP);
				this.slotClicked(menu.getSlot(emptySlot), emptySlot, 0, ClickType.PICKUP);
			}
			else
				break;
		}
	}
}
