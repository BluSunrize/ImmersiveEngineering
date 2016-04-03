package blusunrize.lib.manual;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.lib.manual.ManualPages.PositionedItemStack;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

public class ManualUtils
{
	public static boolean stackMatchesObject(ItemStack stack, Object o)
	{
		if(o instanceof String)
			return compareToOreName(stack, (String)o);
		if(o instanceof ItemStack)
			return OreDictionary.itemMatches((ItemStack)o, stack, false);
		return false;
	}
	public static boolean compareToOreName(ItemStack stack, String oreName)
	{
		for(int oid : OreDictionary.getOreIDs(stack))
			if(OreDictionary.getOreName(oid).equals(oreName))
				return true;
		return false;
	}
	public static boolean isExistingOreName(String name)
	{
		if(!OreDictionary.doesOreNameExist(name))
			return false;
		else
			return !OreDictionary.getOres(name).isEmpty();
	}

	public static void drawTexturedRect(int x, int y, int w, int h, double... uv)
	{
		Tessellator tes = Tessellator.instance;
		tes.startDrawingQuads();
		tes.addVertexWithUV(x, y+h, 0, uv[0], uv[3]);
		tes.addVertexWithUV(x+w, y+h, 0, uv[1], uv[3]);
		tes.addVertexWithUV(x+w, y, 0, uv[1], uv[2]);
		tes.addVertexWithUV(x, y, 0, uv[0], uv[2]);
		tes.draw();
	}

	public static ArrayList<String> getPrimitiveSpellingCorrections(String query, String[] valid, int maxDistance)
	{
		ArrayList<String> list = new ArrayList<String>();
		for(String s : valid)
			if(s!=null && !s.trim().isEmpty())
				if(getSpellingDistanceBetweenStrings(query,s)<maxDistance)
					list.add(s);

		Collections.sort(list, new Comparator<String>(){
			@Override
			public int compare(String s0, String s1)
			{
				return getSpellingDistanceBetweenStrings(s1,s0);
			}
		});

		return list;
	}
	public static int getSpellingDistanceBetweenStrings(String query, String target)
	{
		query = query.toLowerCase();
		target = target.toLowerCase();

		String[] queryWords = query.split(" ");
		String[] targetWords = target.split(" ");
		int distance = 0;
		for(int iWord=0; iWord<queryWords.length; iWord++)
		{
			if(iWord>=targetWords.length)
				distance++;
			else
			{
				int wordDistance = 0;
				for(int iChar=0; iChar<queryWords[iWord].length(); iChar++)
					if(iChar>=targetWords[iWord].length())
						distance++;
					else
					{
						if(queryWords[iWord].charAt(iChar) != targetWords[iWord].charAt(iChar))
						{
							wordDistance++;
							if(iChar>0 && queryWords[iWord].charAt(iChar-1)==targetWords[iWord].charAt(iChar) && queryWords[iWord].charAt(iChar)==targetWords[iWord].charAt(iChar-1))
								wordDistance-=2;//switched letters don't increase distance
						}
					}
				if(wordDistance>0)
					wordDistance += targetWords[iWord].length()-queryWords[iWord].length();
				distance += wordDistance;
			}
		}
		return distance;
	}

	/**
	 * Custom implementation of drawing a split string because Mojang's doesn't reset text colour between lines >___>
	 */
	public static void drawSplitString(FontRenderer fontRenderer, String string, int x, int y, int width, int colour)
	{
		fontRenderer.resetStyles();
		fontRenderer.textColor = colour;
		List list = fontRenderer.listFormattedStringToWidth(string, width);
		FloatBuffer currentGLColor = BufferUtils.createFloatBuffer(16);
		for(Iterator iterator = list.iterator(); iterator.hasNext(); y += fontRenderer.FONT_HEIGHT)
		{
			String next = (String)iterator.next();
			int currentColour = fontRenderer.textColor;
			GL11.glGetFloat(GL11.GL_CURRENT_COLOR, currentGLColor);
			//Resetting colour if GL colour differs from textColor
			//that case happens because the formatting reset does not reset textColor
			if(!(currentGLColor.get(0)==(currentColour>>16&255)/255f && currentGLColor.get(1)==(currentColour>>8&255)/255f && currentGLColor.get(2)==(currentColour&255)/255f))
				fontRenderer.textColor = colour;
			fontRenderer.drawString(next, x, y, fontRenderer.textColor, false);
		}
	}

	static HashMap<String, ResourceLocation> resourceMap = new HashMap<String, ResourceLocation>();
	public static Tessellator tes()
	{
		return Tessellator.instance;
	}
	public static Minecraft mc()
	{
		return Minecraft.getMinecraft();
	}
	public static void bindTexture(String path)
	{
		mc().getTextureManager().bindTexture(getResource(path));
	}
	public static ResourceLocation getResource(String path)
	{
		ResourceLocation rl = resourceMap.containsKey(path) ? resourceMap.get(path) : new ResourceLocation(path);
		if(!resourceMap.containsKey(path))
			resourceMap.put(path, rl);
		return rl;
	}
	/**
	 * 
	 * @return either null (unknown recipe type) or an Object[] with content as follows
	 * 			index 0: width of the recipe
	 * 			index 1: height of the recipe
	 * 			index 2: recipe as PositionedItemStack[]
	 */
	public static Object[] getRecipeForDisplay(IRecipe rec)
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
		else
		{
			try {
				IELogger.info("Found custom IRecipe with output "+rec.getRecipeOutput());
			} catch (Exception x) {
				IELogger.info("Found custom IRecipe with unknown output");
			}
			return null;
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
		PositionedItemStack[] pIngredients = new PositionedItemStack[ingredients.length+1];
		int xBase = (120-(w+2)*18)/2;
		for(int hh=0; hh<h; hh++)
			for(int ww=0; ww<w; ww++)
				if(hh*w+ww<ingredients.length)
					pIngredients[hh*w+ww] = new PositionedItemStack(ingredients[hh*w+ww], xBase+ww*18,hh*18);
		pIngredients[pIngredients.length-1] = new PositionedItemStack(rec.getRecipeOutput(), xBase+w*18+18, (int)(h/2f*18)-8);
		return new Object[]{w, h, pIngredients};
	}
}
