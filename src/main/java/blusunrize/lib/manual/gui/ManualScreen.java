/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.gui;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualInstance.ManualLink;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.Tree.AbstractNode;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.*;

public class ManualScreen extends Screen
{
	private Minecraft mc = Minecraft.getInstance();
	private int xSize = 186;
	private int ySize = 198;
	private int guiLeft;
	private int guiTop;
	private int manualTick = 0;
	private List<Button> pageButtons = new ArrayList<>();

	@Nonnull
	public AbstractNode<ResourceLocation, ManualEntry> currentNode;
	public Stack<ManualLink> previousSelectedEntry = new Stack<>();
	public int page;
	public static ManualScreen activeManual;

	ManualInstance manual;
	String texture;
	private double[] lastClick;
	private double[] lastDrag;
	private TextFieldWidget searchField;
	private int prevGuiScale = -1;
	private ClickableList entryList;
	private ClickableList suggestionList;

	public ManualScreen(ManualInstance manual, String texture)
	{
		super(new StringTextComponent("manual"));
		this.manual = manual;
		this.currentNode = manual.contentTree.getRoot();
		this.texture = texture;

		prevGuiScale = mc.gameSettings.guiScale;
		if(prevGuiScale!=0&&prevGuiScale!=2&&manual.allowGuiRescale())
			mc.gameSettings.guiScale = 2;
		activeManual = this;
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
	public void init()
	{
		if(mc.gameSettings.guiScale==1)
		{
			mc.gameSettings.guiScale = 2;
			this.width = mc.mainWindow.getScaledWidth();
			this.height = mc.mainWindow.getScaledHeight();
			mc.gameSettings.guiScale = 1;
		}
		this.manual.openManual();

		guiLeft = (this.width-this.xSize)/2;
		guiTop = (this.height-this.ySize)/2;
		boolean textField = false;

		this.pageButtons.clear();
		if(currentNode.isLeaf())
		{
			currentNode.getLeafData().addButtons(this, guiLeft+32, guiTop+28, page, pageButtons);
			for(Button b : pageButtons)
				addButton(b);
		}
		else
		{
			List<AbstractNode<ResourceLocation, ManualEntry>> children = new ArrayList<>();
			for(AbstractNode<ResourceLocation, ManualEntry> node : currentNode.getChildren())
				if(manual.showNodeInList(node))
					children.add(node);
			entryList = new ClickableList(this, guiLeft+40, guiTop+20, 100, 168,
					1f, children, sel -> {
				if(sel!=null)
				{
					previousSelectedEntry.clear();
					setCurrentNode(sel);
					ManualScreen.this.fullInit();
				}
			});
			addButton(entryList);
			suggestionList = new ClickableList(this, guiLeft+180, guiTop+138, 100, 80, 1f,
					new ArrayList<>(), sel -> {
			});
			suggestionList.visible = false;
			addButton(suggestionList);
			textField = true;
		}
		if(currentNode.getSuperNode()!=null)
			addButton(new GuiButtonManualNavigation(this, guiLeft+24, guiTop+10, 10, 10, 0,
					btn -> {
						if(currentNode.isLeaf()&&!previousSelectedEntry.isEmpty())
							previousSelectedEntry.pop().changePage(ManualScreen.this, false);
						else if(currentNode.getSuperNode()!=null)
							setCurrentNode(currentNode.getSuperNode());
						page = 0;
						ManualScreen.this.fullInit();
					}));

		if(textField)
		{
			mc.keyboardListener.enableRepeatEvents(true);
			searchField = new TextFieldWidget(font, guiLeft+166, guiTop+78, 120, 12, "");
			searchField.setTextColor(-1);
			searchField.setDisabledTextColour(-1);
			searchField.setEnableBackgroundDrawing(false);
			searchField.setMaxStringLength(17);
			searchField.setFocused2(true);
			searchField.setCanLoseFocus(false);
		}
		else if(searchField!=null)
			searchField = null;
	}

	public void fullInit()
	{
		super.init(minecraft, width, height);
	}

	@Override
	public void render(int mouseX, int mouseY, float f)
	{
		manualTick++;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		manual.entryRenderPre();

		ManualUtils.bindTexture(texture);
		this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);
		if(this.searchField!=null)
		{
			int l = searchField.getText().length()*6;
			if(l > 20)
				this.blit(guiLeft+166, guiTop+74, 136+(120-l), 238, l, 18);
			if(suggestionList.visible)
			{
				this.blit(guiLeft+174, guiTop+100, 214, 212, 16, 26);
				int h = suggestionList.getHeight();
				int w = 76;
				this.blit(guiLeft+174, guiTop+116, 230, 212, 16, 16);//Top Left
				this.blit(guiLeft+174, guiTop+132+h, 230, 228, 16, 10);//Bottom Left
				this.blit(guiLeft+190+w, guiTop+116, 246, 212, 10, 16);//Top Right
				this.blit(guiLeft+190+w, guiTop+132+h, 246, 228, 10, 10);//Bottom Right
				for(int hh = 0; hh < h; hh++)
				{
					this.blit(guiLeft+174, guiTop+132+hh, 230, 228, 16, 1);
					for(int ww = 0; ww < w; ww++)
						this.blit(guiLeft+190+ww, guiTop+132+hh, 246, 228, 1, 1);
					this.blit(guiLeft+190+w, guiTop+132+hh, 246, 228, 10, 1);
				}
				for(int ww = 0; ww < w; ww++)
				{
					this.blit(guiLeft+190+ww, guiTop+116, 246, 212, 1, 16);
					this.blit(guiLeft+190+ww, guiTop+132+h, 246, 228, 1, 10);

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
				this.blit(guiLeft+32, guiTop+179, 0, 216+(b0?20: 0), 16, 10);
			if(page < selectedEntry.getPageCount()-1)
				this.blit(guiLeft+136, guiTop+179, 0, 226+(b1?20: 0), 16, 10);

			manual.titleRenderPre();
			//Title
			this.drawCenteredStringScaled(manual.fontRenderer(), TextFormatting.BOLD+selectedEntry.getTitle(), guiLeft+xSize/2, guiTop+14, manual.getTitleColour(), 1, true);
			this.drawCenteredStringScaled(manual.fontRenderer(), manual.formatEntrySubtext(selectedEntry.getSubtext()), guiLeft+xSize/2,
					guiTop+22, manual.getSubTitleColour(), 1, true);
			//Page Number
			this.drawCenteredStringScaled(manual.fontRenderer(), TextFormatting.BOLD.toString()+(page+1), guiLeft+xSize/2, guiTop+183, manual.getPagenumberColour(), 1, false);
			manual.titleRenderPost();

			GL11.glColor3f(1, 1, 1);
			selectedEntry.renderPage(this, guiLeft+32, guiTop+28, mouseX-32, mouseY-28);

			mouseX += guiLeft;
			mouseY += guiTop;
		}
		else
		{
			String title = ManualUtils.getTitleForNode(currentNode, manual);
			manual.titleRenderPre();
			this.drawCenteredStringScaled(manual.fontRenderer(), TextFormatting.BOLD+title, guiLeft+xSize/2, guiTop+12, manual.getTitleColour(), 1, true);
			manual.titleRenderPost();
		}
		if(this.searchField!=null)
		{
			this.searchField.render(mouseX, mouseY, f);
			if(suggestionList.visible)
				//TODO translation
				manual.fontRenderer().drawString("It looks like you meant:", guiLeft+180, guiTop+128, manual.getTextColour());
		}
		for(Button btn : pageButtons)
			btn.render(mouseX, mouseY, f);
		super.render(mouseX, mouseY, f);
		GlStateManager.enableBlend();
		manual.entryRenderPost();
	}

	@Override
	public void onClose()
	{
		this.manual.closeManual();
		super.onClose();
		if(prevGuiScale!=-1&&manual.allowGuiRescale())
			mc.gameSettings.guiScale = prevGuiScale;
		activeManual = null;
	}

	private void drawCenteredStringScaled(FontRenderer fr, String s, int x, int y, int colour, float scale, boolean shadow)
	{
		int xx = (int)Math.floor(x/scale-(fr.getStringWidth(s)/2.));
		int yy = (int)Math.floor(y/scale-(fr.FONT_HEIGHT/2.));
		if(scale!=1)
		{
			GlStateManager.pushMatrix();
			GlStateManager.scalef(scale, scale, scale);
		}
		if(shadow)
			fr.drawStringWithShadow(s, xx, yy, colour);
		else
			fr.drawString(s, xx, yy, colour);
		if(scale!=1)
			GlStateManager.popMatrix();
	}

	@Override
	public List<String> getTooltipFromItem(ItemStack stack)
	{
		List<String> tooltip = super.getTooltipFromItem(stack);
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
	public void renderTooltip(List<String> text, int x, int y, FontRenderer font)
	{
		manual.tooltipRenderPre();
		super.renderTooltip(text, x, y, font);
		manual.tooltipRenderPost();
	}

	@Override
	public boolean mouseScrolled(double x, double y, double wheel)
	{
		super.mouseScrolled(x, y, wheel);
		if(wheel!=0&&currentNode.isLeaf())
		{
			if(wheel > 0&&page > 0)
			{
				page--;
				this.fullInit();
				return true;
			}
			else if(wheel < 0&&page < currentNode.getLeafData().getPageCount()-1)
			{
				page++;
				this.fullInit();
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button)
	{
		if(button==0&&currentNode.isLeaf())
		{
			ManualEntry selectedEntry = currentNode.getLeafData();
			double mxRelative = mx-guiLeft;
			double myRelative = my-guiTop;
			if(page > 0&&mxRelative > 32&&mxRelative < 32+17&&myRelative > 179&&myRelative < 179+10)
			{
				page--;
				this.fullInit();
				return true;
			}
			else if(page < selectedEntry.getPageCount()-1&&mxRelative > 135&&mxRelative < 135+17&&myRelative > 179&&myRelative < 179+10)
			{
				page++;
				this.fullInit();
				return true;
			}
			else
			{
				ItemStack highlighted = selectedEntry.getHighlightedStack(page);
				if(!highlighted.isEmpty())
				{
					ManualLink link = this.getManual().getManualLink(highlighted);
					if(link!=null)
						link.changePage(this, true);
					return true;
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
				IELogger.logger.info("Changing to super node {}", currentNode.getSuperNode());
				setCurrentNode(currentNode.getSuperNode());
				page = 0;
			}
			this.fullInit();
			return true;
		}
		lastClick = new double[]{mx-guiLeft, my-guiTop};
		if(super.mouseClicked(mx, my, button))
			return true;
		if(this.searchField!=null)
			this.searchField.mouseClicked(mx, my, button);
		return false;
	}

	@Override
	public boolean mouseReleased(double mx, double my, int action)
	{
		lastClick = null;
		lastDrag = null;
		return super.mouseReleased(mx, my, action);
	}

	@Override
	public boolean mouseDragged(double mx, double my, int button, double p_mouseDragged_6_, double p_mouseDragged_8_)
	{
		if(lastClick!=null&&currentNode.isLeaf())
		{
			if(lastDrag==null)
				lastDrag = new double[]{mx-guiLeft, my-guiTop};
			currentNode.getLeafData().mouseDragged(this, guiLeft+32, guiTop+28, lastClick[0], lastClick[1], mx-guiLeft,
					my-guiTop, lastDrag[0], lastDrag[1], buttons.get(button));
			lastDrag = new double[]{mx-guiLeft, my-guiTop};
			return true;
		}
		return false;
	}

	@Override
	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_)
	{
		if(this.searchField!=null&&this.searchField.charTyped(p_charTyped_1_, p_charTyped_2_))
		{
			updateSearch();
			return true;
		}
		else
			return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_)
	{
		if(this.searchField!=null&&this.searchField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_))
		{
			updateSearch();
			return true;
		}
		else
			return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	private void updateSearch()
	{
		String search = searchField.getText();
		if(search.trim().isEmpty())
		{
			suggestionList.visible = false;
			this.fullInit();
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
					String title = ManualUtils.getTitleForNode(node, manual).toLowerCase(Locale.ENGLISH);
					if(title.contains(searchFinal))
						lHeaders.add(node);
					else
						lSpellcheck.add(node);
				}
			});
			List<AbstractNode<ResourceLocation, ManualEntry>> lCorrections =
					ManualUtils.getPrimitiveSpellingCorrections(search, lSpellcheck, 4,
							(e) -> ManualUtils.getTitleForNode(e, manual));
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

			entryList.setEntries(lHeaders);
			if(!lCorrections.isEmpty())
				suggestionList.setEntries(lCorrections);
			suggestionList.visible = !lCorrections.isEmpty();
		}
	}

	@Override
	public void fillGradient(int x1, int yA, int x2, int yB, int colorA, int colorB)
	{
		super.fillGradient(x1, yA, x2, yB, colorA, colorB);
	}
}