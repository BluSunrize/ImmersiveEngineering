package blusunrize.immersiveengineering.client.gui.manual;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.IManualPage;
import blusunrize.immersiveengineering.client.ClientUtils;

public class GuiManual extends GuiScreen
{
	int xSize = 186;
	int ySize = 198;
	int guiLeft;
	int guiTop;
	int manualTick=0;
	List<GuiButton> pageButtons = new ArrayList();

	public static String selectedEntry;
	public static int page;
	public static GuiManual activeManual;

	public String[] headers;
	public GuiManual(EntityPlayer player)
	{
		super();
		headers = new String[manualContents.size()];
		for(int i=0;i<manualContents.size();i++)
			headers[i] = StatCollector.translateToLocal(manualContents.get(i).name);
		activeManual=this;
	}
	@Override
    public boolean doesGuiPauseGame()
    {
        return false;
    }

	@Override
	public void initGui()
	{
		guiLeft =  (this.width - this.xSize) / 2;
		guiTop =  (this.height - this.ySize) / 2;

		this.buttonList.clear();
		this.pageButtons.clear();
		if(getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = getEntry(selectedEntry);
			IManualPage mPage = (page<0||page>=entry.pages.length)?null: entry.pages[page];
			if(mPage!=null)
				mPage.initPage(this, guiLeft+32,guiTop+28, pageButtons);
			buttonList.addAll(pageButtons);
		}
		else
			this.buttonList.add(new GuiClickableList(0, guiLeft+40,guiTop+16, 100,148, "ie.manual.entry.",".name", 1f, headers));
	}

	@Override
	public void drawScreen(int mx, int my, float f)
	{
		manualTick++;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ClientUtils.bindTexture("immersiveengineering:textures/gui/manual.png");
		this.drawTexturedModalRect(guiLeft,guiTop, 0,0, xSize,ySize);

		if(getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = getEntry(selectedEntry);
			mx-=guiLeft;
			my-=guiTop;
			boolean b0 = mx>32&&mx<32+17 && my>179&&my<179+10;
			boolean b1 = mx>135&&mx<135+17 && my>179&&my<179+10;

			if(page>0)
				this.drawTexturedModalRect(guiLeft+ 32,guiTop+179, 0,216+(b0?20:0), 16,10);
			if(page<entry.pages.length-1)
				this.drawTexturedModalRect(guiLeft+136,guiTop+179, 0,226+(b1?20:0), 16,10);

			boolean uni = ClientUtils.font().getUnicodeFlag();
			ClientUtils.font().setUnicodeFlag(true);
			
			//Title
			this.drawCenteredStringScaled(fontRendererObj, EnumChatFormatting.BOLD+StatCollector.translateToLocal("ie.manual.entry."+entry.name+".name"), guiLeft+xSize/2,guiTop+14, 0xd4804a, 1, true);
			this.drawCenteredStringScaled(fontRendererObj, StatCollector.translateToLocal("ie.manual.entry."+entry.name+".subtext"), guiLeft+xSize/2,guiTop+22, 0xd4804a, 1, true);
			//Page Number
			this.drawCenteredStringScaled(fontRendererObj, EnumChatFormatting.BOLD.toString()+(page+1), guiLeft+xSize/2,guiTop+183, 0x9c917c, 1, false);
			
			GL11.glColor3f(1,1,1);
			IManualPage mPage = (page<0||page>=entry.pages.length)?null: entry.pages[page];
			if(mPage!=null)
				mPage.renderPage(this, guiLeft+32,guiTop+30, mx+guiLeft,my+guiTop);

			mx+=guiLeft;
			my+=guiTop;
			ClientUtils.font().setUnicodeFlag(uni);
		}
		super.drawScreen(mx, my, f);
	}

	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 0)
		{
			int sel = ((GuiClickableList)button).selectedOption;
			if(sel>=0&&sel<headers.length)
				selectedEntry = headers[sel];
			((GuiClickableList)button).selectedOption=-1;
			this.initGui();
		}
		if(pageButtons.contains(button) && getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = getEntry(selectedEntry);
			IManualPage mPage = (page<0||page>=entry.pages.length)?null: entry.pages[page];
			if(mPage!=null)
				mPage.buttonPressed(this, button);
		}
	}


	public void drawCenteredStringScaled(FontRenderer fr, String s, int x, int y, int colour, float scale, boolean shadow)
	{
		int xx = (int)Math.floor(x/scale - (fr.getStringWidth(s)/2));
		int yy = (int)Math.floor(y/scale - (fr.FONT_HEIGHT/2));
		if(scale!=1)
			GL11.glScalef(scale, scale, scale);
		fr.drawString(s, xx,yy, colour, shadow);
		if(scale!=1)
			GL11.glScalef(1/scale, 1/scale, 1/scale);
	}

	@Override
	public void mouseClicked(int mx, int my, int button)
	{
		super.mouseClicked(mx,my,button);
		if(button==0 && getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = getEntry(selectedEntry);
			mx -= guiLeft;
			my -= guiTop;
			if(page>0 && mx>32&&mx<32+17 && my>179&&my<179+10)
			{
				page--;
				this.initGui();
			}
			if(page<entry.pages.length-1 && mx>135&&mx<135+17 && my>179&&my<179+10)
			{
				page++;
				this.initGui();
			}

		}
		else if(button==1)
		{
			selectedEntry=null;
			page=0;
			this.initGui();
		}
	}


	public static ArrayList<ManualEntry> manualContents = new ArrayList<ManualEntry>();
	public static void addEntry(String name, IManualPage... pages)
	{
		manualContents.add(new ManualEntry(name,pages));
	}
	public static ManualEntry getEntry(String name)
	{
		for(ManualEntry e : manualContents)
			if(e.name.equalsIgnoreCase(name))
				return e;
		return null;
	}
	public static class ManualEntry
	{
		String name;
		IManualPage[] pages;
		public ManualEntry(String name, IManualPage... pages)
		{
			this.name=name;
			this.pages=pages;
		}
	}
}