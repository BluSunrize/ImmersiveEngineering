/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;

import java.util.Locale;

public class ManualElementItem extends SpecialManualElements
{
	private final NonNullList<ItemStack> stacks;
	private final int yOffset;
	private final int lines;
	private final float scale;
	private final int longLineLen;
	private final int shortLineLen;
	private final int combinedLen;
	private final int itemsLastLine;

	static NonNullList<ItemStack> parseArray(ItemStack... stacks)
	{
		NonNullList<ItemStack> list = NonNullList.withSize(stacks.length, ItemStack.EMPTY);
		for(int i = 0; i < stacks.length; i++)
			list.set(i, stacks[i]);
		return list;
	}

	public ManualElementItem(ManualInstance manual, ItemStack... stacks)
	{
		this(manual, parseArray(stacks));
	}

	public ManualElementItem(ManualInstance manual, NonNullList<ItemStack> stacks)
	{
		super(manual);
		this.stacks = stacks;
		int totalLength = stacks.size();
		scale = totalLength > 7?1f: totalLength > 4?1.5f: 1.75f;
		//Alternating long and short lines of items
		int longLineLen = (int)(8/scale);
		int shortLineLen = longLineLen-1;
		int combinedLen = longLineLen+shortLineLen;
		lines = (totalLength/combinedLen*2)+
				(totalLength%combinedLen/longLineLen)+
				(totalLength%combinedLen%longLineLen > 0?1: 0);
		float avgPerLine = totalLength/(float)lines;
		this.longLineLen = MathHelper.ceil(avgPerLine);
		this.shortLineLen = MathHelper.floor(avgPerLine);
		this.combinedLen = longLineLen+shortLineLen;
		int itemsLastLines = totalLength%this.combinedLen;
		if(itemsLastLines==this.longLineLen) itemsLastLine = this.longLineLen;
		else if(itemsLastLines==0) itemsLastLine = this.shortLineLen;
		else itemsLastLine = itemsLastLines%this.longLineLen;
		yOffset = lines*(int)(18*scale);

	}

	@Override
	public void render(ManualScreen gui, int x, int y, int mx, int my)
	{
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		highlighted = ItemStack.EMPTY;
		int length = stacks.size();
		if(length > 0)
		{
			GlStateManager.scalef(scale, scale, scale);
			for(int line = 0; line < lines; line++)
			{
				int perLine = line==lines-1?itemsLastLine: line%2==0?longLineLen: shortLineLen;
				if(line==0&&perLine > length)
					perLine = length;
				int w2 = perLine*(int)(18*scale)/2;
				for(int i = 0; i < perLine; i++)
				{
					int item = line/2*combinedLen+line%2*longLineLen+i;
					if(item >= length)
						break;
					int xx = x+60-w2+(int)(i*18*scale);
					int yy = y+(lines < 2?4: 0)+line*(int)(18*scale);
					ManualUtils.renderItem().renderItemAndEffectIntoGUI(stacks.get(item), (int)(xx/scale), (int)(yy/scale));
					if(mx >= xx&&mx < xx+(16*scale)&&my >= yy&&my < yy+(16*scale))
						highlighted = stacks.get(item);
				}
			}
			GlStateManager.scalef(1/scale, 1/scale, 1/scale);
		}
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.enableBlend();

		this.renderHighlightedTooltip(gui, mx, my);
		RenderHelper.disableStandardItemLighting();
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		for(ItemStack stack : stacks)
			if(stack.getDisplayName().getFormattedText().toLowerCase(Locale.ENGLISH).contains(searchTag))
				return true;
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		return yOffset;
	}
}
