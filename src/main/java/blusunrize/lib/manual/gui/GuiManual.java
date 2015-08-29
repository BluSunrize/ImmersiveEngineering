package blusunrize.lib.manual.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.ManualUtils;

public class GuiManual extends GuiScreen
{
	int xSize = 186;
	int ySize = 198;
	int guiLeft;
	int guiTop;
	int manualTick=0;
	List<GuiButton> pageButtons = new ArrayList();

	public static String selectedCategory;
	public static String selectedEntry;
	public static int page;
	public static GuiManual activeManual;

	ManualInstance manual;
	String texture;
	String[] headers;
	boolean backButtonPressed = false;
	int[] lastClick;
	int[] lastDrag;

	public GuiManual(ManualInstance manual, String texture)
	{
		super();
		this.manual = manual;
		this.texture = texture;

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
		headers=null;
		if(manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			IManualPage mPage = (page<0||page>=entry.getPages().length)?null: entry.getPages()[page];
			if(mPage!=null)
				mPage.initPage(this, guiLeft+32,guiTop+28, pageButtons);
			buttonList.addAll(pageButtons);
		}
		else if(manual.getSortedCategoryList()==null||manual.getSortedCategoryList().length<=1)
		{
			ArrayList<String> lHeaders = new ArrayList<String>();
			for(ManualEntry e : manual.manualContents.values())
				if(manual.showEntryInList(e))
					lHeaders.add(e.getName());
			headers = lHeaders.toArray(new String[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft+40,guiTop+20, 100,148, 1f, 1, headers));
		}
		else if(manual.manualContents.containsKey(selectedCategory))
		{
			ArrayList<String> lHeaders = new ArrayList<String>();
			for(ManualEntry e : manual.manualContents.get(selectedCategory))
				if(manual.showEntryInList(e))
					lHeaders.add(e.getName());
			headers = lHeaders.toArray(new String[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft+40,guiTop+20, 100,148, 1f, 1, headers));
		}
		else
		{
			ArrayList<String> lHeaders = new ArrayList<String>();
			for(String cat : manual.getSortedCategoryList())
				if(manual.showCategoryInList(cat))
					lHeaders.add(cat);
			headers = lHeaders.toArray(new String[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft+40,guiTop+20, 100,148, 1f, 0, headers));
		}
		if(manual.manualContents.containsKey(selectedCategory) || manual.getEntry(selectedEntry)!=null)
			this.buttonList.add(new GuiButtonManualNavigation(this, 1, guiLeft+24,guiTop+10, 10,10, 0));
	}

	@Override
	public void drawScreen(int mx, int my, float f)
	{
		manualTick++;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		ManualUtils.bindTexture(texture);
		this.drawTexturedModalRect(guiLeft,guiTop, 0,0, xSize,ySize);

		boolean uni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(true);
		if(manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			mx-=guiLeft;
			my-=guiTop;
			boolean b0 = mx>32&&mx<32+17 && my>179&&my<179+10;
			boolean b1 = mx>135&&mx<135+17 && my>179&&my<179+10;

			GL11.glEnable(GL11.GL_BLEND);
			if(page>0)
				this.drawTexturedModalRect(guiLeft+ 32,guiTop+179, 0,216+(b0?20:0), 16,10);
			if(page<entry.getPages().length-1)
				this.drawTexturedModalRect(guiLeft+136,guiTop+179, 0,226+(b1?20:0), 16,10);

			//Title
			this.drawCenteredStringScaled(manual.fontRenderer, EnumChatFormatting.BOLD+manual.formatEntryName(entry.getName()), guiLeft+xSize/2,guiTop+14, manual.getTitleColour(), 1, true);
			this.drawCenteredStringScaled(manual.fontRenderer, manual.formatEntrySubtext(entry.getName()), guiLeft+xSize/2,guiTop+22, manual.getSubTitleColour(), 1, true);
			//Page Number
			this.drawCenteredStringScaled(manual.fontRenderer, EnumChatFormatting.BOLD.toString()+(page+1), guiLeft+xSize/2,guiTop+183, manual.getPagenumberColour(), 1, false);

			GL11.glColor3f(1,1,1);
			IManualPage mPage = (page<0||page>=entry.getPages().length)?null: entry.getPages()[page];
			if(mPage!=null)
				mPage.renderPage(this, guiLeft+32,guiTop+28, mx+guiLeft,my+guiTop);

			mx+=guiLeft;
			my+=guiTop;
		}
		else
		{
			String title = manual.manualContents.containsKey(selectedCategory)?manual.formatCategoryName(selectedCategory) : manual.getManualName();
			this.drawCenteredStringScaled(manual.fontRenderer, EnumChatFormatting.BOLD+title, guiLeft+xSize/2,guiTop+12, manual.getTitleColour(), 1, true);
		}
		manual.fontRenderer.setUnicodeFlag(uni);
		super.drawScreen(mx, my, f);
	}

