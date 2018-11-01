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
import blusunrize.lib.manual.Tree.AbstractNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

public class GuiManual extends GuiScreen
{
	private int xSize = 186;
	private int ySize = 198;
	private int guiLeft;
	private int guiTop;
	private int manualTick = 0;
	private List<GuiButton> pageButtons = new ArrayList<>();

	@Nonnull
	public AbstractNode<ResourceLocation, ManualEntry> currentNode;
	public Stack<ManualLink> previousSelectedEntry = new Stack<>();
	public int page;
	public static GuiManual activeManual;

	ManualInstance manual;
	String texture;
	private boolean buttonHeld = false;
	private int[] lastClick;
	private int[] lastDrag;
	private GuiTextField searchField;
	private int hasSuggestions = -1;
	private int prevGuiScale = -1;

	public GuiManual(ManualInstance manual, String texture)
	{
		super();
		this.manual = manual;
		this.currentNode = manual.contentTree.getRoot();
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

	public ManualEntry getCurrentPage()
	{
		return currentNode.getLeafData();
	}

	public void setCurrentNode(@Nonnull AbstractNode<ResourceLocation, ManualEntry> entry)
	{
		currentNode = entry;
		if(currentNode.isLeaf())
			manual.openEntry(currentNode.getLeafData());
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
		hasSuggestions = -1;
		if(currentNode.isLeaf())
		{
			currentNode.getLeafData().addButtons(this, guiLeft+32, guiTop+28, page, pageButtons);
			buttonList.addAll(pageButtons);
		}
		else
		{
			ArrayList<AbstractNode<ResourceLocation, ManualEntry>> children = new ArrayList<>();
			for(AbstractNode<ResourceLocation, ManualEntry> node : currentNode.getChildren())
				if(manual.showNodeInList(node))
					children.add(node);
			this.buttonList.add(new GuiClickableList(this, 0, guiLeft+40, guiTop+20, 100, 168,
					1f, children));
			textField = true;
		}
		if(currentNode.getSuperNode()!=null)
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
	public void drawScreen(int mouseX, int mouseY, float f)
	{
		manualTick++;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		boolean uni = manual.fontRenderer.getUnicodeFlag();
		manual.fontRenderer.setUnicodeFlag(true);
		manual.entryRenderPre();

		ManualUtils.bindTexture(texture);
		this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		if(this.searchField!=null)
		{
			int l = searchField.getText().length()*6;
			if(l > 20)
				this.drawTexturedModalRect(guiLeft+166, guiTop+74, 136+(120-l), 238, l, 18);
			if(this.hasSuggestions!=-1&&this.hasSuggestions < this.buttonList.size())
			{
				this.drawTexturedModalRect(guiLeft+174, guiTop+100, 214, 212, 16, 26);
				int h = ((GuiClickableList)this.buttonList.get(hasSuggestions)).getFontHeight()*Math.min(((GuiClickableList)this.buttonList.get(hasSuggestions)).perPage, ((GuiClickableList)this.buttonList.get(hasSuggestions)).headers.length);
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

		if(currentNode.isLeaf())
		{
			ManualEntry selectedEntry = currentNode.getLeafData();
			mouseX -= guiLeft;
			mouseY -= guiTop;
			boolean b0 = mouseX > 32&&mouseX < 32+17&&mouseY > 179&&mouseY < 179+10;
			boolean b1 = mouseX > 135&&mouseX < 135+17&&mouseY > 179&&mouseY < 179+10;

			GL11.glEnable(GL11.GL_BLEND);
			if(page > 0)
				this.drawTexturedModalRect(guiLeft+32, guiTop+179, 0, 216+(b0?20: 0), 16, 10);
			if(page < selectedEntry.getPageCount()-1)
				this.drawTexturedModalRect(guiLeft+136, guiTop+179, 0, 226+(b1?20: 0), 16, 10);

			manual.titleRenderPre();
			//Title
			this.drawCenteredStringScaled(manual.fontRenderer, TextFormatting.BOLD+selectedEntry.getTitle(), guiLeft+xSize/2, guiTop+14, manual.getTitleColour(), 1, true);
			this.drawCenteredStringScaled(manual.fontRenderer, selectedEntry.getSubtext(), guiLeft+xSize/2, guiTop+22, manual.getSubTitleColour(), 1, true);
			//Page Number
			this.drawCenteredStringScaled(manual.fontRenderer, TextFormatting.BOLD.toString()+(page+1), guiLeft+xSize/2, guiTop+183, manual.getPagenumberColour(), 1, false);
			manual.titleRenderPost();

			GL11.glColor3f(1, 1, 1);
			selectedEntry.renderPage(this, guiLeft+32, guiTop+28, mouseX-32, mouseY-28);

			mouseX += guiLeft;
			mouseY += guiTop;
		}
		else
		{
			String title = ManualUtils.getTitleForNode(currentNode);
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
		for (GuiButton btn:pageButtons)
			btn.drawButton(mc, mouseX, mouseY, f);
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
		if(prevGuiScale!=-1&&manual.allowGuiRescale())
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
			if(sel >= 0&&sel < clickList.headers.length)
			{
				if(button.id==0)
					previousSelectedEntry.clear();
				setCurrentNode(clickList.nodes.get(sel));
			}
			((GuiClickableList)button).selectedOption = -1;
			this.initGui();
		}
		else if(button.id==1)
		{
			if(currentNode.isLeaf()&&!previousSelectedEntry.isEmpty())
				previousSelectedEntry.pop().changePage(this, false);
			else if(currentNode.getSuperNode()!=null)
				setCurrentNode(currentNode.getSuperNode());
			page = 0;
			this.initGui();
		}
		else if(pageButtons.contains(button)&&currentNode.isLeaf())
		{
			currentNode.getLeafData().buttonPressed(this, button);
		}
		buttonHeld = true;
	}


	private void drawCenteredStringScaled(FontRenderer fr, String s, int x, int y, int colour, float scale, boolean shadow)
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
	public void renderToolTip(ItemStack stack, int x, int y)
	{
		super.renderToolTip(stack, x, y);
	}

	//Change access from protected to public
	@Override
	public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor)
	{
		super.drawGradientRect(left, top, right, bottom, startColor, endColor);
		GlStateManager.enableBlend();
	}

	@Override
	public List<String> getItemToolTip(ItemStack stack)
	{
		List<String> tooltip = super.getItemToolTip(stack);
		if(currentNode.isLeaf())
		{
			if(currentNode.getLeafData().getHighlightedStack(page)==stack)
			{
				ManualLink link = this.manual.getManualLink(stack);
				if(link!=null)
					tooltip.add(manual.formatLink(link));
			}
		}
		return tooltip;
	}

	@Override
	public void drawHoveringText(List<String> text, int x, int y, @Nonnull FontRenderer font)
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
		if(wheel!=0&&currentNode.isLeaf())
		{
			if(wheel > 0&&page > 0)
			{
				page--;
				this.initGui();
			}
			else if(wheel < 0&&page < currentNode.getLeafData().getPageCount()-1)
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
		if(button==0&&currentNode.isLeaf())
		{
			ManualEntry selectedEntry = currentNode.getLeafData();
			mx -= guiLeft;
			my -= guiTop;
			if(page > 0&&mx > 32&&mx < 32+17&&my > 179&&my < 179+10)
			{
				page--;
				this.initGui();
			}
			else if(page < selectedEntry.getPageCount()-1&&mx > 135&&mx < 135+17&&my > 179&&my < 179+10)
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
						link.changePage(this, true);
				}
			}
		}
		else if(button==1)
		{
			if(searchField!=null&&!searchField.getText().isEmpty())
				searchField.setText("");
			else if(currentNode.isLeaf()&&!previousSelectedEntry.isEmpty())
				previousSelectedEntry.pop().changePage(this, false);
			else if(currentNode.getSuperNode()!=null)
			{
				setCurrentNode(currentNode.getSuperNode());
				page = 0;
			}
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
		if(lastClick!=null&&currentNode.isLeaf())
		{
			if(lastDrag==null)
				lastDrag = new int[]{mx-guiLeft, my-guiTop};
			currentNode.getLeafData().mouseDragged(this, guiLeft+32, guiTop+28, lastClick[0], lastClick[1], mx-guiLeft,
					my-guiTop, lastDrag[0], lastDrag[1], buttonList.get(button));
			lastDrag = new int[]{mx-guiLeft, my-guiTop};
		}
	}

	@Override
	protected void keyTyped(char c, int i) throws IOException
	{
		if(this.searchField!=null&&this.searchField.textboxKeyTyped(c, i))
		{
			String search = searchField.getText();
			if(search.trim().isEmpty())
			{
				hasSuggestions = -1;
				this.initGui();
			}
			else
			{
				search = search.toLowerCase(Locale.ENGLISH);
				ArrayList<AbstractNode<ResourceLocation, ManualEntry>> lHeaders = new ArrayList<>();
				Set<AbstractNode<ResourceLocation, ManualEntry>> lSpellcheck = new HashSet<>();
				final String searchFinal = search;
				manual.contentTree.fullStream().forEach((node) ->
				{
					if(manual.showNodeInList(node))
					{
						String title = ManualUtils.getTitleForNode(node).toLowerCase(Locale.ENGLISH);
						if(title.contains(searchFinal))
							lHeaders.add(node);
						else
							lSpellcheck.add(node);
					}
				});
				List<AbstractNode<ResourceLocation, ManualEntry>> lCorrections =
						ManualUtils.getPrimitiveSpellingCorrections(search, lSpellcheck, 4,
								ManualUtils::getTitleForNode);
				for(AbstractNode<ResourceLocation, ManualEntry> node : lSpellcheck)
					if(!lCorrections.contains(node))
					{
						if(node.isLeaf()&&node.getLeafData().listForSearch(search))
						{
							lHeaders.add(node);
							lCorrections.add(node);
							break;
						}
					}

				this.buttonList.set(0, new GuiClickableList(this, 0, guiLeft+40, guiTop+20, 100, 148,
						1f, lHeaders));
				if(!lCorrections.isEmpty())
				{
					GuiClickableList suggestions = new GuiClickableList(this, 11, guiLeft+180, guiTop+138, 100, 80, 1f,
							lCorrections);
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