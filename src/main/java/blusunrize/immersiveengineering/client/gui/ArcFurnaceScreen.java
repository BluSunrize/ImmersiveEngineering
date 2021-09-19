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
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;

import static blusunrize.immersiveengineering.client.ClientUtils.mc;

public class ArcFurnaceScreen extends IEContainerScreen<ArcFurnaceContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("arc_furnace");
	private ArcFurnaceTileEntity tile;
	private GuiButtonIE distributeButton;

	public ArcFurnaceScreen(ArcFurnaceContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.imageHeight = 207;
		this.tile = container.tile;
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		ArrayList<Component> tooltip = new ArrayList<>();
		if(mx > leftPos+157&&mx < leftPos+164&&my > topPos+22&&my < topPos+68)
			tooltip.add(new TextComponent(tile.getEnergyStored(null)+"/"+tile.getMaxEnergyStored(null)+" IF"));
		if(distributeButton.isHovered())
			tooltip.add(new TranslatableComponent(Lib.GUI_CONFIG+"arcfurnace.distribute"));

		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}


	@Override
	protected void renderBg(PoseStack transform, float f, int mx, int my)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos, 0, 0, imageWidth, imageHeight);

		for(MultiblockProcess<?> process : tile.processQueue)
			if(process instanceof MultiblockProcessInMachine)
			{
				float mod = process.processTick/(float)process.maxTicks;
				int slot = ((MultiblockProcessInMachine<?>)process).getInputSlots()[0];
				int h = (int)Math.max(1, mod*16);
				this.blit(transform, leftPos+27+slot%3*21, topPos+34+slot/3*18+(16-h), 176, 16-h, 2, h);
			}

		int stored = (int)(46*(tile.getEnergyStored(null)/(float)tile.getMaxEnergyStored(null)));
		fillGradient(transform, leftPos+157, topPos+22+(46-stored), leftPos+164, topPos+68, 0xffb51500, 0xff600b00);
	}

	@Override
	public void init()
	{
		super.init();
		Minecraft mc = mc();
		distributeButton = new GuiButtonIE(leftPos+10, topPos+10, 16, 16, TextComponent.EMPTY, TEXTURE, 179, 0,
				btn -> {
					if(mc.player!=null&&mc.player.inventory.getCarried().isEmpty())
						autoSplitStacks();
				})
		{
			@Override
			public boolean isHovered()
			{
				return super.isHovered()&&mc.player!=null&&mc.player.inventory.getCarried().isEmpty();
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
