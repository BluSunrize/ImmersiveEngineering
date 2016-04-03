package blusunrize.lib.manual;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import com.google.common.collect.ArrayListMultimap;

import blusunrize.lib.manual.gui.GuiButtonManualLink;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.GuiManual;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;

public abstract class ManualPages implements IManualPage
{
	protected ManualInstance manual;
	protected String text;
	protected String localizedText;
	public ManualPages(ManualInstance manual, String text)
	{
		this.manual=manual;
		this.text=text;
	}
	@Override
	public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
	{
		if(text!=null&&!text.isEmpty())
		{
			boolean uni = manual.fontRenderer.getUnicodeFlag();
			manual.fontRenderer.setUnicodeFlag(true);
			this.localizedText = manual.formatText(text);
			this.localizedText = addLinks(manual, gui, this.localizedText, x,y, 120, pageButtons);
			if(this.localizedText==null)
				this.localizedText="";
			manual.fontRenderer.setUnicodeFlag(uni);
		}
	}
	@Override
	public void buttonPressed(GuiManual gui, GuiButton button)
	{
		if(button instanceof GuiButtonManualLink && GuiManual.activeManual!=null && manual.showEntryInList(manual.getEntry(((GuiButtonManualLink)button).key)))
		{
			if(GuiManual.previousSelectedEntry.size()>0)
				GuiManual.previousSelectedEntry.add(0,GuiManual.selectedEntry);
			else
				GuiManual.previousSelectedEntry.add(GuiManual.selectedEntry);
			GuiManual.selectedEntry = ((GuiButtonManualLink)button).key;
			GuiManual.page = ((GuiButtonManualLink)button).pageLinked;
			GuiManual.activeManual.initGui();
		}
	}
	@Override
	public ManualInstance getManualHelper()
	{
		return manual;
	}
	@Override
	public void mouseDragged(int x, int y, int clickX, int clickY, int mx, int my, int lastX, int lastY, int button)
	{
	}
	//	@Override
	//	public void buttonPressed(GuiManual gui, GuiButton button)
	//	{
	//		if(button instanceof GuiButtonManualLink && GuiManual.activeManual!=null && manual.showEntryInList(manual.getEntry(((GuiButtonManualLink)button).key)))
	//		{
	//			GuiManual.selectedEntry = ((GuiButtonManualLink)button).key;
	//			GuiManual.page = ((GuiButtonManualLink)button).pageLinked;
	//			GuiManual.activeManual.initGui();
	//		}
	//	}
	@Override
	public void recalculateCraftingRecipes()
	{
	}

	public static class Text extends ManualPages
	{
		public Text(ManualInstance manual, String text)
		{
			super(manual,text);
		}

		@Override
		public void renderPage(GuiManual gui, int x, int y, int mx, int my)
		{
			if(localizedText!=null&&!localizedText.isEmpty())
				ManualUtils.drawSplitString(manual.fontRenderer, localizedText, x,y, 120, manual.getTextColour());
			//				manual.fontRenderer.drawSplitString(localizedText, x,y, 120, manual.getTextColour());
		}

		@Override
		public boolean listForSearch(String searchTag)
		{
			return false;
		}
	}

