/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualInstance.ManualLink;
import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.*;

public class GuiManual extends GuiScreen
{
	int xSize = 186;
	int ySize = 198;
	int guiLeft;
	int guiTop;
	int manualTick=0;
	List<GuiButton> pageButtons = new ArrayList<>();

	public String selectedCategory;
	private ManualEntry selectedEntry;
	public Stack<ManualEntry> previousSelectedEntry = new Stack<>();
	public int page;
	public static GuiManual activeManual;

	ManualInstance manual;
	String texture;
	boolean buttonHeld = false;
	int[] lastClick;
	int[] lastDrag;
	GuiTextField searchField;
	int hasSuggestions = -1;
	int prevGuiScale = -1;

	public GuiManual(ManualInstance manual, String texture)
	{
		super();
		this.manual = manual;
		this.texture = texture;

		prevGuiScale = Minecraft.getMinecraft().gameSettings.guiScale;
		if(prevGuiScale!=0 && prevGuiScale!=2 && manual.allowGuiRescale())
			Minecraft.getMinecraft().gameSettings.guiScale=2;
		activeManual=this;
	}
	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	public ManualEntry getSelectedEntry()
	{
		return selectedEntry;
	}
	public void setSelectedEntry(ManualEntry entry)
	{
		selectedEntry = entry;
		if(entry!=null)
			manual.openEntry(entry);
	}
	public ManualInstance getManual()
	{
		return this.manual;
	}

