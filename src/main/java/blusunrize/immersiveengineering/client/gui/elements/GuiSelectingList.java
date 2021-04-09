/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui.elements;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ClientUtils;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;

import javax.annotation.Nullable;

public class GuiSelectingList extends GuiReactiveList
{
	public GuiSelectingList(Screen gui, int x, int y, int w, int h, IPressable handler, String... entries)
	{
		super(gui, x, y, w, h, handler, entries);
	}

	@Override
	public void render(MatrixStack transform, int mx, int my, float partialTicks)
	{
		super.render(transform, mx, my, partialTicks);
		if(selectedOption >= offset&&selectedOption-offset < perPage)
		{
			FontRenderer fr = ClientUtils.mc().fontRenderer;
			int yOff = (selectedOption-offset)*fr.FONT_HEIGHT;
			fill(transform, x, y+yOff, x+width, y+yOff+fr.FONT_HEIGHT, Lib.COLOUR_I_ImmersiveOrange&0x88ffffff);
		}
	}

	public void setSelectedString(String key)
	{
		for(int i = 0; i < this.entries.length; i++)
			if(key.equals(this.entries[i]))
				this.selectedOption = i;
	}

	@Nullable
	public String getSelectedString()
	{
		if(this.selectedOption >= 0&&this.selectedOption < this.entries.length)
			return this.entries[this.selectedOption];
		return null;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int key)
	{
		int curSel = selectedOption;
		boolean ret = super.mouseClicked(mx, my, key);
		// Keep selected option selected if clicked outside of list
		if(!ret)
			this.selectedOption = curSel;
		return ret;
	}
}