	public static class Image extends ManualPages
	{
		String[] resources;
		int[][] sizing;
		public Image(ManualInstance helper, String text, String... images)
		{
			super(helper,text);
			resources = new String[images.length];
			sizing = new int[images.length][4];
			for(int i=0; i<images.length; i++)
			{
				String[] split = images[i].split(";");
				if(split.length<5)
					continue;
				resources[i] = split[0];
				try{
					sizing[i][0] = Integer.parseInt(split[1]);
					sizing[i][1] = Integer.parseInt(split[2]);
					sizing[i][2] = Integer.parseInt(split[3]);
					sizing[i][3] = Integer.parseInt(split[4]);
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}

		@Override
		public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
		{
			int yOff = 0;
			for(int i=0; i<resources.length; i++)
				if(resources[i]!=null&&!resources[i].isEmpty())
					yOff += sizing[i][3]+5;
			super.initPage(gui, x, y+yOff, pageButtons);
		}

		@Override
		public void renderPage(GuiManual gui, int x, int y, int mx, int my)
		{
			int yOff = 0;
			for(int i=0; i<resources.length; i++)
				if(resources[i]!=null&&!resources[i].isEmpty())
				{
					int xOff = 60-sizing[i][2]/2;
					gui.drawGradientRect(x+xOff-2,y+yOff-2,x+xOff+sizing[i][2]+2,y+yOff+sizing[i][3]+2, 0xffeaa74c,0xfff6b059);
					gui.drawGradientRect(x+xOff-1,y+yOff-1,x+xOff+sizing[i][2]+1,y+yOff+sizing[i][3]+1, 0xffc68e46,0xffbe8844);
					yOff += sizing[i][3]+5;
				}
			String lastResource="";
			yOff = 0;
			for(int i=0; i<resources.length; i++)
				if(resources[i]!=null&&!resources[i].isEmpty())
				{
					if(resources[i]!=lastResource)
						ManualUtils.bindTexture(resources[i]);
					int xOff = 60-sizing[i][2]/2;
					ManualUtils.drawTexturedRect(x+xOff,y+yOff,sizing[i][2],sizing[i][3], (sizing[i][0])/256f,(sizing[i][0]+sizing[i][2])/256f, (sizing[i][1])/256f,(sizing[i][1]+sizing[i][3])/256f);
					yOff += sizing[i][3]+5;
					lastResource = resources[i];
				}

			if(localizedText!=null&&!localizedText.isEmpty())
				ManualUtils.drawSplitString(manual.fontRenderer, localizedText, x,y+yOff, 120, manual.getTextColour());
			//			manual.fontRenderer.drawSplitString(localizedText, x,y+yOff, 120, manual.getTextColour());
		}

		@Override
		public boolean listForSearch(String searchTag)
		{
			return false;
		}
	}

	public static class Table extends ManualPages
	{
		String[][] table;
		String[][] localizedTable;
		int textHeight;
		int[] bars;
		//		int[] barsH;
		boolean horizontalBars = false; 
		public Table(ManualInstance manual, String text, String[][] table, boolean horizontalBars)
		{
			super(manual,text);
			this.table = table;
			this.horizontalBars=horizontalBars;
		}

		@Override
		public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
		{
			super.initPage(gui, x, y, pageButtons);
			manual.fontRenderer.setUnicodeFlag(true);
			int l = localizedText!=null?manual.fontRenderer.listFormattedStringToWidth(localizedText, 120).size():0;
			textHeight = l*manual.fontRenderer.FONT_HEIGHT+6;
			try{
				if(table!=null)
				{
					localizedTable = new String[table.length][];

					bars = new int[1];
					for(int i=0; i<table.length; i++)
					{
						localizedTable[i] = new String[table[i].length];
						for(int j=0; j<table[i].length; j++)
							localizedTable[i][j] = StatCollector.translateToLocal(table[i][j]);

						if(table[i].length-1 > bars.length)
						{
							int[] newBars = new int[table[i].length-1];
							System.arraycopy(bars,0, newBars,0, bars.length);
							bars = newBars;
						}
						for(int j=0; j<table[i].length-1; j++)
						{
							int fl = manual.fontRenderer.getStringWidth(localizedTable[i][j]);
							if(fl>bars[j])
								bars[j]=fl;
						}
					}
				}
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			manual.fontRenderer.setUnicodeFlag(false);
		}

		@Override
		public void renderPage(GuiManual gui, int x, int y, int mx, int my)
		{	
			if(localizedText!=null&&!localizedText.isEmpty())
				ManualUtils.drawSplitString(manual.fontRenderer, localizedText, x,y, 120, manual.getTextColour());
			//			manual.fontRenderer.drawSplitString(localizedText, x,y, 120, manual.getTextColour());

			if(localizedTable!=null)
			{
				int col = manual.getHighlightColour()|0xff000000;
				gui.drawGradientRect(x,y+textHeight-2,x+120,y+textHeight-1, col,col);
				int[] textOff = new int[bars!=null?bars.length:0];

				//				gui.drawGradientRect(x,y+textHeight+yOff-2,x+120,y+textHeight+yOff-1,  manual.getTextColour()|0xff000000, manual.getTextColour()|0xff000000);
				if(bars!=null)
				{
					int xx = x;
					for(int i=0; i<bars.length; i++)
					{
						xx += bars[i]+4;

						//						gui.drawGradientRect(xx,y+textHeight-4,xx+1,y+textHeight+yOff, col,col);
						//						gui.drawGradientRect(xx,y+textHeight-4,xx+1,y+textHeight+(manual.fontRenderer.FONT_HEIGHT+1)*localizedTable.length, col,col);
						xx+=4;
						textOff[i] = xx;
					}
				}

				int yOff = 0;
				for(int i=0; i<localizedTable.length; i++)
				{
					for(int j=0; j<localizedTable[i].length; j++)
						if(localizedTable[i][j]!=null)
						{
							int xx = textOff.length>0&&j>0?textOff[j-1]:x;
							int w = Math.max(10, 120-(j>0?textOff[j-1]-x:0));
							ManualUtils.drawSplitString(manual.fontRenderer, localizedTable[i][j], xx,y+textHeight+yOff, w, manual.getTextColour());
							//							manual.fontRenderer.drawSplitString(localizedTable[i][j], xx,y+textHeight+yOff, w, manual.getTextColour());
							if(j!=0)
							{
								int l = manual.fontRenderer.listFormattedStringToWidth(localizedTable[i][j], w).size();

								if(horizontalBars)
								{
									float scale = .5f;
									GL11.glScalef(1, scale, 1);
									gui.drawGradientRect(x,(int)((y+textHeight+yOff+l*manual.fontRenderer.FONT_HEIGHT)/scale),x+120,(int)((y+textHeight+yOff+l*manual.fontRenderer.FONT_HEIGHT)/scale+1),  manual.getTextColour()|0xff000000, manual.getTextColour()|0xff000000);
									GL11.glScalef(1, 1/scale, 1);
								}

								yOff += l*(manual.fontRenderer.FONT_HEIGHT+1);
							}
						}
				}

				if(bars!=null)
					for(int i=0; i<bars.length; i++)
						gui.drawGradientRect(textOff[i]-4,y+textHeight-4,textOff[i]-3,y+textHeight+yOff, col,col);
			}
		}

		@Override
		public boolean listForSearch(String searchTag)
		{
			return false;
		}
	}

	public static class ItemDisplay extends ManualPages
	{
		ItemStack[] stacks;
		public ItemDisplay(ManualInstance manual, String text, ItemStack... stacks)
		{
			super(manual,text);
			this.stacks=stacks;
		}

		@Override
		public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
		{
			int length = stacks.length;
			int yOffset = 0;
			if(length>0)
			{
				float scale = length>7?1f: length>4?1.5f: 2f;
				int line0 = (int)(8/scale);
				int line1 = line0-1;
				int lineSum = line0+line1;
				int lines = (length/lineSum*2)+(length%lineSum/line0)+(length%lineSum%line0>0?1:0);
				float equalPerLine = length/(float)lines;
				line1 = (int)Math.floor(equalPerLine);
				line0 = (int)Math.ceil(equalPerLine);
				lineSum = line0+line1;
				yOffset = lines*(int)(18*scale);
			}
			super.initPage(gui, x, y+yOffset, pageButtons);
		}

		@Override
		public void renderPage(GuiManual gui, int x, int y, int mx, int my)
		{
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();
			ItemStack highlighted = null;
			int yOffset = 0;
			int length = stacks.length;
			if(length>0)
			{
				float scale = length>8?1f: length>3?1.5f: 2f;
				int line0 = (int)(7.5/scale);
				int line1 = line0-1;
				int lineSum = line0+line1;
				int lines = (length/lineSum*2)+(length%lineSum/line0)+(length%lineSum%line0>0?1:0);
				float equalPerLine = length/(float)lines;
				line1 = (int)Math.floor(equalPerLine);
				line0 = (int)Math.ceil(equalPerLine);
				lineSum = line0+line1;
				int lastLines = length%lineSum;
				int lastLine = lastLines==line0?line0: lastLines==0?line1: lastLines%line0;
				GL11.glScalef(scale,scale,scale);
				RenderItem.getInstance().renderWithColor=true;
				yOffset = lines*(int)(18*scale);
				for(int line=0; line<lines; line++)
				{
					int perLine = line==lines-1?lastLine: line%2==0?line0:line1;
					if(line==0 && perLine>length)
						perLine = length;
					int w2 = perLine*(int)(18*scale)/2;
					for(int i=0; i<perLine; i++)
					{
						int item = line/2*lineSum+line%2*line0+i;
						if(item>=length)
							break;
						int xx = x+60-w2 + (int)(i*18*scale);
						int yy = y+(lines<2?4:0)+line*(int)(18*scale);
						RenderItem.getInstance().renderItemAndEffectIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, stacks[item], (int)(xx/scale),(int)(yy/scale));
						if(mx>=xx&&mx<xx+(16*scale) && my>=yy&&my<yy+(16*scale))
							highlighted = stacks[item];
					}
				}
				GL11.glScalef(1/scale,1/scale,1/scale);
			}
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);
			if(localizedText!=null&&!localizedText.isEmpty())
				ManualUtils.drawSplitString(manual.fontRenderer, localizedText, x,y+yOffset, 120, manual.getTextColour());
			//			manual.fontRenderer.drawSplitString(localizedText, x,y+yOffset, 120, manual.getTextColour());

			manual.fontRenderer.setUnicodeFlag(false);
			if(highlighted!=null)
				gui.renderToolTip(highlighted, mx, my);
			RenderHelper.disableStandardItemLighting();
		}

		@Override
		public boolean listForSearch(String searchTag)
		{
			for(ItemStack stack : stacks)
				if(stack.getDisplayName().toLowerCase().contains(searchTag))
					return true;
			return false;
		}
	}

