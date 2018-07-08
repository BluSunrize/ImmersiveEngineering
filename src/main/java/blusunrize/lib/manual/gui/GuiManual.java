/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.lib.manual.IManualPage;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualInstance.ManualEntry;
import blusunrize.lib.manual.ManualInstance.ManualLink;
import blusunrize.lib.manual.ManualUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.*;

public class GuiManual extends GuiScreen
{
	int xSize = 186;
	int ySize = 198;
	int guiLeft;
	int guiTop;
	int manualTick = 0;
	List<GuiButton> pageButtons = new ArrayList();

	public String selectedCategory;
	private String selectedEntry;
	public Stack<String> previousSelectedEntry = new Stack();
	public int page;
	public static GuiManual activeManual;

	ManualInstance manual;
	String texture;
	String[] headers = new String[0];
	boolean buttonHeld = false;
	int[] lastClick;
	int[] lastDrag;
	GuiTextField searchField;
	int hasSuggestions = -1;
	String[] suggestionHeaders = new String[0];
	int prevGuiScale = -1;

	public GuiManual(ManualInstance manual, String texture)
	{
		super();
		this.manual = manual;
		this.texture = texture;

		prevGuiScale = Minecraft.getMinecraft().gameSettings.guiScale;
		if(prevGuiScale!=0&&prevGuiScale!=2&&manual.allowGuiRescale())
			Minecraft.getMinecraft().gameSettings.guiScale = 2;
		activeManual = this;
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	public String getSelectedEntry()
	{
		return selectedEntry;
	}

	public void setSelectedEntry(String string)
	{
		selectedEntry = string;
		if(string!=null)
			manual.openEntry(string);
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
			Minecraft.getMinecraft().gameSettings.guiScale = 2;
			ScaledResolution res = new ScaledResolution(this.mc);
			this.width = res.getScaledWidth();
			this.height = res.getScaledHeight();
			Minecraft.getMinecraft().gameSettings.guiScale = 1;
		}
		this.manual.openManual();

		guiLeft = (this.width-this.xSize)/2;
		guiTop = (this.height-this.ySize)/2;
		boolean textField = false;

		this.buttonList.clear();
		this.pageButtons.clear();
		headers = new String[0];
		suggestionHeaders = new String[0];
		hasSuggestions = -1;
		if(manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			IManualPage mPage = (page < 0||page >= entry.getPages().length)?null: entry.getPages()[page];
			if(mPage!=null)
				mPage.initPage(this, guiLeft+32, guiTop+28, pageButtons);
			buttonList.addAll(pageButtons);
		}
		else if(manual.getSortedCategoryList()==null||manual.getSortedCategoryList().length <= 1)
		{
			ArrayList<String> lHeaders = new ArrayList<String>();
			for(ManualEntry e : manual.manualContents.values())
				if(manual.showEntryInList(e))
					lHeaders.add(e.getName());
			headers = lHeaders.toArray(new String[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft+40, guiTop+20, 100, 168, 1f, 1, headers));
			textField = true;
		}
		else if(manual.manualContents.containsKey(selectedCategory))
		{
			ArrayList<String> lHeaders = new ArrayList<String>();
			for(ManualEntry e : manual.manualContents.get(selectedCategory))
				if(manual.showEntryInList(e))
					lHeaders.add(e.getName());
			headers = lHeaders.toArray(new String[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft+40, guiTop+20, 100, 168, 1f, 1, headers));
			textField = true;
		}
		else
		{
			ArrayList<String> lHeaders = new ArrayList<String>();
			for(String cat : manual.getSortedCategoryList())
				if(manual.showCategoryInList(cat))
					lHeaders.add(cat);
			headers = lHeaders.toArray(new String[lHeaders.size()]);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft+40, guiTop+20, 100, 168, 1f, 0, headers));
			textField = true;
		}
		if(manual.manualContents.containsKey(selectedCategory)||manual.getEntry(selectedEntry)!=null)
			this.buttonList.add(new GuiButtonManualNavigation(this, 1, guiLeft+24, guiTop+10, 10, 10, 0));

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
	public void drawScreen(int mx, int my, float f)
	{
		manualTick++;
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		boolean uni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(true);
		manual.entryRenderPre();

		ManualUtils.bindTexture(texture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		if(this.searchField!=null)
		{
			int l = searchField.getText()!=null?searchField.getText().length()*6: 0;
			if(l > 20)
				this.drawTexturedModalRect(guiLeft+166, guiTop+74, 136+(120-l), 238, l, 18);
			if(this.hasSuggestions!=-1&&this.hasSuggestions < this.buttonList.size())
			{
				this.drawTexturedModalRect(guiLeft+174, guiTop+100, 214, 212, 16, 26);
				int h = ((GuiClickableList)this.buttonList.get(hasSuggestions)).getFontHeight()*Math.min(((GuiClickableList)this.buttonList.get(hasSuggestions)).perPage, ((GuiClickableList)this.buttonList.get(hasSuggestions)).entries.length);
				int w = 76;
				this.drawTexturedModalRect(guiLeft+174, guiTop+116, 230, 212, 16, 16);//Top Left
				this.drawTexturedModalRect(guiLeft+174, guiTop+132+h, 230, 228, 16, 10);//Bottom Left
				this.drawTexturedModalRect(guiLeft+190+w, guiTop+116, 246, 212, 10, 16);//Top Right
				this.drawTexturedModalRect(guiLeft+190+w, guiTop+132+h, 246, 228, 10, 10);//Bottom Right
				for(int hh = 0; hh < h; hh++)
				{
					this.drawTexturedModalRect(guiLeft+174, guiTop+132+hh, 230, 228, 16, 1);
					for(int ww = 0; ww < w; ww++)
						this.drawTexturedModalRect(guiLeft+190+ww, guiTop+132+hh, 246, 228, 1, 1);
					this.drawTexturedModalRect(guiLeft+190+w, guiTop+132+hh, 246, 228, 10, 1);
				}
				for(int ww = 0; ww < w; ww++)
				{
					this.drawTexturedModalRect(guiLeft+190+ww, guiTop+116, 246, 212, 1, 16);
					this.drawTexturedModalRect(guiLeft+190+ww, guiTop+132+h, 246, 228, 1, 10);

				}
			}
		}

		if(manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			mx -= guiLeft;
			my -= guiTop;
			boolean b0 = mx > 32&&mx < 32+17&&my > 179&&my < 179+10;
			boolean b1 = mx > 135&&mx < 135+17&&my > 179&&my < 179+10;

			GlStateManager.enableBlend();
			if(page > 0)
				this.drawTexturedModalRect(guiLeft+32, guiTop+179, 0, 216+(b0?20: 0), 16, 10);
			if(page < entry.getPages().length-1)
				this.drawTexturedModalRect(guiLeft+136, guiTop+179, 0, 226+(b1?20: 0), 16, 10);

			manual.titleRenderPre();
			//Title
			this.drawCenteredStringScaled(manual.fontRenderer, TextFormatting.BOLD+manual.formatEntryName(entry.getName()), guiLeft+xSize/2, guiTop+14, manual.getTitleColour(), 1, true);
			this.drawCenteredStringScaled(manual.fontRenderer, manual.formatEntrySubtext(entry.getName()), guiLeft+xSize/2, guiTop+22, manual.getSubTitleColour(), 1, true);
			//Page Number
			this.drawCenteredStringScaled(manual.fontRenderer, TextFormatting.BOLD.toString()+(page+1), guiLeft+xSize/2, guiTop+183, manual.getPagenumberColour(), 1, false);
			manual.titleRenderPost();

			GlStateManager.color(1, 1, 1);
			IManualPage mPage = (page < 0||page >= entry.getPages().length)?null: entry.getPages()[page];
			if(mPage!=null)
				mPage.renderPage(this, guiLeft+32, guiTop+28, mx+guiLeft, my+guiTop);

			mx += guiLeft;
			my += guiTop;
		}
		else
		{
			String title = manual.manualContents.containsKey(selectedCategory)?manual.formatCategoryName(selectedCategory): manual.getManualName();
			manual.titleRenderPre();
			this.drawCenteredStringScaled(manual.fontRenderer, TextFormatting.BOLD+title, guiLeft+xSize/2, guiTop+12, manual.getTitleColour(), 1, true);
			manual.titleRenderPost();
		}
		if(this.searchField!=null)
		{
			manual.fontRenderer.setUnicodeFlag(true);
			this.searchField.drawTextBox();
			if(this.hasSuggestions!=-1&&this.hasSuggestions < this.buttonList.size())
				manual.fontRenderer.drawString("It looks like you meant:", guiLeft+180, guiTop+128, manual.getTextColour(), false);
		}
		manual.fontRenderer.setUnicodeFlag(uni);
		super.drawScreen(mx, my, f);
		GlStateManager.enableBlend();
		manual.entryRenderPost();
	}

	@Override
	public void onGuiClosed()
	{
		this.manual.closeManual();
		super.onGuiClosed();
		if(prevGuiScale!=-1&&manual.allowGuiRescale())
			Minecraft.getMinecraft().gameSettings.guiScale = prevGuiScale;
	}

	@Override
	public void actionPerformed(GuiButton button)
	{
		if(buttonHeld)
			return;
		if(button.id==0)
		{
			int sel = ((GuiClickableList)button).selectedOption;
			if(sel >= 0&&sel < headers.length)
			{
				if(((GuiClickableList)button).translationType==0)
					selectedCategory = headers[sel];
				else
				{
					previousSelectedEntry.clear();
					setSelectedEntry(headers[sel]);
				}
			}
			((GuiClickableList)button).selectedOption = -1;
			this.initGui();
		}
		else if(button.id==11)
		{
			int sel = ((GuiClickableList)button).selectedOption;
			if(sel >= 0&&sel < suggestionHeaders.length)
				setSelectedEntry(suggestionHeaders[sel]);
			((GuiClickableList)button).selectedOption = -1;
			this.initGui();
		}
		else if(button.id==1)
		{
			if(selectedEntry!=null)
				setSelectedEntry(previousSelectedEntry.isEmpty()?null: previousSelectedEntry.pop());
			else if(selectedCategory!=null)
				selectedCategory = null;
			page = 0;
			this.initGui();
		}
		else if(pageButtons.contains(button)&&manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			IManualPage mPage = (page < 0||page >= entry.getPages().length)?null: entry.getPages()[page];
			if(mPage!=null)
				mPage.buttonPressed(this, button);
		}
		buttonHeld = true;
	}


	public void drawCenteredStringScaled(FontRenderer fr, String s, int x, int y, int colour, float scale, boolean shadow)
	{
		int xx = (int)Math.floor(x/scale-(fr.getStringWidth(s)/2));
		int yy = (int)Math.floor(y/scale-(fr.FONT_HEIGHT/2));
		if(scale!=1)
			GlStateManager.scale(scale, scale, scale);
		fr.drawString(s, xx, yy, colour, shadow);
		if(scale!=1)
			GlStateManager.scale(1/scale, 1/scale, 1/scale);
	}

	@Override
	public void drawGradientRect(int x1, int y1, int x2, int y2, int colour1, int colour2)
	{
		super.drawGradientRect(x1, y1, x2, y2, colour1, colour2);
		GlStateManager.enableBlend();
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
		ManualEntry entry = manual.getEntry(selectedEntry);
		if(entry!=null)
		{
			IManualPage mPage = (page < 0||page >= entry.getPages().length)?null: entry.getPages()[page];
			if(mPage!=null&&mPage.getHighlightedStack()==stack)
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
		super.drawHoveringText(text, x, y, font);
		manual.tooltipRenderPost();
	}

	@Override
	public void handleMouseInput() throws IOException
	{
		super.handleMouseInput();
		int wheel = Mouse.getEventDWheel();
		if(wheel!=0&&manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			if(wheel > 0&&page > 0)
			{
				page--;
				this.initGui();
			}
			else if(wheel < 0&&page < entry.getPages().length-1)
			{
				page++;
				this.initGui();
			}
		}
	}

	@Override
	public void mouseClicked(int mx, int my, int button) throws IOException
	{
		super.mouseClicked(mx, my, button);
		if(button==0&&manual.getEntry(selectedEntry)!=null)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			mx -= guiLeft;
			my -= guiTop;
			if(page > 0&&mx > 32&&mx < 32+17&&my > 179&&my < 179+10)
			{
				page--;
				this.initGui();
			}
			else if(page < entry.getPages().length-1&&mx > 135&&mx < 135+17&&my > 179&&my < 179+10)
			{
				page++;
				this.initGui();
			}
			else
			{
				IManualPage mPage = (page < 0||page >= entry.getPages().length)?null: entry.getPages()[page];
				if(mPage!=null)
				{
					ItemStack highlighted = mPage.getHighlightedStack();
					if(!highlighted.isEmpty())
					{
						ManualLink link = this.getManual().getManualLink(highlighted);
						if(link!=null)
							link.changePage(this);
					}
				}
			}
		}
		else if(button==1)
		{
			if(searchField!=null&&searchField.getText()!=null&&!searchField.getText().isEmpty())
				searchField.setText("");
			else if(selectedEntry!=null)
				setSelectedEntry(previousSelectedEntry.isEmpty()?null: previousSelectedEntry.pop());
			else if(selectedCategory!=null)
				selectedCategory = null;
			page = 0;
			this.initGui();
		}
		lastClick = new int[]{mx, my};
		if(this.searchField!=null)
			this.searchField.mouseClicked(mx, my, button);
	}

	@Override
	protected void mouseReleased(int mx, int my, int action)
	{
		super.mouseReleased(mx, my, action);
		if(buttonHeld&&(action==0||action==1))
			buttonHeld = false;
		lastClick = null;
		lastDrag = null;
	}

	@Override
	protected void mouseClickMove(int mx, int my, int button, long time)
	{
		if(lastClick!=null&&manual.getEntry(selectedEntry)!=null&&page < manual.getEntry(selectedEntry).getPages().length)
		{
			ManualEntry entry = manual.getEntry(selectedEntry);
			if(lastDrag==null)
				lastDrag = new int[]{mx-guiLeft, my-guiTop};
			entry.getPages()[page].mouseDragged(guiLeft+32, guiTop+28, lastClick[0], lastClick[1], mx-guiLeft, my-guiTop, lastDrag[0], lastDrag[1], button);
			lastDrag = new int[]{mx-guiLeft, my-guiTop};
		}
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException
	{
		if(this.searchField!=null&&this.searchField.textboxKeyTyped(c, i))
		{
			String search = searchField.getText();
			if(search==null||search.trim().isEmpty())
			{
				hasSuggestions = -1;
				this.initGui();
			}
			else
			{
				search = search.toLowerCase(Locale.ENGLISH);
				ArrayList<String> lHeaders = new ArrayList<String>();
				HashMap<String, String> lSpellcheck = new HashMap<String, String>();
				for(ManualEntry e : manual.manualContents.values())
				{
					if(manual.showEntryInList(e))
					{
						if(manual.formatEntryName(e.getName()).toLowerCase(Locale.ENGLISH).contains(search))
							lHeaders.add(e.getName());
						else
							lSpellcheck.put(manual.formatEntryName(e.getName()), e.getName());
					}
				}
				ArrayList<String> lCorrections = ManualUtils.getPrimitiveSpellingCorrections(search, lSpellcheck.keySet().toArray(new String[lSpellcheck.keySet().size()]), 4);
				for(String key : lSpellcheck.keySet())
					if(!lCorrections.contains(key))
					{
						ManualEntry e = manual.getEntry(lSpellcheck.get(key));
						for(IManualPage page : e.getPages())
							if(page.listForSearch(search))
							{
								lHeaders.add(e.getName());
								break;
							}
					}

				headers = lHeaders.toArray(new String[lHeaders.size()]);
				this.buttonList.set(0, new GuiClickableList(this, 0, guiLeft+40, guiTop+20, 100, 148, 1f, 1, headers));
				if(!lCorrections.isEmpty())
				{
					GuiClickableList suggestions = new GuiClickableList(this, 11, guiLeft+180, guiTop+138, 100, 80, 1f, -1, lCorrections.toArray(new String[0]));
					if(hasSuggestions!=-1)
						this.buttonList.set(hasSuggestions, suggestions);
					else
					{
						hasSuggestions = this.buttonList.size();
						this.buttonList.add(suggestions);
					}
					this.suggestionHeaders = new String[lCorrections.size()];
					for(int j = 0; j < this.suggestionHeaders.length; j++)
						this.suggestionHeaders[j] = lSpellcheck.get(lCorrections.get(j));
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