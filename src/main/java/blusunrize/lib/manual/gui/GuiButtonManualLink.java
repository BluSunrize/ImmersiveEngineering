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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.Collections;
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
		super(x, y, w, h, "", btn -> {
			if(link!=null)
				link.changePage(gui, true);
		});
		this.gui = gui;
		this.link = link;
		this.localized = localized;
		if(gui.manual.improveReadability())
			this.localized = TextFormatting.BOLD+localized;
	}

	@Override
	public void render(int mx, int my, float partialTicks)
	{
		Minecraft mc = Minecraft.getInstance();
		isHovered = mx >= this.x&&my >= this.y&&mx < this.x+this.width&&my < this.y+this.height;
		if(isHovered)
		{
			drawHovered(mc, true, mx, my);
			for(GuiButtonManualLink btn : otherParts)
				if(btn!=this)
					btn.drawHovered(mc, false, mx, my);
		}
	}

	private void drawHovered(Minecraft mc, boolean mouse, int mx, int my)
	{
		FontRenderer font = gui.manual.fontRenderer();
		font.drawString(localized, x, y, gui.manual.getHighlightColour());
		List<String> tooltip;
		if(link!=null)
			tooltip = Collections.singletonList(gui.manual.formatLink(link));
		else
			tooltip = Collections.singletonList("Invalid link");
		gui.renderTooltip(tooltip, mx+8, my+4, font);
	}
}