/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualInstance.ManualLink;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;

public class GuiButtonManualLink extends Button
{
	public String localized;
	@Nullable
	public ManualLink link;
	ManualScreen gui;
	public List<GuiButtonManualLink> otherParts = ImmutableList.of();

	public GuiButtonManualLink(ManualScreen gui, int x, int y, int w, int h, @Nullable ManualLink link, String localized)
	{
		super(x, y, w, h, Component.empty(), btn -> {
			if(link!=null)
				link.changePage(gui, true);
		}, DEFAULT_NARRATION);
		this.gui = gui;
		this.link = link;
		this.localized = localized;
		if(gui.manual.improveReadability())
			this.localized = ChatFormatting.BOLD+localized;
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partialTicks)
	{
		isHovered = mx >= this.getX()&&my >= this.getY()&&mx < this.getX()+this.width&&my < this.getY()+this.height;
		if(isHovered)
		{
			drawHovered(transform, mx, my);
			for(GuiButtonManualLink btn : otherParts)
				if(btn!=this)
					btn.drawHovered(transform, mx, my);
		}
	}

	private void drawHovered(PoseStack transform, int mx, int my)
	{
		Font font = gui.manual.fontRenderer();
		font.draw(transform, localized, getX(), getY(), gui.manual.getHighlightColour());
		String tooltip;
		if(link!=null)
			tooltip = gui.manual.formatLink(link);
		else
			tooltip = "Invalid link";
		gui.renderTooltip(transform, Language.getInstance().getVisualOrder(
				ImmutableList.of(Component.literal(tooltip))
		), mx+8, my+4, font);
	}
}