	public static class Crafting extends ManualPages
	{
		Object[] stacks;
		ArrayListMultimap<Object, PositionedItemStack[]> recipes = ArrayListMultimap.create();
		int recipePage[];
		int yOff[];
		public Crafting(ManualInstance manual, String text, Object... stacks)
		{
			super(manual,text);
			this.stacks=stacks;
			this.recipePage=new int[stacks.length];
			this.yOff=new int[stacks.length];
			recalculateCraftingRecipes();
		}

		@Override
		public void recalculateCraftingRecipes()
		{
			this.recipes.clear();
			List cmRecipes = CraftingManager.getInstance().getRecipeList();
			for(Object o : cmRecipes)
				if(o instanceof IRecipe)
				{
					for(int iStack=0; iStack<stacks.length; iStack++)
					{
						Object stack = stacks[iStack];
						if(stack instanceof ItemStack[])
							for(ItemStack subStack: (ItemStack[])stack)
								checkRecipe((IRecipe)o, stack, subStack, iStack);
						else
							checkRecipe((IRecipe)o, stack, stack, iStack);
					}
				}
		}

		void checkRecipe(IRecipe rec, Object key, Object stack, int iStack)
		{
			if(rec.getRecipeOutput()!=null && ManualUtils.stackMatchesObject(rec.getRecipeOutput(), stack))
			{
				Object[] val = ManualUtils.getRecipeForDisplay(rec);
				if (val!=null&&val.length==3)
				{
					this.recipes.put(key, (PositionedItemStack[])val[2]);
					int h = (Integer) val[1];
					if(h*18>yOff[iStack])
						yOff[iStack]=h*18;
				}
			}
		}

