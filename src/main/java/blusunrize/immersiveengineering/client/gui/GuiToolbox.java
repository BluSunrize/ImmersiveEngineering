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
import blusunrize.immersiveengineering.common.gui.ContainerToolbox;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.ArrayList;

public class GuiToolbox extends GuiIEContainerBase
{
	public GuiToolbox(InventoryPlayer inventoryPlayer, World world, EntityEquipmentSlot slot, ItemStack toolbox)
	{
		super(new ContainerToolbox(inventoryPlayer, world, slot, toolbox));
		this.ySize = 238;
	}

	@Override
	public void render(int mx, int my, float partial)
	{
		super.render(mx, my, partial);
		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		int slot = -1;
		for(int i = 0; i < ((ContainerToolbox)this.inventorySlots).internalSlots; i++)
		{
			Slot s = this.inventorySlots.inventorySlots.get(i);
			if(!s.getHasStack()&&mx > guiLeft+s.xPos&&mx < guiLeft+s.xPos+16&&my > guiTop+s.yPos&&my < guiTop+s.yPos+16)
				slot = i;
		}
		String ss = null;
		if(slot >= 0)
			ss = slot < 3?"food": slot < 10?"tool": slot < 16?"wire": "any";
		if(ss!=null)
			tooltip.add(new TextComponentTranslation(Lib.DESC_INFO+"toolbox."+ss).setStyle(new Style().setColor(TextFormatting.GRAY)));
		if(!tooltip.isEmpty())
		{
			ClientUtils.drawHoveringText(tooltip, mx, my, fontRenderer, guiLeft+xSize, -1);
			RenderHelper.enableGUIStandardItemLighting();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
	{
		GlStateManager.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/toolbox.png");
		this.drawTexturedModalRect(guiLeft, guiTop-17, 0, 0, 176, ySize+17);
	}

}