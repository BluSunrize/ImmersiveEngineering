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
import blusunrize.immersiveengineering.client.gui.elements.GuiButtonIE.IIEPressable;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

public class GuiSelectingList extends GuiReactiveList
{
	public GuiSelectingList(int x, int y, int w, int h, IIEPressable<GuiSelectingList> handler, String... entries)
	{
		super(x, y, w, h, handler, () -> Arrays.asList(entries));
	}

	@Override
	public void render(PoseStack transform, int mx, int my, float partialTicks)
	{
		super.render(transform, mx, my, partialTicks);
		if(selectedOption >= offset&&selectedOption-offset < perPage)
		{
			Font fr = ClientUtils.mc().font;
			int yOff = (selectedOption-offset)*fr.lineHeight;
			fill(transform, x, y+yOff, x+width, y+yOff+fr.lineHeight, Lib.COLOUR_I_ImmersiveOrange&0x88ffffff);
		}
	}

	public void setSelectedString(String key)
	{
		final List<String> entries = this.entries.get();
		for(int i = 0; i < entries.size(); i++)
			if(key.equals(entries.get(i)))
				this.selectedOption = i;
	}

	@Nullable
	public String getSelectedString()
	{
		final List<String> entries = this.entries.get();
		if(this.selectedOption >= 0&&this.selectedOption < entries.size())
			return entries.get(this.selectedOption);
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
