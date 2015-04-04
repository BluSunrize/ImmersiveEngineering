package blusunrize.immersiveengineering.client.gui.manual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import blusunrize.immersiveengineering.api.IManualPage;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.Config;

import com.google.common.collect.ArrayListMultimap;

import cpw.mods.fml.relauncher.ReflectionHelper;

public abstract class ManualPages implements IManualPage
{
	String text;
	String localizedText;
	public ManualPages(String text)
	{
		this.text=text;
	}
	@Override
	public void initPage(GuiScreen gui, int x, int y, List<GuiButton> pageButtons)
	{
		if(text!=null&&!text.isEmpty())
		{
			String s = StatCollector.translateToLocal("ie.manual.entry."+text);
			boolean uni = ClientUtils.font().getUnicodeFlag();
			ClientUtils.font().setUnicodeFlag(true);
			this.localizedText=formatText(s);
			this.localizedText = addLinks(this.localizedText, x,y, 120, pageButtons);
			ClientUtils.font().setUnicodeFlag(uni);
		}
	}
	@Override
	public void buttonPressed(GuiScreen gui, GuiButton button)
	{
		if(button instanceof GuiButtonManualLink && GuiManual.activeManual!=null)
		{
			GuiManual.selectedEntry = ((GuiButtonManualLink)button).key;
			GuiManual.page = ((GuiButtonManualLink)button).pageLinked;
			GuiManual.activeManual.initGui();
		}
	}

	public static class Text extends ManualPages
	{
		public Text(String text)
		{
			super(text);
		}

		@Override
		public void renderPage(GuiScreen gui, int x, int y, int mx, int my)
		{
			if(localizedText!=null&&!localizedText.isEmpty())
				ClientUtils.font().drawSplitString(localizedText, x,y, 120, 0x444444);
		}
	}

	public static class Image extends ManualPages
	{
		String[] resources;
		int[][] sizing;
		public Image(String text, String... images)
		{
			super(text);
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
		public void renderPage(GuiScreen gui, int x, int y, int mx, int my)
		{
			String lastResource="";
			int yOff = 0;
			for(int i=0; i<resources.length; i++)
				if(resources[i]!=null&&!resources[i].isEmpty())
				{
					if(resources[i]!=lastResource)
						ClientUtils.bindTexture(resources[i]);
					int xOff = 60-sizing[i][2]/2;
					ClientUtils.drawTexturedRect(x+xOff,y+yOff,sizing[i][2],sizing[i][3], 256f, sizing[i][0],sizing[i][0]+sizing[i][2], sizing[i][1],sizing[i][1]+sizing[i][3]);
					yOff += sizing[i][3]+4;
					lastResource = resources[i];
				}

			if(localizedText!=null&&!localizedText.isEmpty())
				ClientUtils.font().drawSplitString(localizedText, x,y+yOff, 120, 0x444444);
		}
	}

	public static class Items extends ManualPages
	{
		ItemStack[] stacks;
		public Items(String text, ItemStack... stacks)
		{
			super(text);
			this.stacks=stacks;
		}

		@Override
		public void renderPage(GuiScreen gui, int x, int y, int mx, int my)
		{
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();

			float scale = 2f;
			int w = (105-stacks.length*16)/(stacks.length+1);
			GL11.glScalef(scale,scale,scale);
			for(int i=0; i<stacks.length; i++)
				RenderItem.getInstance().renderItemIntoGUI(ClientUtils.font(), ClientUtils.mc().renderEngine, stacks[i], (int)((x+w+(18+w)*i)/scale), (int)((y+4)/scale));
			GL11.glScalef(1/scale,1/scale,1/scale);

			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);

			if(localizedText!=null&&!localizedText.isEmpty())
				ClientUtils.font().drawSplitString(localizedText, x,y+44, 110, 0x444444);
		}
	}