	@Override
	public void initGui()
	{
		if(Minecraft.getMinecraft().gameSettings.guiScale==1)
		{
			Minecraft.getMinecraft().gameSettings.guiScale=2;
			ScaledResolution res = new ScaledResolution(this.mc);
			this.width = res.getScaledWidth();
			this.height = res.getScaledHeight();
			Minecraft.getMinecraft().gameSettings.guiScale=1;
		}
		this.manual.openManual();

		guiLeft =  (this.width - this.xSize) / 2;
		guiTop =  (this.height - this.ySize) / 2;
		boolean textField = false;

		this.buttonList.clear();
		this.pageButtons.clear();
		hasSuggestions = -1;
		if(selectedEntry!=null)
		{
			selectedEntry.addButtons(this, guiLeft+32,guiTop+28, pageButtons);
			buttonList.addAll(pageButtons);
		}
		else if(manual.getSortedCategoryList()==null||manual.getSortedCategoryList().length<=1)
		{
			ArrayList<ManualEntry> lHeaders = new ArrayList<>();
			for(ManualEntry e : manual.manualContents.values())
				if(manual.showEntryInList(e))
					lHeaders.add(e);
			ManualEntry[] entries = lHeaders.toArray(new ManualEntry[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft + 40, guiTop + 20, 100, 168,
					1f, entries));
			textField = true;
		}
		else if(manual.manualContents.containsKey(selectedCategory))
		{
			ArrayList<ManualEntry> lHeaders = new ArrayList<>();
			for(ManualEntry e : manual.manualContents.get(selectedCategory))
				if(manual.showEntryInList(e))
					lHeaders.add(e);
			ManualEntry[] entries = lHeaders.toArray(new ManualEntry[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft + 40, guiTop + 20, 100, 168,
					1f, entries));
			textField = true;
		}
		else
		{
			ArrayList<String> lHeaders = new ArrayList<>();
			for(String cat : manual.getSortedCategoryList())
				if(manual.showCategoryInList(cat))
					lHeaders.add(cat);
			String[] headers = lHeaders.toArray(new String[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft + 40, guiTop + 20, 100, 168,
					1f, headers));
			textField = true;
		}
		if(manual.manualContents.containsKey(selectedCategory) || selectedEntry!=null)
			this.buttonList.add(new GuiButtonManualNavigation(this, 1, guiLeft+24,guiTop+10, 10,10, 0));

		if(textField)
		{
			Keyboard.enableRepeatEvents(true);
			searchField = new GuiTextField(99, this.fontRenderer, guiLeft+166, guiTop+78, 120, 12);
			searchField.setTextColor(-1);
			searchField.setDisabledTextColour(-1);
			searchField.setEnableBackgroundDrawing(false);
			searchField.setMaxStringLength(17);
			searchField.setFocused(true);
			searchField.setCanLoseFocus(false);
		}
		else if(searchField!=null)
			searchField = null;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f)
	{
		manualTick++;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		boolean uni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(true);
		manual.entryRenderPre();

		ManualUtils.bindTexture(texture);
		this.drawTexturedModalRect(guiLeft,guiTop, 0,0, xSize,ySize);
		if(this.searchField!=null)
		{
			int l = searchField.getText()!=null?searchField.getText().length()*6:0;
			if(l>20)
				this.drawTexturedModalRect(guiLeft+166,guiTop+74, 136+(120-l),238, l,18);
			if(this.hasSuggestions!=-1 && this.hasSuggestions<this.buttonList.size())
			{
				this.drawTexturedModalRect(guiLeft+174,guiTop+100, 214,212, 16,26);
				int h = ((GuiClickableList)this.buttonList.get(hasSuggestions)).getFontHeight() * Math.min( ((GuiClickableList)this.buttonList.get(hasSuggestions)).perPage, ((GuiClickableList)this.buttonList.get(hasSuggestions)).headers.length);
				int w = 76;
				this.drawTexturedModalRect(guiLeft+174,guiTop+116, 230,212, 16,16);//Top Left
				this.drawTexturedModalRect(guiLeft+174,guiTop+132+h, 230,228, 16,10);//Bottom Left
				this.drawTexturedModalRect(guiLeft+190+w,guiTop+116, 246,212, 10,16);//Top Right
				this.drawTexturedModalRect(guiLeft+190+w,guiTop+132+h, 246,228, 10,10);//Bottom Right
				for(int hh=0; hh<h; hh++)
				{
					this.drawTexturedModalRect(guiLeft+174,guiTop+132+hh, 230,228, 16,1);
					for(int ww=0; ww<w; ww++)
						this.drawTexturedModalRect(guiLeft+190+ww,guiTop+132+hh, 246,228, 1,1);
					this.drawTexturedModalRect(guiLeft+190+w,guiTop+132+hh, 246,228, 10,1);
				}
				for(int ww=0; ww<w; ww++)
				{
					this.drawTexturedModalRect(guiLeft+190+ww,guiTop+116, 246,212, 1,16);
					this.drawTexturedModalRect(guiLeft+190+ww,guiTop+132+h, 246,228, 1,10);

				}
			}
		}

		if(selectedEntry!=null)
		{
			mouseX-=guiLeft;
			mouseY-=guiTop;
			boolean b0 = mouseX>32&&mouseX<32+17 && mouseY>179&&mouseY<179+10;
			boolean b1 = mouseX>135&&mouseX<135+17 && mouseY>179&&mouseY<179+10;

			GL11.glEnable(GL11.GL_BLEND);
			if(page>0)
				this.drawTexturedModalRect(guiLeft+ 32,guiTop+179, 0,216+(b0?20:0), 16,10);
			if(page<selectedEntry.getPageCount()-1)
				this.drawTexturedModalRect(guiLeft+136,guiTop+179, 0,226+(b1?20:0), 16,10);

			manual.titleRenderPre();
			//Title
			this.drawCenteredStringScaled(manual.fontRenderer, TextFormatting.BOLD+selectedEntry.getTitle(), guiLeft+xSize/2,guiTop+14, manual.getTitleColour(), 1, true);
			this.drawCenteredStringScaled(manual.fontRenderer, selectedEntry.getSubtext(), guiLeft+xSize/2,guiTop+22, manual.getSubTitleColour(), 1, true);
			//Page Number
			this.drawCenteredStringScaled(manual.fontRenderer, TextFormatting.BOLD.toString()+(page+1), guiLeft+xSize/2,guiTop+183, manual.getPagenumberColour(), 1, false);
			manual.titleRenderPost();

			GL11.glColor3f(1,1,1);
			selectedEntry.renderPage(this, guiLeft+32,guiTop+28, mouseX, mouseY);

			mouseX+=guiLeft;
			mouseY+=guiTop;
		}
		else
		{
			String title = manual.manualContents.containsKey(selectedCategory)?manual.formatCategoryName(selectedCategory) : manual.getManualName();
			manual.titleRenderPre();
			this.drawCenteredStringScaled(manual.fontRenderer, TextFormatting.BOLD+title, guiLeft+xSize/2,guiTop+12, manual.getTitleColour(), 1, true);
			manual.titleRenderPost();
		}
		if(this.searchField!=null)
		{
			manual.fontRenderer.setUnicodeFlag(true);
			this.searchField.drawTextBox();
			if(this.hasSuggestions!=-1 && this.hasSuggestions<this.buttonList.size())
				manual.fontRenderer.drawString("It looks like you meant:", guiLeft+180, guiTop+128, manual.getTextColour(), false);
		}
		manual.fontRenderer.setUnicodeFlag(uni);
		super.drawScreen(mouseX, mouseY, f);
		GlStateManager.enableBlend();
		manual.entryRenderPost();
	}

