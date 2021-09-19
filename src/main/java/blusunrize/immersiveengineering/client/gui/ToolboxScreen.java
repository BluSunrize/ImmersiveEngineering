/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.client.TextUtils;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.gui.ToolboxContainer;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.fml.client.gui.GuiUtils;

import java.util.ArrayList;

public class ToolboxScreen extends IEContainerScreen<ToolboxContainer>
{
	private static final ResourceLocation TEXTURE = makeTextureLocation("toolbox");

	public ToolboxScreen(ToolboxContainer container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title);
		this.imageHeight = 238;
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partial)
	{
		super.render(transform, mx, my, partial);
		ArrayList<Component> tooltip = new ArrayList<>();
		int slot = -1;
		for(int i = 0; i < this.menu.internalSlots; i++)
		{
			Slot s = this.menu.getSlot(i);
			if(!s.hasItem()&&mx > leftPos+s.x&&mx < leftPos+s.x+16&&my > topPos+s.y&&my < topPos+s.y+16)
				slot = i;
		}
		String ss = null;
		if(slot >= 0)
			ss = slot < 3?"food": slot < 10?"tool": slot < 16?"wire": "any";
		if(ss!=null)
			tooltip.add(TextUtils.applyFormat(
					new TranslatableComponent(Lib.DESC_INFO+"toolbox."+ss),
					ChatFormatting.GRAY
			));
		if(!tooltip.isEmpty())
			GuiUtils.drawHoveringText(transform, tooltip, mx, my, width, height, -1, font);
	}

	@Override
	protected void renderBg(PoseStack transform, float par1, int par2, int par3)
	{
		ClientUtils.bindTexture(TEXTURE);
		this.blit(transform, leftPos, topPos-17, 0, 0, 176, imageHeight+17);
	}

}