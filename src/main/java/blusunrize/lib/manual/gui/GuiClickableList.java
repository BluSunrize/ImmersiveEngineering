/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.Tree;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.List;

public class GuiClickableList extends Button
{
	String[] headers;
	boolean[] isCategory;
	@Nonnull
	List<Tree.AbstractNode<ResourceLocation, ManualEntry>> nodes;
	private float textScale;
	private int offset;
	private int maxOffset;
	int perPage;
	private GuiManual gui;

	private long prevWheelNano = 0;

	GuiClickableList(GuiManual gui, int x, int y, int w, int h, float textScale,
					 @Nonnull List<Tree.AbstractNode<ResourceLocation, ManualEntry>> nodes,
					 IPressable handler)
	{
		super(x, y, w, h, "", handler);
		this.gui = gui;
		this.textScale = textScale;
		this.nodes = nodes;
		headers = new String[nodes.size()];
		isCategory = new boolean[nodes.size()];
		for(int i = 0; i < nodes.size(); i++)
		{
			headers[i] = ManualUtils.getTitleForNode(nodes.get(i), gui.manual);
			isCategory[i] = !nodes.get(i).isLeaf();
		}

		perPage = (h-8)/getFontHeight();
		if(perPage < headers.length)
			maxOffset = headers.length-perPage;
	}

	int getFontHeight()
	{
		return (int)(gui.manual.fontRenderer.FONT_HEIGHT*textScale);
	}

	@Override
	public void render(int mx, int my, float partialTicks)
	{
		FontRenderer fr = gui.manual.fontRenderer;

		int mmY = my-this.y;
		GlStateManager.pushMatrix();
		GlStateManager.scalef(textScale, textScale, textScale);
		GlStateManager.translatef(x/textScale, y/textScale, 0);
		isHovered = mx >= x&&mx < x+width&&my >= y&&my < y+height;
		for(int i = 0; i < Math.min(perPage, headers.length); i++)
		{
			GlStateManager.color3f(1, 1, 1);
			int col = gui.manual.getTextColour();
			boolean currEntryHovered = isHovered&&mmY >= i*getFontHeight()&&mmY < (i+1)*getFontHeight();
			if(currEntryHovered)
				col = gui.manual.getHighlightColour();
			if(i!=0)
				GlStateManager.translatef(0, getFontHeight(), 0);
			int j = offset+i;
			if(j > headers.length-1)
				j = headers.length-1;
			String s = headers[j];
			if(isCategory[j])
			{
				ManualUtils.bindTexture(gui.texture);
				GlStateManager.enableBlend();
				this.blit(0, 0, 11, 226+(currEntryHovered?20: 0), 5, 10);
			}
			fr.drawString(s, isCategory[j]?7: 0, 0, col);
		}
		GlStateManager.scalef(1/textScale, 1/textScale, 1/textScale);
		GlStateManager.popMatrix();
		if(maxOffset > 0)
		{
			int h1 = offset*getFontHeight();
			int h2 = height-8-maxOffset*getFontHeight();
			fill(x+width, y+h1, x+width+8, y+h1+h2, 0x0a000000);
			fill(x+width+1, y+h1, x+width+6, y+h1+h2, 0x28000000);
			if(offset > 0)
				fill(x+width, y, x+width+8, y+h1, 0x0a000000);
			if(offset < maxOffset)
			{
				int h3 = (maxOffset-offset)*getFontHeight();
				fill(x+width, y+height-8-h3, x+width+8, y+height-8, 0x0a000000);
			}
		}
	}

	@Override
	public boolean mouseScrolled(double p_mouseScrolled_1_, double p_mouseScrolled_3_, double amount)
	{
		if(amount < 0&&offset < maxOffset)
		{
			offset++;
			return true;
		}
		if(amount > 0&&offset > 0)
		{
			offset--;
			return true;
		}
		return false;
	}

	public int selectedOption = -1;

	@Override
	public boolean mouseClicked(double mx, double my, int btn)
	{
		boolean b = super.clicked(mx, my);
		selectedOption = -1;
		if(b)
		{
			double mmY = my-this.y;
			for(int i = 0; i < Math.min(perPage, headers.length); i++)
				if(mmY >= i*getFontHeight()&&mmY < (i+1)*getFontHeight())
					selectedOption = offset+i;
		}
		return selectedOption!=-1;
	}
}