	@Override
	public void onGuiClosed()
	{
		this.manual.closeManual();
		super.onGuiClosed();
		if(prevGuiScale!=-1 && manual.allowGuiRescale())
			Minecraft.getMinecraft().gameSettings.guiScale = prevGuiScale;
	}

	@Override
	public void actionPerformed(GuiButton button)
	{
		if(buttonHeld)
			return;
		if(button instanceof GuiClickableList)
		{
			GuiClickableList clickList = ((GuiClickableList)button);
			int sel = clickList.selectedOption;
			if(sel>=0&&sel<clickList.headers.length)
			{
				if(clickList.entries==null)
					selectedCategory = clickList.headers[sel];
				else
				{
					if (button.id==0)
						previousSelectedEntry.clear();
					setSelectedEntry(clickList.entries[sel]);
				}
			}
			((GuiClickableList)button).selectedOption=-1;
			this.initGui();
		}
		else if(button.id == 1)
		{
			if(selectedEntry!=null)
				setSelectedEntry(previousSelectedEntry.isEmpty()?null:previousSelectedEntry.pop());
			else if(selectedCategory!=null)
				selectedCategory=null;
			page=0;
			this.initGui();
		}
		else if(pageButtons.contains(button) && selectedEntry!=null)
		{
			selectedEntry.buttonPressed(this, button);
		}
		buttonHeld=true;
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
	public List<String> getItemToolTip(ItemStack stack)
	{
		List<String> tooltip = super.getItemToolTip(stack);
		if(selectedEntry!=null)
		{
			if(selectedEntry.getHighlightedStack(page)==stack)
			{
				ManualLink link = this.manual.getManualLink(stack);
				if(link!=null)
					tooltip.add(manual.formatLink(link));
			}
		}
		return tooltip;
	}
	@Override
	public void drawHoveringText(List text, int x, int y, FontRenderer font)
	{
		manual.tooltipRenderPre();
		super.drawHoveringText(text,x,y,font);
		manual.tooltipRenderPost();
	}

	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		if(wheel!=0 && selectedEntry!=null)
		{
			if(wheel>0 && page>0)
			{
				page--;
				this.initGui();
			}
			else if(wheel<0 && page<selectedEntry.getPageCount()-1)
			{
				page++;
				this.initGui();
			}
		}
	}
	@Override
	public void mouseClicked(int mx, int my, int button) throws IOException
	{
		super.mouseClicked(mx,my,button);
		if(button==0 && selectedEntry!=null)
		{
			mx -= guiLeft;
			my -= guiTop;
			if(page>0 && mx>32&&mx<32+17 && my>179&&my<179+10)
			{
				page--;
				this.initGui();
			}
			else if(page<selectedEntry.getPageCount()-1 && mx>135&&mx<135+17 && my>179&&my<179+10)
			{
				page++;
				this.initGui();
			}
			else
			{
				ItemStack highlighted = selectedEntry.getHighlightedStack(page);
				if(!highlighted.isEmpty())
				{
					ManualLink link = this.getManual().getManualLink(highlighted);
					if(link!=null)
						link.changePage(this);
				}
			}
		}
		else if(button==1)
		{
			if(searchField!=null && searchField.getText()!=null && !searchField.getText().isEmpty())
				searchField.setText("");
			else if(selectedEntry!=null)
				setSelectedEntry(previousSelectedEntry.isEmpty()?null:previousSelectedEntry.pop());
			else if(selectedCategory!=null)
				selectedCategory=null;
			page=0;
			this.initGui();
		}
		lastClick = new int[]{mx,my};
		if(this.searchField!=null)
			this.searchField.mouseClicked(mx, my, button);
	}
	@Override
	protected void mouseReleased(int mx, int my, int action)
	{
		super.mouseReleased(mx, my, action);
		if(buttonHeld && (action==0||action==1))
			buttonHeld=false;
		lastClick = null;
		lastDrag = null;
	}
	@Override
	protected void mouseClickMove(int mx, int my, int button, long time)
	{
		if(lastClick!=null && selectedEntry!=null)
		{
			if(lastDrag==null)
				lastDrag = new int[]{mx-guiLeft,my-guiTop};
			selectedEntry.mouseDragged(this, guiLeft+32,guiTop+28, lastClick[0],lastClick[1], mx-guiLeft,
					my-guiTop, lastDrag[0],lastDrag[1], buttonList.get(button));
			lastDrag = new int[]{mx-guiLeft,my-guiTop};
		}
	}
	@Override
	protected void keyTyped(char c, int i) throws IOException
	{
		if(this.searchField!=null && this.searchField.textboxKeyTyped(c, i))
		{
			String search = searchField.getText();
			if(search==null || search.trim().isEmpty())
			{
				hasSuggestions = -1;
				this.initGui();
			}
			else
			{
				search = search.toLowerCase(Locale.ENGLISH);
				ArrayList<ManualEntry> lHeaders = new ArrayList<>();
				HashMap<String, ManualEntry> lSpellcheck = new HashMap<>();
				for(ManualEntry e : manual.manualContents.values())
				{
					if(manual.showEntryInList(e))
					{
						if(e.getTitle().contains(search))
							lHeaders.add(e);
						else
							lSpellcheck.put(e.getTitle(), e);
					}
				}
				ArrayList<String> lCorrections = ManualUtils.getPrimitiveSpellingCorrections(search, lSpellcheck.keySet().toArray(new String[lSpellcheck.keySet().size()]), 4);
				List<ManualEntry> lCorrectionEntries = new ArrayList<>(lCorrections.size());
				for(String key : lSpellcheck.keySet())
					if(!lCorrections.contains(key))
					{
						ManualEntry e = lSpellcheck.get(key);
						if (e.listForSearch(search))
						{
							lHeaders.add(e);
							lCorrectionEntries.add(e);
							break;
						}
					}

				ManualEntry[] entries = lHeaders.toArray(new ManualEntry[lHeaders.size()]);
				this.buttonList.set(0, new GuiClickableList(this, 0, guiLeft+40,guiTop+20, 100,148,
						1f, entries));
				if(!lCorrections.isEmpty())
				{
					GuiClickableList suggestions = new GuiClickableList(this, 11, guiLeft+180,guiTop+138, 100,80, 1f,
							lCorrectionEntries.toArray(new ManualEntry[lCorrectionEntries.size()]));
					if(hasSuggestions!=-1)
						this.buttonList.set(hasSuggestions, suggestions);
					else
					{
						hasSuggestions = this.buttonList.size();
						this.buttonList.add(suggestions);
					}
				}
				else if(hasSuggestions!=-1)
				{
					this.buttonList.remove(hasSuggestions);
					hasSuggestions = -1;
				}
			}
		}
		else
		{
			super.keyTyped(c, i);
		}
	}

}