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
import net.minecraft.network.chat.TextComponent;

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
		super(x, y, w, h, TextComponent.EMPTY, btn -> {
			if(link!=null)
				link.changePage(gui, true);
		});
		this.gui = gui;
		this.link = link;
		this.localized = localized;
		if(gui.manual.improveReadability())
			this.localized = ChatFormatting.BOLD+localized;
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		isHovered = mx >= this.x&&my >= this.y&&mx < this.x+this.width&&my < this.y+this.height;
		if(isHovered)
		{
			drawHovered(transform, mc, true, mx, my);
			for(GuiButtonManualLink btn : otherParts)
				if(btn!=this)
					btn.drawHovered(transform, mc, false, mx, my);
		}
	}

	private void drawHovered(PoseStack transform, Minecraft mc, boolean mouse, int mx, int my)
	{
		Font font = gui.manual.fontRenderer();
		font.draw(transform, localized, x, y, gui.manual.getHighlightColour());
		String tooltip;
		if(link!=null)
			tooltip = gui.manual.formatLink(link);
		else
			tooltip = "Invalid link";
		gui.renderToolTip(transform, Language.getInstance().getVisualOrder(
				ImmutableList.of(new TextComponent(tooltip))
		), mx+8, my+4, font);
	}
}