package blusunrize.lib.manual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.lib.manual.gui.GuiButtonManualLink;
import blusunrize.lib.manual.gui.GuiButtonManualNavigation;
import blusunrize.lib.manual.gui.GuiManual;

import com.google.common.collect.ArrayListMultimap;

import cpw.mods.fml.relauncher.ReflectionHelper;

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
				manual.fontRenderer.drawSplitString(localizedText, x,y, 120, manual.getTextColour());
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
				manual.fontRenderer.drawSplitString(localizedText, x,y+yOff, 120, manual.getTextColour());
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
				manual.fontRenderer.drawSplitString(localizedText, x,y, 120, manual.getTextColour());

			if(localizedTable!=null)
			{
				int col = manual.getHighlightColour()|0xff000000;
				gui.drawGradientRect(x,y+textHeight-2,x+120,y+textHeight-1, col,col);
				int[] textOff = new int[bars!=null?bars.length:0];
				if(bars!=null)
				{
					int xx = x;
					for(int i=0; i<bars.length; i++)
					{
						xx += bars[i]+4;
						gui.drawGradientRect(xx,y+textHeight-4,xx+1,y+textHeight+(manual.fontRenderer.FONT_HEIGHT+1)*localizedTable.length, col,col);
						xx+=4;
						textOff[i] = xx;
					}
				}

				//				gui.drawGradientRect(x,y+textHeight+yOff-2,x+120,y+textHeight+yOff-1,  manual.getTextColour()|0xff000000, manual.getTextColour()|0xff000000);

				int yOff = 0;
				for(int i=0; i<localizedTable.length; i++)
				{
					for(int j=0; j<localizedTable[i].length; j++)
						if(localizedTable[i][j]!=null)
						{
							int xx = textOff.length>0&&j>0?textOff[j-1]:x;
							int w = Math.max(10, 120-(j>0?textOff[j-1]-x:0));
							manual.fontRenderer.drawSplitString(localizedTable[i][j], xx,y+textHeight+yOff, w, manual.getTextColour());
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
			}
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
			super.initPage(gui, x, y+44, pageButtons);
		}

		@Override
		public void renderPage(GuiManual gui, int x, int y, int mx, int my)
		{
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();

			float scale = 2f;
			int w = (105-stacks.length*16)/(stacks.length+1);
			GL11.glScalef(scale,scale,scale);
			RenderItem.getInstance().renderWithColor=true;
			for(int i=0; i<stacks.length; i++)
				RenderItem.getInstance().renderItemAndEffectIntoGUI(manual.fontRenderer, ManualUtils.mc().renderEngine, stacks[i], (int)((x+w+(18+w)*i)/scale), (int)((y+4)/scale));
			GL11.glScalef(1/scale,1/scale,1/scale);

			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);

			if(localizedText!=null&&!localizedText.isEmpty())
				manual.fontRenderer.drawSplitString(localizedText, x,y+44, 120, manual.getTextColour());
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

			this.recipes.clear();
			List cmRecipes = CraftingManager.getInstance().getRecipeList();
			for(Object o : cmRecipes)
				if(o!=null && o instanceof IRecipe)
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
				Object[] ingredientsPre=null;
				int w=0;
				int h=0;
				if(rec instanceof ShapelessRecipes)
				{
					ingredientsPre = ((ShapelessRecipes)rec).recipeItems.toArray();
					w = ingredientsPre.length>6?3: ingredientsPre.length>1?2: 1;
					h = ingredientsPre.length>4?3: ingredientsPre.length>2?2: 1;
				}
				else if(rec instanceof ShapelessOreRecipe)
				{
					ingredientsPre = ((ShapelessOreRecipe)rec).getInput().toArray();
					w = ingredientsPre.length>6?3: ingredientsPre.length>1?2: 1;
					h = ingredientsPre.length>4?3: ingredientsPre.length>2?2: 1;
				}
				else if(rec instanceof ShapedOreRecipe)
				{
					ingredientsPre = ((ShapedOreRecipe)rec).getInput();
					w = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe)rec, "width");
					h = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe)rec, "height");
				}
				else if(rec instanceof ShapedRecipes)
				{
					ingredientsPre = ((ShapedRecipes)rec).recipeItems;
					w = ((ShapedRecipes)rec).recipeWidth;
					h = ((ShapedRecipes)rec).recipeHeight;
				}
				Object[] ingredients = new Object[ingredientsPre.length];
				for(int iO=0; iO<ingredientsPre.length; iO++)
				{
					if(ingredientsPre[iO] instanceof List)
					{
						ingredients[iO] = new ArrayList((List)ingredientsPre[iO]);
						Iterator<ItemStack> itValidate = ((ArrayList<ItemStack>)ingredients[iO]).iterator();
						while(itValidate.hasNext())
						{
							ItemStack stVal = itValidate.next();
							if(stVal==null || stVal.getItem()==null || stVal.getDisplayName()==null)
								itValidate.remove();
						}
					}
					else
						ingredients[iO] = ingredientsPre[iO];
				}
				if(ingredients!=null)
				{
					PositionedItemStack[] pIngredients = new PositionedItemStack[ingredients.length+1];
					int xBase = (120-(w+2)*18)/2;
					for(int hh=0; hh<h; hh++)
						for(int ww=0; ww<w; ww++)
							if(hh*w+ww<ingredients.length)
								pIngredients[hh*w+ww] = new PositionedItemStack(ingredients[hh*w+ww], xBase+ww*18,hh*18);
					pIngredients[pIngredients.length-1] = new PositionedItemStack(rec.getRecipeOutput(), xBase+w*18+18, (int)(h/2f*18)-8);
					this.recipes.put(key, pIngredients);
				}
				if(h*18>yOff[iStack])
					yOff[iStack]=h*18;
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
				manual.fontRenderer.drawSplitString(localizedText, x,y+totalYOff-2, 120, manual.getTextColour());

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
	}

	public static class CraftingMulti extends ManualPages
	{
		ArrayList<PositionedItemStack[]> recipes = new ArrayList();
		int recipePage;
		int yOff;
		public CraftingMulti(ManualInstance manual, String text, Object... stacks)
		{
			super(manual,text);
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
					if(o!=null && o instanceof IRecipe)
					{
						for(int iStack=0; iStack<stacks.length; iStack++)
						{
							Object stack = stacks[iStack];
							if(((IRecipe)o).getRecipeOutput()!=null && ManualUtils.stackMatchesObject(((IRecipe)o).getRecipeOutput(), stack))
							{
								IRecipe r = (IRecipe)o;
								Object[] ingredientsPre=null;
								int w=0;
								int h=0;
								if(r instanceof ShapelessRecipes)
								{
									ingredientsPre = ((ShapelessRecipes)r).recipeItems.toArray();
									w = ingredientsPre.length>6?3: ingredientsPre.length>1?2: 1;
									h = ingredientsPre.length>4?3: ingredientsPre.length>2?2: 1;
								}
								else if(r instanceof ShapelessOreRecipe)
								{
									ingredientsPre = ((ShapelessOreRecipe)r).getInput().toArray();
									w = ingredientsPre.length>6?3: ingredientsPre.length>1?2: 1;
									h = ingredientsPre.length>4?3: ingredientsPre.length>2?2: 1;
								}
								else if(r instanceof ShapedOreRecipe)
								{
									ingredientsPre = ((ShapedOreRecipe)r).getInput();
									w = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe)r, "width");
									h = ReflectionHelper.getPrivateValue(ShapedOreRecipe.class, (ShapedOreRecipe)r, "height");
								}
								else if(r instanceof ShapedRecipes)
								{
									ingredientsPre = ((ShapedRecipes)r).recipeItems;
									w = ((ShapedRecipes)r).recipeWidth;
									h = ((ShapedRecipes)r).recipeHeight;
								}
								Object[] ingredients = new Object[ingredientsPre.length];
								for(int iO=0; iO<ingredientsPre.length; iO++)
								{
									if(ingredientsPre[iO] instanceof List)
									{
										ingredients[iO] = new ArrayList((List)ingredientsPre[iO]);
										Iterator<ItemStack> itValidate = ((ArrayList<ItemStack>)ingredients[iO]).iterator();
										while(itValidate.hasNext())
										{
											ItemStack stVal = itValidate.next();
											if(stVal==null || stVal.getItem()==null || stVal.getDisplayName()==null)
												itValidate.remove();
										}
									}
									else
										ingredients[iO] = ingredientsPre[iO];
								}
								if(ingredients!=null)
								{
									PositionedItemStack[] pIngredients = new PositionedItemStack[ingredients.length+1];
									int xBase = (120-(w+2)*18)/2;
									for(int hh=0; hh<h; hh++)
										for(int ww=0; ww<w; ww++)
											if(hh*w+ww<ingredients.length)
												pIngredients[hh*w+ww] = new PositionedItemStack(ingredients[hh*w+ww], xBase+ww*18,hh*18);
									pIngredients[pIngredients.length-1] = new PositionedItemStack(((IRecipe)o).getRecipeOutput(), xBase+w*18+18, (int)(h/2f*18)-8);
									if(iStack<this.recipes.size())
										this.recipes.add(iStack,pIngredients);
									else
										this.recipes.add(pIngredients);
								}
								if(h*18>yOff)
									yOff=h*18;
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
				ManualUtils.drawTexturedRect(x+maxX-17,y+yOff/2-5, 16,10, 256, 0,16, 226,236);
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

			manual.fontRenderer.setUnicodeFlag(uni);
			if(localizedText!=null&&!localizedText.isEmpty())
				manual.fontRenderer.drawSplitString(localizedText, x,y+yOff+2, 120, manual.getTextColour());

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

		Iterator<String[]> itRep = repList.iterator();
		while(itRep.hasNext())
		{
			String[] rep = itRep.next();
			for(int yOff = 0; yOff<list.size(); yOff++)
			{
				String s = list.get(yOff);
				if((start=s.indexOf(rep[0]))>=0)
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
					break;
				}
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

		public ItemStack getStack()
		{
			if(stack instanceof ItemStack)
				return (ItemStack)stack;
			else if(stack instanceof List && !((List)stack).isEmpty())
			{
				int perm = (int) (System.nanoTime()/1000000000 % ((List)stack).size());
				return (ItemStack) ((List)stack).get(perm);
			}
			return null;
		}
	}
}