		@Override
		public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
		{
			int i=1;
			int yyOff=0;
			for(Object stack : this.stacks)
			{
				if(this.recipes.get(stack).size()>1)
				{
					pageButtons.add(new GuiButtonManualNavigation(gui, 100*i+0, x-2,y+yyOff+yOff[i-1]/2-3, 8,10, 0));
					pageButtons.add(new GuiButtonManualNavigation(gui, 100*i+1, x+122-16,y+yyOff+yOff[i-1]/2-3, 8,10, 1));
				}
				if(this.recipes.get(stack).size()>0)
					yyOff += yOff[i-1]+8;
				i++;
			}
			super.initPage(gui, x, y+yyOff-2, pageButtons);
		}

		@Override
		public void renderPage(GuiManual gui, int x, int y, int mx, int my)
		{
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();

			int totalYOff = 0;
			ItemStack highlighted = null;
			for(int i=0; i<stacks.length; i++)
			{
				Object stack = stacks[i];
				List<PositionedItemStack[]> rList = this.recipes.get(stack);
				if(!rList.isEmpty() && recipePage[i]>=0 && recipePage[i]<this.recipes.size())
				{
					int maxX=0;
					for(PositionedItemStack pstack : rList.get(recipePage[i]))
						if(pstack!=null)
						{
							if(pstack.x>maxX)
								maxX=pstack.x;
							gui.drawGradientRect(x+pstack.x, y+totalYOff+pstack.y, x+pstack.x+16,y+totalYOff+pstack.y+16, 0x33666666,0x33666666);
						}
					ManualUtils.bindTexture(manual.texture);
					ManualUtils.drawTexturedRect(x+maxX-17,y+totalYOff+yOff[i]/2-5, 16,10, 0/256f,16/256f, 226/256f,236/256f);

					totalYOff += yOff[i]+8;
				}
			}
			totalYOff=0;
			GL11.glTranslated(0, 0, 300);
			boolean uni = manual.fontRenderer.getUnicodeFlag();
			manual.fontRenderer.setUnicodeFlag(false);
			RenderItem.getInstance().renderWithColor=true;
			for(int i=0; i<stacks.length; i++)
			{
				Object stack = stacks[i];
				List<PositionedItemStack[]> rList = this.recipes.get(stack);
				if(!rList.isEmpty() && recipePage[i]>=0 && recipePage[i]<this.recipes.size())
				{
					for(PositionedItemStack pstack : rList.get(recipePage[i]))
						if(pstack!=null)
							if(pstack.getStack()!=null)
							{
								RenderItem.getInstance().renderItemAndEffectIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, pstack.getStack(), x+pstack.x, y+totalYOff+pstack.y);
								//								RenderItem.getInstance().renderItemIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, pstack.getStack(), x+pstack.x, y+totalYOff+pstack.y);
								RenderItem.getInstance().renderItemOverlayIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, pstack.getStack(), x+pstack.x, y+totalYOff+pstack.y);
								if(mx>=x+pstack.x&&mx<x+pstack.x+16 && my>=y+totalYOff+pstack.y&&my<y+totalYOff+pstack.y+16)
									highlighted = pstack.getStack();
							}
					totalYOff += yOff[i]+8;
				}
			}

