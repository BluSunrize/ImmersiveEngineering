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
import blusunrize.lib.manual.Tree.AbstractNode;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ClickableList extends Button
{
	private String[] headers;
	private boolean[] isCategory;
	@Nonnull
	private List<Tree.AbstractNode<ResourceLocation, ManualEntry>> nodes = new ArrayList<>();
	private float textScale;
	private final Consumer<AbstractNode<ResourceLocation, ManualEntry>> handler;
	private int offset;
	private int maxOffset;
	private final int perPage;
	private ManualScreen gui;

	ClickableList(ManualScreen gui, int x, int y, int w, int h, float textScale,
				  @Nonnull List<Tree.AbstractNode<ResourceLocation, ManualEntry>> nodes,
				  Consumer<Tree.AbstractNode<ResourceLocation, ManualEntry>> handler)
	{
		super(x, y, w, h, "", btn -> {
		});
		this.gui = gui;
		this.textScale = textScale;
		this.handler = handler;
		this.perPage = (h-8)/getFontHeight();
		setEntries(nodes);
	}

	int getFontHeight()
	{
		return (int)(gui.manual.fontRenderer().FONT_HEIGHT*textScale);
	}

	@Override
	public void render(int mx, int my, float partialTicks)
	{
		if(!visible)
			return;
		FontRenderer fr = gui.manual.fontRenderer();

		int mmY = my-this.y;
		RenderSystem.pushMatrix();
		RenderSystem.scalef(textScale, textScale, textScale);
		RenderSystem.translatef(x/textScale, y/textScale, 0);
		isHovered = mx >= x&&mx < x+width&&my >= y&&my < y+height;
		for(int i = 0; i < Math.min(perPage, headers.length); i++)
		{
			RenderSystem.color3f(1, 1, 1);
			int col = gui.manual.getTextColour();
			boolean currEntryHovered = isHovered&&mmY >= i*getFontHeight()&&mmY < (i+1)*getFontHeight();
			if(currEntryHovered)
				col = gui.manual.getHighlightColour();
			if(i!=0)
				RenderSystem.translatef(0, getFontHeight(), 0);
			int j = offset+i;
			if(j > headers.length-1)
				j = headers.length-1;
			String s = headers[j];
			if(isCategory[j])
			{
				ManualUtils.bindTexture(gui.texture);
				RenderSystem.enableBlend();
				this.blit(0, 0, 11, 226+(currEntryHovered?20: 0), 5, 10);
			}
			fr.drawString(s, isCategory[j]?7: 0, 0, col);
		}
		RenderSystem.scalef(1/textScale, 1/textScale, 1/textScale);
		RenderSystem.popMatrix();
		if(maxOffset > 0)
		{
			final int minVisibleBlack = 0x1B<<24;
			final int mainBarBlack = 0x28<<24;
			final float totalHeight = maxOffset*getFontHeight()+getHeight();
			final float heightTopRel = (offset*getFontHeight())/totalHeight;
			final float heightBottomRel = (offset*getFontHeight()+getHeight())/totalHeight;
			final int heightTopAbs = (int)(heightTopRel*getHeight());
			final int heightBottomAbs = (int)(heightBottomRel*getHeight());
			fill(x+width, y, x+width+8, y+height, minVisibleBlack);
			fill(x+width+1, y+heightTopAbs, x+width+7, y+heightBottomAbs, mainBarBlack);
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


	@Nullable
	public AbstractNode<ResourceLocation, ManualEntry> getSelected(double mx, double my)
	{
		if(!super.clicked(mx, my))
			return null;
		double mmY = my-this.y;
		for(int i = 0; i < Math.min(perPage, headers.length); i++)
			if(mmY >= i*getFontHeight()&&mmY < (i+1)*getFontHeight())
				return nodes.get(offset+i);
		return null;
	}

	@Override
	public void onClick(double mx, double my)
	{
		handler.accept(getSelected(mx, my));
	}

	@Override
	protected boolean clicked(double mx, double my)
	{
		return getSelected(mx, my)!=null;
	}

	public void setEntries(List<AbstractNode<ResourceLocation, ManualEntry>> nodes)
	{
		this.nodes = nodes;
		headers = new String[nodes.size()];
		isCategory = new boolean[nodes.size()];
		for(int i = 0; i < nodes.size(); i++)
		{
			headers[i] = ManualUtils.getTitleForNode(nodes.get(i), gui.manual);
			isCategory[i] = !nodes.get(i).isLeaf();
		}

		if(perPage < headers.length)
			maxOffset = headers.length-perPage;
		else
			maxOffset = 0;
		height = getFontHeight()*Math.min(perPage, headers.length);
	}
}