	public static class Crafting extends ManualPages
	{
		ItemStack[] stacks;
		ArrayListMultimap<ItemStack, PositionedItemStack[]> recipes = ArrayListMultimap.create();
		int recipePage[];
		int yOff[];
		public Crafting(String text, ItemStack... stacks)
		{
			super(text);
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
						ItemStack stack = stacks[iStack];
						if(((IRecipe)o).getRecipeOutput()!=null && OreDictionary.itemMatches(stack, ((IRecipe)o).getRecipeOutput(), false))
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
								//System.out.println(((IRecipe)o).getRecipeOutput());
								int xBase = (120-(w+2)*18)/2;
								for(int hh=0; hh<h; hh++)
									for(int ww=0; ww<w; ww++)
										if(hh*w+ww<ingredients.length)
											pIngredients[hh*w+ww] = new PositionedItemStack(ingredients[hh*w+ww], xBase+ww*18,hh*18);
								pIngredients[pIngredients.length-1] = new PositionedItemStack(((IRecipe)o).getRecipeOutput(), xBase+w*18+18, (int)(h/2f*18)-8);
								this.recipes.put(stack, pIngredients);
							}
							if(h*18>yOff[iStack])
								yOff[iStack]=h*18;
						}
					}
				}
		}

		@Override
		public void initPage(GuiScreen gui, int x, int y, List<GuiButton> pageButtons)
		{
			super.initPage(gui, x, y, pageButtons);
			//Load Recipes
			int i=1;
			int yyOff=0;
			for(ItemStack stack : this.stacks)
			{
				if(this.recipes.get(stack).size()>1)
				{
					pageButtons.add(new GuiButtonArrow(100*i+0, x-2,y+yyOff+yOff[i-1]/2-3, 0));
					pageButtons.add(new GuiButtonArrow(100*i+1, x+122-16,y+yyOff+yOff[i-1]/2-3, 1));
				}
				yyOff += yOff[i-1]+8;
				i++;
			}
		}

		@Override
		public void renderPage(GuiScreen gui, int x, int y, int mx, int my)
		{
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();

			int totalYOff = 0;
			ItemStack highlighted = null;
			for(int i=0; i<stacks.length; i++)
			{
				ItemStack stack = stacks[i];
				List<PositionedItemStack[]> rList = this.recipes.get(stack);
				if(!rList.isEmpty() && recipePage[i]>=0 && recipePage[i]<this.recipes.size())
				{
					ClientUtils.bindTexture("immersiveengineering:textures/models/white.png");
					int maxX=0;
					for(PositionedItemStack pstack : rList.get(recipePage[i]))
						if(pstack!=null)
						{
							if(pstack.x>maxX)
								maxX=pstack.x;
							ClientUtils.drawColouredRect(x+pstack.x, y+totalYOff+pstack.y, 16,16, 0x33666666);
						}
					ClientUtils.bindTexture("immersiveengineering:textures/gui/manual.png");
					ClientUtils.drawTexturedRect(x+maxX-17,y+totalYOff+yOff[i]/2-5, 16,10, 256, 0,16, 226,236);

					totalYOff += yOff[i]+8;
				}
			}
			totalYOff=0;
			GL11.glTranslated(0, 0, 300);
			boolean uni = ClientUtils.font().getUnicodeFlag();
			ClientUtils.font().setUnicodeFlag(false);
			for(int i=0; i<stacks.length; i++)
			{
				ItemStack stack = stacks[i];
				List<PositionedItemStack[]> rList = this.recipes.get(stack);
				if(!rList.isEmpty() && recipePage[i]>=0 && recipePage[i]<this.recipes.size())
				{
					for(PositionedItemStack pstack : rList.get(recipePage[i]))
						if(pstack!=null)
							if(pstack.getStack()!=null)
							{
								RenderItem.getInstance().renderItemIntoGUI(ClientUtils.font(), ClientUtils.mc().renderEngine, pstack.getStack(), x+pstack.x, y+totalYOff+pstack.y);
								RenderItem.getInstance().renderItemOverlayIntoGUI(ClientUtils.font(), ClientUtils.mc().renderEngine, pstack.getStack(), x+pstack.x, y+totalYOff+pstack.y);
								if(mx>=x+pstack.x&&mx<x+pstack.x+16 && my>=y+totalYOff+pstack.y&&my<y+totalYOff+pstack.y+16)
									highlighted = pstack.getStack();
							}
					totalYOff += yOff[i]+8;
				}
			}
			ClientUtils.font().setUnicodeFlag(uni);
			GL11.glTranslated(0, 0, -300);
			if(highlighted!=null)
				ClientUtils.renderToolTip(highlighted, mx, my);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			GL11.glEnable(GL11.GL_BLEND);

			if(localizedText!=null&&!localizedText.isEmpty())
				ClientUtils.font().drawSplitString(localizedText, x,y+totalYOff+2, 110, 0x444444);
		}

		@Override
		public void buttonPressed(GuiScreen gui, GuiButton button)
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

	public static String formatText(String s)
	{
		s = s.replaceAll("<br>", "\n");
		int start;
		int overflow=0;
		while( (start=s.indexOf("<config"))>=0 && overflow<50)
		{
			overflow++;
			int end = s.indexOf(">", start);
			String rep = s.substring(start, end+1);
			String[] segment = rep.substring(0,rep.length()-1).split(":");
			if(segment.length<3)
				break;
			String result = "";
			if(segment[1].equalsIgnoreCase("b"))
			{
				if(segment.length>4)
					result = (Config.getBoolean(segment[2])?segment[3]:segment[4]);
				else
					result = ""+Config.getBoolean(segment[2]);
			}
			else if(segment[1].equalsIgnoreCase("i"))
				result = ""+Config.getInt(segment[2]);
			else if(segment[1].equalsIgnoreCase("iA"))
			{
				int[] iA = Config.getIntArray(segment[2]);
				if(segment.length>3)
					try{
						int idx = Integer.parseInt(segment[3]);
						result = ""+iA[idx];
					}catch(Exception ex){
						break;
					}
				else
					for(int i=0; i<iA.length; i++)
						result += (i>0?", ":"")+iA[i];
			}
			else if(segment[1].equalsIgnoreCase("dA"))
			{
				double[] iD = Config.getDoubleArray(segment[2]);
				if(segment.length>3)
					try{
						int idx = Integer.parseInt(segment[3]);
						result = ""+ClientUtils.formatDouble(iD[idx], "#.***");
					}catch(Exception ex){
						break;
					}
				else
					for(int i=0; i<iD.length; i++)
						result += (i>0?", ":"")+ClientUtils.formatDouble(iD[i], "#.***");
			}

			s = s.replaceFirst(rep, result);
		}
		return s;
	}
	public static String addLinks(String text, int x, int y, int width, List<GuiButton> pageButtons)
	{
		List<String[]> repList = new ArrayList<String[]>();
		int start;
		int overflow=0;
		while( (start=text.indexOf("<link"))>=0 && overflow<50)
		{
			overflow++;
			int end = text.indexOf(">", start);
			String rep = text.substring(start, end+1);
			String[] segment = rep.substring(0,rep.length()-1).split(":");
			if(segment.length<3)
				break;
			String page = segment.length>3?segment[3]:"0";
			String result = segment[2];
			String[] resultParts = result.split(" ");
			for(String part : resultParts)
				repList.add(new String[]{part,segment[1],page});
			text = text.replaceFirst(rep, result);
		}


		List<String> list = ClientUtils.font().listFormattedStringToWidth(text, width);

		Iterator<String[]> itRep = repList.iterator();
		while(itRep.hasNext())
		{
			String[] rep = itRep.next();
			for(int yOff = 0; yOff<list.size(); yOff++)
			{
				String s = list.get(yOff);
				if((start=s.indexOf(rep[0]))>=0)
				{
					int bx = ClientUtils.font().getStringWidth(s.substring(0,start));
					int by = yOff*ClientUtils.font().FONT_HEIGHT;
					String bkey = rep[1];
					int bw = ClientUtils.font().getStringWidth(rep[0]);
					int bpage = 0;
					try{
						bpage = Integer.parseInt(rep[2]);
					}catch(Exception e){}
					pageButtons.add(new GuiButtonManualLink(900+overflow, x+bx,y+by, bw,(int)(ClientUtils.font().FONT_HEIGHT*1.5), bkey, rep[0], bpage));
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