			GL11.glTranslated(0, 0, -300);
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);
			RenderHelper.disableStandardItemLighting();

			manual.fontRenderer.setUnicodeFlag(uni);
			if(localizedText!=null&&!localizedText.isEmpty())
				ManualUtils.drawSplitString(manual.fontRenderer, localizedText, x,y+totalYOff-2, 120, manual.getTextColour());
			//			manual.fontRenderer.drawSplitString(localizedText, x,y+totalYOff-2, 120, manual.getTextColour());

			manual.fontRenderer.setUnicodeFlag(false);
			if(highlighted!=null)
				gui.renderToolTip(highlighted, mx, my);
			RenderHelper.disableStandardItemLighting();
		}

		@Override
		public void buttonPressed(GuiManual gui, GuiButton button)
		{
			super.buttonPressed(gui, button);
			int r = button.id/100-1;
			if(r>=0 && r<stacks.length)
			{
				if(button.id%100==0)
					recipePage[r]--;
				else
					recipePage[r]++;

				if(recipePage[r]>=this.recipes.get(stacks[r]).size())
					recipePage[r]=0;
				if(recipePage[r]<0)
					recipePage[r]=this.recipes.get(stacks[r]).size()-1;
			}
		}

		@Override
		public boolean listForSearch(String searchTag)
		{
			for(Object stack: stacks)
			{
				if(stack instanceof ItemStack[])
				{
					for(ItemStack subStack: (ItemStack[])stack)
						if(subStack.getDisplayName().toLowerCase().contains(searchTag))
							return true;
				}
				else if(stack instanceof ItemStack)
				{
					if(((ItemStack)stack).getDisplayName().toLowerCase().contains(searchTag))
						return true;
				}
				else if(stack instanceof String)
				{
					if(ManualUtils.isExistingOreName((String)stack))
						for(ItemStack subStack: OreDictionary.getOres((String)stack))
							if(subStack.getDisplayName().toLowerCase().contains(searchTag))
								return true;
				}
			}
			return false;
		}
	}

	public static class CraftingMulti extends ManualPages
	{
		Object[] stacks;
		ArrayList<PositionedItemStack[]> recipes = new ArrayList();
		int recipePage;
		int yOff;
		public CraftingMulti(ManualInstance manual, String text, Object... stacks)
		{
			super(manual,text);
			this.stacks = stacks;
			recalculateCraftingRecipes();
		}

		@Override
		public void recalculateCraftingRecipes()
		{
			this.recipes.clear();
			if(stacks!=null&&stacks.length>0&&stacks[0] instanceof PositionedItemStack[])
			{
				for(PositionedItemStack[] pisA : (PositionedItemStack[][])stacks)
				{
					for(PositionedItemStack pis : pisA)
						if(pis!=null && pis.y+18>yOff)
							yOff=pis.y+18;
					this.recipes.add(pisA);
				}
			}
			else
			{
				List cmRecipes = CraftingManager.getInstance().getRecipeList();
				for(Object o : cmRecipes)
					if(o instanceof IRecipe)
					{
						for(int iStack=0; iStack<stacks.length; iStack++)
						{
							Object stack = stacks[iStack];
							if(((IRecipe)o).getRecipeOutput()!=null && ManualUtils.stackMatchesObject(((IRecipe)o).getRecipeOutput(), stack))
							{
								Object[] val = ManualUtils.getRecipeForDisplay((IRecipe)o);
								if (val!=null&&val.length==3) {
									if (iStack < this.recipes.size())
										this.recipes.add(iStack, (PositionedItemStack[]) val[2]);
									else
										this.recipes.add((PositionedItemStack[]) val[2]);
									int h = (Integer) val[1];
									if (h * 18 > yOff)
										yOff = h * 18;
								}
							}
						}
					}
			}
		}

		@Override
		public void initPage(GuiManual gui, int x, int y, List<GuiButton> pageButtons)
		{
			if(this.recipes.size()>1)
			{
				pageButtons.add(new GuiButtonManualNavigation(gui, 100+0, x-2,y+yOff/2-3, 8,10, 0));
				pageButtons.add(new GuiButtonManualNavigation(gui, 100+1, x+122-16,y+yOff/2-3, 8,10, 1));
			}
			super.initPage(gui, x, y+yOff+2, pageButtons);
		}

		@Override
		public void renderPage(GuiManual gui, int x, int y, int mx, int my)
		{
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();

			ItemStack highlighted = null;

			if(!recipes.isEmpty() && recipePage>=0 && recipePage<this.recipes.size())
			{
				int maxX=0;
				for(PositionedItemStack pstack : recipes.get(recipePage))
					if(pstack!=null)
					{
						if(pstack.x>maxX)
							maxX=pstack.x;
						gui.drawGradientRect(x+pstack.x, y+pstack.y, x+pstack.x+16,y+pstack.y+16, 0x33666666,0x33666666);
					}
				ManualUtils.bindTexture(manual.texture);
				ManualUtils.drawTexturedRect(x+maxX-17,y+yOff/2-5, 16,10, 0/256f,16/256f, 226/256f,236/256f);

			}

			GL11.glTranslated(0, 0, 300);
			boolean uni = manual.fontRenderer.getUnicodeFlag();
			manual.fontRenderer.setUnicodeFlag(false);
			RenderItem.getInstance().renderWithColor=true;
			if(!recipes.isEmpty() && recipePage>=0 && recipePage<this.recipes.size())
			{
				for(PositionedItemStack pstack : recipes.get(recipePage))
					if(pstack!=null)
						if(pstack.getStack()!=null)
						{
							RenderItem.getInstance().renderItemAndEffectIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, pstack.getStack(), x+pstack.x, y+pstack.y);
							RenderItem.getInstance().renderItemOverlayIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, pstack.getStack(), x+pstack.x, y+pstack.y);
							if(mx>=x+pstack.x&&mx<x+pstack.x+16 && my>=y+pstack.y&&my<y+pstack.y+16)
								highlighted = pstack.getStack();
						}
			}

			GL11.glTranslated(0, 0, -300);
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);
			RenderHelper.disableStandardItemLighting();

			manual.fontRenderer.setUnicodeFlag(uni);
			if(localizedText!=null&&!localizedText.isEmpty())
				ManualUtils.drawSplitString(manual.fontRenderer, localizedText, x,y+yOff+2, 120, manual.getTextColour());
			//			manual.fontRenderer.drawSplitString(localizedText, x,y+yOff+2, 120, manual.getTextColour());

			manual.fontRenderer.setUnicodeFlag(false);
			if(highlighted!=null)
				gui.renderToolTip(highlighted, mx, my);
			RenderHelper.disableStandardItemLighting();
		}

		@Override
		public void buttonPressed(GuiManual gui, GuiButton button)
		{
			super.buttonPressed(gui, button);
			if(button.id%100==0)
				recipePage--;
			else
				recipePage++;

			if(recipePage>=this.recipes.size())
				recipePage=0;
			if(recipePage<0)
				recipePage=this.recipes.size()-1;
		}

		@Override
		public boolean listForSearch(String searchTag)
		{
			for(PositionedItemStack[] recipe: this.recipes)
				for(PositionedItemStack stack: recipe)
				{
					if(stack.stack instanceof ItemStack[])
					{
						for(ItemStack subStack: (ItemStack[])stack.stack)
							if(subStack.getDisplayName().toLowerCase().contains(searchTag))
								return true;
					}
					else if(stack.stack instanceof ArrayList)
						for(ItemStack subStack: (ArrayList<ItemStack>)stack.stack)
						{
							if(subStack.getDisplayName().toLowerCase().contains(searchTag))
								return true;
						}
					else if(stack.stack instanceof ItemStack)
					{
						if(((ItemStack)stack.stack).getDisplayName().toLowerCase().contains(searchTag))
							return true;
					}
					else if(stack.stack instanceof String)
					{
						if(ManualUtils.isExistingOreName((String)stack.stack))
							for(ItemStack subStack: OreDictionary.getOres((String)stack.stack))
								if(subStack.getDisplayName().toLowerCase().contains(searchTag))
									return true;
					}
				}
			return false;
		}
	}

	public static String addLinks(ManualInstance helper, GuiManual gui, String text, int x, int y, int width, List<GuiButton> pageButtons)
	{
		List<String[]> repList = new ArrayList<String[]>();
		int start;
		int overflow=0;
		while( (start=text.indexOf("<link"))>=0 && overflow<50)
		{
			overflow++;
			int end = text.indexOf(">", start);
			String rep = text.substring(start, end+1);
			String[] segment = rep.substring(0,rep.length()-1).split(";");
			if(segment.length<3)
				break;
			String page = segment.length>3?segment[3]:"0";
			String result = segment[2];
			String[] resultParts = result.split(" ");
			for(String part : resultParts)
				repList.add(new String[]{part,segment[1],page});
			text = text.replaceFirst(rep, result);
		}


		List<String> list = helper.fontRenderer.listFormattedStringToWidth(text, width);

		int linkIdx = 0;
		for(int yOff = 0; yOff<list.size(); yOff++)
		{
			int lastIdx = 0;
			for(; linkIdx<repList.size(); linkIdx++)
			{
				String[] rep = repList.get(linkIdx);
				String s = list.get(yOff);
				if((start=s.indexOf(rep[0]))>=lastIdx)
				{
					int bx = helper.fontRenderer.getStringWidth(s.substring(0,start));
					int by = yOff*helper.fontRenderer.FONT_HEIGHT;
					String bkey = rep[1];
					int bw = helper.fontRenderer.getStringWidth(rep[0]);
					int bpage = 0;
					try{
						bpage = Integer.parseInt(rep[2]);
					}catch(Exception e){}
					pageButtons.add(new GuiButtonManualLink(gui, 900+overflow, x+bx,y+by, bw,(int)(helper.fontRenderer.FONT_HEIGHT*1.5), bkey, rep[0], bpage));
					lastIdx = start;
				}
				else
					break;
			}
		}
		return text;
	}

	public static class PositionedItemStack
	{
		public Object stack;
		public int x;
		public int y;
		public PositionedItemStack(Object stack, int x, int y)
		{
			this.stack=stack;
			this.x=x;
			this.y=y;
		}

		public ArrayList<ItemStack> displayList;
		public ItemStack getStack()
		{
			if(displayList==null)
			{
				displayList = new ArrayList<ItemStack>();
				if(stack instanceof ItemStack)
				{
					if(((ItemStack)stack).getItem().getHasSubtypes() && ((ItemStack)stack).getItemDamage()==OreDictionary.WILDCARD_VALUE)
					{
						ArrayList<ItemStack> list = new ArrayList<ItemStack>();
						((ItemStack)stack).getItem().getSubItems(((ItemStack)stack).getItem(), ((ItemStack)stack).getItem().getCreativeTab(), list);
						if(list.size()>0)
							displayList.addAll(list);
					}
					else
						displayList.add((ItemStack)stack);
				}
				else if(stack instanceof List && !((List)stack).isEmpty())
				{
					for(ItemStack subStack : (List<ItemStack>)this.stack)
					{
						if(subStack.getItem().getHasSubtypes() && subStack.getItemDamage()==OreDictionary.WILDCARD_VALUE)
						{
							ArrayList<ItemStack> list = new ArrayList<ItemStack>();
							subStack.getItem().getSubItems(subStack.getItem(), subStack.getItem().getCreativeTab(), list);
							if(list.size()>0)
								displayList.addAll(list);
						}
						else
							displayList.add(subStack);
					}
				}
			}
			if(displayList==null || displayList.isEmpty())
				return null;

			int perm = (int) (System.nanoTime()/1000000000 % displayList.size());
			return displayList.get(perm);
		}
	}
}