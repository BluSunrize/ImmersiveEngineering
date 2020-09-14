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
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;

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
		super(x, y, w, h, StringTextComponent.EMPTY, btn -> {
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
	public void render(MatrixStack transform, int mx, int my, float partialTicks)
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

	private void drawHovered(MatrixStack transform, Minecraft mc, boolean mouse, int mx, int my)
	{
		FontRenderer font = gui.manual.fontRenderer();
		font.drawString(transform, localized, x, y, gui.manual.getHighlightColour());
		String tooltip;
		if(link!=null)
			tooltip = gui.manual.formatLink(link);
		else
			tooltip = "Invalid link";
		gui.renderToolTip(transform, ImmutableList.of(
				IReorderingProcessor.func_242239_a(tooltip, Style.EMPTY)
		), mx+8, my+4, font);
	}
}