	@Override
	public void actionPerformed(GuiButton button)
	{
		if(button.id == 0)
		{
			int sel = ((GuiClickableList)button).selectedOption;
			if(sel>=0&&sel<headers.length)
			{
				if(manual.manualContents.keySet().size()==1 || manual.manualContents.containsKey(selectedCategory))
					selectedEntry = headers[sel];
				else
					selectedCategory = headers[sel];
			}
			((GuiClickableList)button).selectedOption=-1;
			this.initGui();
		}
		if(button.id == 1 && !backButtonPressed)
		{
			if(selectedEntry!=null)
				selectedEntry=null;
			else if(selectedCategory!=null)
				selectedCategory=null;
			page=0;
			backButtonPressed=true;
			this.initGui();
		}
		if(pageButtons.contains(button) && manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			IManualPage mPage = (page<0||page>=entry.getPages().length)?null: entry.getPages()[page];
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
	public void drawGradientRect(int x1, int y1, int x2, int y2, int colour1, int colour2)
	{
		super.drawGradientRect(x1, y1, x2, y2, colour1, colour2);
		GL11.glEnable(GL11.GL_BLEND);
	}
	@Override
	public void renderToolTip(ItemStack stack, int x, int y)
	{
		super.renderToolTip(stack, x, y);
	}
	@Override
	public void drawHoveringText(List text, int x, int y, FontRenderer font)
	{
		super.drawHoveringText(text,x,y,font);
	}

	@Override
	public void handleMouseInput()
	{
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		if(wheel!=0 && manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			if(wheel>0 && page>0)
			{
				page--;
				this.initGui();
			}
			else if(wheel<0 && page<entry.getPages().length-1)
			{
				page++;
				this.initGui();
			}
		}
	}
	@Override
	public void mouseClicked(int mx, int my, int button)
	{
		super.mouseClicked(mx,my,button);
		if(button==0 && manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			mx -= guiLeft;
			my -= guiTop;
			if(page>0 && mx>32&&mx<32+17 && my>179&&my<179+10)
			{
				page--;
				this.initGui();
			}
			if(page<entry.getPages().length-1 && mx>135&&mx<135+17 && my>179&&my<179+10)
			{
				page++;
				this.initGui();
			}

		}
		else if(button==1)
		{
			if(selectedEntry!=null)
				selectedEntry=null;
			else if(selectedCategory!=null)
				selectedCategory=null;
			page=0;
			this.initGui();
		}
		lastClick = new int[]{mx,my};
	}
	@Override
	protected void mouseMovedOrUp(int mx, int my, int action)
	{
		super.mouseMovedOrUp(mx, my, action);
		if(backButtonPressed && (action==0||action==1))
			backButtonPressed=false;
		lastClick = null;
		lastDrag = null;
	}
	@Override
	protected void mouseClickMove(int mx, int my, int button, long time)
	{
		if(lastClick!=null && manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			if(lastDrag==null)
				lastDrag = new int[]{mx-guiLeft,my-guiTop};
			entry.getPages()[page].mouseDragged(guiLeft+32,guiTop+28, lastClick[0],lastClick[1], mx-guiLeft,my-guiTop, lastDrag[0],lastDrag[1], button);
			lastDrag = new int[]{mx-guiLeft,my-guiTop};
		}
	}


}