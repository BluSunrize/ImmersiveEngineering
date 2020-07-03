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
import blusunrize.immersiveengineering.common.gui.ToolboxContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;

public class ToolboxScreen extends IEContainerScreen<ToolboxContainer>
{
	public ToolboxScreen(ToolboxContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title);
		this.ySize = 238;
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		ArrayList<ITextComponent> tooltip = new ArrayList<>();
		int slot = -1;
		for(int i = 0; i < this.container.internalSlots; i++)
		{
			Slot s = this.container.getSlot(i);
			if(!s.getHasStack()&&mx > guiLeft+s.xPos&&mx < guiLeft+s.xPos+16&&my > guiTop+s.yPos&&my < guiTop+s.yPos+16)
				slot = i;
		}
		String ss = null;
		if(slot >= 0)
			ss = slot < 3?"food": slot < 10?"tool": slot < 16?"wire": "any";
		if(ss!=null)
			tooltip.add(new TranslationTextComponent(Lib.DESC_INFO+"toolbox."+ss).setStyle(new Style().setColor(TextFormatting.GRAY)));
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void func_230450_a_(MatrixStack transform, float par1, int par2, int par3)
	{
		RenderSystem.color3f(1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/toolbox.png");
		this.blit(transform, guiLeft, guiTop-17, 0, 0, 176, ySize+17);
	}

}