/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual;

import blusunrize.lib.manual.gui.ManualScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class ManualElementImage extends SpecialManualElements
{
	private final ManualImage[] images;
	private final int size;

	public ManualElementImage(ManualInstance helper, ManualImage... images)
	{
		super(helper);
		this.images = images;
		int size = 0;
		for(ManualImage image : images)
			size += image.vSize+5;
		this.size = size;
	}

	@Override
	public void render(GuiGraphics graphics, ManualScreen gui, int x, int y, int mx, int my)
	{
		int yOff = 0;
		for(ManualImage image1 : images)
		{
			int xOff = 60-image1.uSize/2;
			graphics.fillGradient(x+xOff-2, y+yOff-2, x+xOff+image1.uSize+2, y+yOff+image1.vSize+2,
					0xffeaa74c, 0xfff6b059);
			graphics.fillGradient(x+xOff-1, y+yOff-1, x+xOff+image1.uSize+1, y+yOff+image1.vSize+1,
					0xffc68e46, 0xffbe8844);
			yOff += image1.vSize+5;
		}
		yOff = 0;
		for(ManualImage image : images)
		{
			int xOff = 60-image.uSize/2;
			ManualUtils.drawTexturedRect(graphics, image.resource, x+xOff, y+yOff, image.uSize, image.vSize, (image.uMin)/256f,
					(image.uMin+image.uSize)/256f, (image.vMin)/256f, (image.vMin+image.vSize)/256f);
			yOff += image.vSize+5;
		}
	}

	@Override
	public boolean listForSearch(String searchTag)
	{
		return false;
	}

	@Override
	public int getPixelsTaken()
	{
		return size;
	}

	public static class ManualImage
	{
		ResourceLocation resource;
		int uMin;
		int uSize;
		int vMin;
		int vSize;

		public ManualImage(ResourceLocation resource, int uMin, int uSize, int vMin, int vSize)
		{
			this.resource = resource;
			this.uMin = uMin;
			this.uSize = uSize;
			this.vMin = vMin;
			this.vSize = vSize;
		}
	}
}
