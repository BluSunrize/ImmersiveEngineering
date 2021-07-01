/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.common.gui.ToolboxBlockContainer;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.function.Consumer;

public class ToolboxBlockScreen extends IEContainerScreen<ToolboxBlockContainer>
{
	public ToolboxBlockScreen(ToolboxBlockContainer container, PlayerInventory inventoryPlayer, ITextComponent title)
	{
		super(container, inventoryPlayer, title, makeTextureLocation("toolbox"));
		this.ySize = 238;
	}

	@Override
	protected void gatherAdditionalTooltips(int mouseX, int mouseY, Consumer<ITextComponent> addLine, Consumer<ITextComponent> addGray)
	{
		super.gatherAdditionalTooltips(mouseX, mouseY, addLine, addGray);
		int slot = -1;
		for(int i = 0; i < this.container.slotCount; i++)
		{
			Slot s = this.container.getSlot(i);
			if(!s.getHasStack()&&mouseX > guiLeft+s.xPos&&mouseX < guiLeft+s.xPos+16&&mouseY > guiTop+s.yPos&&mouseY < guiTop+s.yPos+16)
				slot = i;
		}
		String ss = null;
		if(slot >= 0)
			ss = slot < 3?"food": slot < 10?"tool": slot < 16?"wire": "any";
		if(ss!=null)
			addGray.accept(new TranslationTextComponent(Lib.DESC_INFO+"toolbox."+ss));
	}

	@Override
	protected void drawBackgroundTexture(MatrixStack transform)
	{
		blit(transform, guiLeft, guiTop - 17, 0, 0, 176, ySize + 17);
	}
}