package blusunrize.immersiveengineering.client.gui.manual;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.client.ClientUtils;

public class GuiClickableList extends GuiButton
{
	String[] entries;
	float textScale;
	int offset;
	int maxOffset;
	int perPage;
	String localPrefix;
	String localPostfix;

	public GuiClickableList(int id, int x, int y, int w, int h, String localPrefix, String localPostfix, float textScale, String... entries)
	{
		super(id, x, y, w, h, "");
		this.textScale=textScale;
		this.entries=entries;
		this.localPrefix=localPrefix;
		this.localPostfix=localPostfix;

		perPage = (h-8)/getFontHeight();
		if(perPage<entries.length)
			maxOffset = entries.length-perPage;
	}

	int getFontHeight()
	{
		return (int) (ClientUtils.font().FONT_HEIGHT*textScale);
	}
	@Override
	public void drawButton(Minecraft mc, int mx, int my)
	{
		FontRenderer fr = ClientUtils.font();
		boolean uni = fr.getUnicodeFlag();
		ClientUtils.font().setUnicodeFlag(true);

		int mmY = my-this.yPosition;
		GL11.glPushMatrix();
		GL11.glScalef(textScale, textScale, textScale);
		GL11.glTranslatef(xPosition/textScale, yPosition/textScale, 0);
		for(int i=offset; i<Math.min(perPage, entries.length); i++)
		{
			int col = 0x555555;
			if(mmY>=i*getFontHeight() && mmY<(i+1)*getFontHeight())
				col = 0xd4804a;
			if(i!=0)
				GL11.glTranslatef(0, getFontHeight(), 0);
			fr.drawString(StatCollector.translateToLocal(localPrefix+entries[i]+localPostfix), 0,0, col, false);
		}

		GL11.glScalef(1/textScale,1/textScale,1/textScale);
		GL11.glPopMatrix();

		ClientUtils.font().setUnicodeFlag(uni);

		//Handle DWheel
		int mouseWheel = Mouse.getEventDWheel();
		if(mouseWheel!=0 && maxOffset>0)
		{
			if(mouseWheel<0 && offset<maxOffset)
				offset++;
			if(mouseWheel>0 && offset>0)
				offset--;
		}
	}

	public int selectedOption=-1;
	@Override
	public boolean mousePressed(Minecraft mc, int mx, int my)
	{
		boolean b = super.mousePressed(mc, mx, my);
		selectedOption=-1;
		if(b)
		{
			int mmY = my-this.yPosition;
			for(int i=offset; i<Math.min(perPage, entries.length); i++)
				if(mmY>=i*getFontHeight() && mmY<(i+1)*getFontHeight())
					selectedOption=i;
		}
		return b;
	}
}