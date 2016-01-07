package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

public class NEICokeOvenHandler extends TemplateRecipeHandler
{
	public class CachedCokeOvenRecipe extends CachedRecipe
	{
		PositionedStack input;
		PositionedStack output;
		public int creosote;
		public int time;
		public CachedCokeOvenRecipe(CokeOvenRecipe recipe)
		{
			Object in = recipe.input;
			if(in instanceof String)
				in = OreDictionary.getOres((String)in);
			input = new PositionedStack(in, 25,27);
			output = new PositionedStack(recipe.output, 80,27);
			creosote = recipe.creosoteOutput;
			time = recipe.time;
		}
		@Override
		public List<PositionedStack> getIngredients()
		{
			return getCycledIngredients(cycleticks/20, Arrays.asList(input));
		}
		@Override
		public PositionedStack getResult()
		{
			return output;
		}
	}

	@Override
	public void loadTransferRects()
	{
		transferRects.add(new RecipeTransferRect(new Rectangle(48,21, 21,28), "ieCokeOven"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		boolean b = false;
		if(outputId == "liquid" && results!=null && results.length>0 && results[0] instanceof FluidStack)
			b = ((FluidStack)results[0]).getFluid().equals(IEContent.fluidCreosote);
		if(outputId == "item" && results!=null && results.length>0 && results[0] instanceof ItemStack && FluidContainerRegistry.isFilledContainer((ItemStack) results[0]))
			b = FluidContainerRegistry.getFluidForFilledItem((ItemStack) results[0]).getFluid().equals(IEContent.fluidCreosote);

		if(b || outputId == getOverlayIdentifier())
		{
			for(CokeOvenRecipe r : CokeOvenRecipe.recipeList)
				if(r!=null)
					this.arecipes.add(new CachedCokeOvenRecipe(r));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.stoneDevice.cokeOven.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/cokeOven.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieCokeOven";
	}
	@Override
	public int recipiesPerPage()
	{
		return 2;
	}
	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		if(result!=null)
			for(CokeOvenRecipe r : CokeOvenRecipe.recipeList)
				if(r!=null && Utils.stackMatchesObject(result, r.output))
					this.arecipes.add(new CachedCokeOvenRecipe(r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
		{
			CokeOvenRecipe r = CokeOvenRecipe.findRecipe(ingredient);
			if(r!=null)
				this.arecipes.add(new CachedCokeOvenRecipe(r));
		}
	}

	@Override
	public boolean keyTyped(GuiRecipe gui, char keyChar, int keyCode, int recipe)
	{
		CachedCokeOvenRecipe r = (CachedCokeOvenRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			Point localPoint = GuiDraw.getMousePosition();
			int gl = (gui.width-176)/2;
			int gt = (gui.height-176)/2;
			if(localPoint.x>gl+124 && localPoint.x<=gl+124+16  &&  localPoint.y>gt+12 && localPoint.y<=gt+12+47)
			{
				FluidStack fs = new FluidStack(IEContent.fluidCreosote,r.creosote);
				if(keyCode==NEIClientConfig.getKeyBinding("gui.recipe"))
				{
					if(GuiCraftingRecipe.openRecipeGui("liquid", new Object[] { fs }))
						return true;
				}
				else if(keyCode==NEIClientConfig.getKeyBinding("gui.usage"))
				{
					if(GuiUsageRecipe.openRecipeGui("liquid", new Object[] { fs }))
						return true;
				}
			}
		}
		return super.keyTyped(gui, keyChar, keyCode, recipe); 		
	}
	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe)
	{
		CachedCokeOvenRecipe r = (CachedCokeOvenRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			Point localPoint = GuiDraw.getMousePosition();
			int gl = (gui.width-176)/2;
			int gt = (gui.height-176)/2;
			if(localPoint.x>gl+124 && localPoint.x<=gl+124+16  &&  localPoint.y>gt+12 && localPoint.y<=gt+12+47)
			{
				FluidStack fs = new FluidStack(IEContent.fluidCreosote,r.creosote);
				if(button==0)
				{
					if(GuiCraftingRecipe.openRecipeGui("liquid", new Object[] { fs }))
						return true;
				}
				else if(button==1)
				{
					if(GuiUsageRecipe.openRecipeGui("liquid", new Object[] { fs }))
						return true;
				}
			}
		}
		return super.mouseClicked(gui, button, recipe);
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> list, int recipe)
	{
		CachedCokeOvenRecipe r = (CachedCokeOvenRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			Point localPoint = GuiDraw.getMousePosition();
			int gl = (gui.width-176)/2;
			int gt = (gui.height-134)/2;
			if(localPoint.x>gl+124 && localPoint.x<=gl+124+16  &&  localPoint.y>gt+(64*(recipe%2))+12 && localPoint.y<=gt+(64*(recipe%2))+12+47)
			{
				list.add(IEContent.fluidCreosote.getLocalizedName(new FluidStack(IEContent.fluidCreosote,r.creosote)));
				list.add(r.creosote+" mB");
			}
		}
		return super.handleTooltip(gui, list, recipe);
	}


	@Override
	public void drawBackground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-5,0, 0,8, 176, 68);
		CachedCokeOvenRecipe r = (CachedCokeOvenRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			String s = r.time+" Ticks";
			ClientUtils.font().drawString(s, 50-ClientUtils.font().getStringWidth(s)/2,53, 0xaaaaaa, true);
			GL11.glColor4f(1, 1, 1, 1);

			int h = (int)Math.max(1,47*(r.creosote/(float)12000));
			ClientUtils.drawRepeatedFluidIcon(IEContent.fluidCreosote, 124,12+47-h, 16, h);
			ClientUtils.bindTexture("immersiveengineering:textures/gui/cokeOven.png");
		}
		drawTexturedModalRect(122,10, 175,31, 20,51);
	}

}