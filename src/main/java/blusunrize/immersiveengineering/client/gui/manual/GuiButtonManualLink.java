package blusunrize.immersiveengineering.client.gui.manual;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;
import blusunrize.immersiveengineering.client.ClientUtils;

public class GuiButtonManualLink extends GuiButton
{
	public String key;
	public String localized;
	public int pageLinked;
	public GuiButtonManualLink(int id, int x, int y, int w, int h, String key, String localized, int pageLinked)
	{
		super(id, x,y, w,h, "");
		this.key = key;
		this.localized = localized;
		this.pageLinked = pageLinked;
	}
	
	@Override
	public void drawButton(Minecraft mc, int mx, int my)
	{
		this.field_146123_n = mx >= this.xPosition && my >= this.yPosition && mx < this.xPosition + this.width && my < this.yPosition + this.height;
		if(field_146123_n)
		{
			boolean uni = ClientUtils.font().getUnicodeFlag();
			ClientUtils.font().setUnicodeFlag(true);
			ClientUtils.font().drawString(localized, xPosition, yPosition+2, 0xffffff);
			ClientUtils.font().setUnicodeFlag(false);
			ClientUtils.drawHoveringText(Arrays.asList(StatCollector.translateToLocal("ie.manual.entry."+key+".name")+", "+(pageLinked+1)), mx+8,my+4, ClientUtils.font());
			ClientUtils.font().setUnicodeFlag(uni);
		}
		
	}
}