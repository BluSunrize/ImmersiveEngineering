package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.DieselHandler.FermenterRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.client.gui.GuiFermenter;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIFermenterHandler extends TemplateRecipeHandler
{
	public class CachedFermenterRecipe extends CachedRecipe
	{
		PositionedStack input;
		PositionedStack output;
		FluidStack fluid;
		public int time;
		public CachedFermenterRecipe(FermenterRecipe recipe)
		{
			Object in = recipe.input;
			if(in instanceof String)
				in = OreDictionary.getOres((String)in);
			input = new PositionedStack(in, 19,9);
			if(recipe.output!=null)
				output = new PositionedStack(recipe.output, 86,27);
			fluid = recipe.fluid;
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
	public Class<? extends GuiContainer> getGuiClass()
	{
		return GuiFermenter.class;
	}
	@Override
	public void loadTransferRects()
	{
		transferRects.add(new RecipeTransferRect(new Rectangle(72,12, 30,14), "ieFermenter"));
		transferRects.add(new RecipeTransferRect(new Rectangle(72,44, 30,14), "ieFermenter"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		Fluid ff = null;
		if(outputId == "liquid" && results!=null && results.length>0 && results[0] instanceof FluidStack)
			ff = ((FluidStack)results[0]).getFluid();
		if(outputId == "item" && results!=null && results.length>0 && results[0] instanceof ItemStack && FluidContainerRegistry.isFilledContainer((ItemStack) results[0]))
			ff = FluidContainerRegistry.getFluidForFilledItem((ItemStack) results[0]).getFluid();

		for(FermenterRecipe r : DieselHandler.fermenterList)
			if(r!=null)
				if(outputId == getOverlayIdentifier())
					this.arecipes.add(new CachedFermenterRecipe(r));
				else if(ff!=null && r.fluid!=null && r.fluid.getFluid()==ff)
					this.arecipes.add(new CachedFermenterRecipe(r));
		
		super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.metalMultiblock.fermenter.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/fluidProducer.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieFermenter";
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
			for(FermenterRecipe r : DieselHandler.fermenterList)
				if(r!=null && Utils.stackMatchesObject(result, r.output))
					this.arecipes.add(new CachedFermenterRecipe(r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
		{
			FermenterRecipe r = DieselHandler.findFermenterRecipe(ingredient);
			if(r!=null)
				this.arecipes.add(new CachedFermenterRecipe(r));
		}
	}

	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe)
	{
		CachedFermenterRecipe r = (CachedFermenterRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null && r.fluid!=null)
		{
			Point localPoint = GuiDraw.getMousePosition();
			int gl = (gui.width-176)/2;
			int gt = (gui.height-176)/2;
			if(localPoint.x>gl+110 && localPoint.x<=gl+110+16  &&  localPoint.y>gt+(64*(recipe%2))+13 && localPoint.y<=gt+(64*(recipe%2))+13+47)
			{
				if(button==0)
				{
					if(GuiCraftingRecipe.openRecipeGui("liquid", new Object[] { r.fluid }))
						return true;
				}
				else if(button==1)
				{
					if(GuiUsageRecipe.openRecipeGui("liquid", new Object[] { r.fluid }))
						return true;
				}
			}
		}
		return super.mouseClicked(gui, button, recipe);
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> list, int recipe)
	{
		CachedFermenterRecipe r = (CachedFermenterRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			Point localPoint = GuiDraw.getMousePosition();
			int gl = (gui.width-176)/2;
			int gt = (gui.height-134)/2;
			if(r.fluid!=null && localPoint.x>gl+110 && localPoint.x<=gl+110+16  &&  localPoint.y>gt+(64*(recipe%2))+13 && localPoint.y<=gt+(64*(recipe%2))+13+47)
			{
				list.add(r.fluid.getLocalizedName());
				list.add(r.fluid.amount+" mB");
			}
			if(localPoint.x>gl+79 && localPoint.x<=gl+79+7  &&  localPoint.y>gt+(64*(recipe%2))+25 && localPoint.y<=gt+(64*(recipe%2))+25+18)
				list.add(r.time+" Ticks");
		}
		return super.handleTooltip(gui, list, recipe);
	}


	@Override
	public void drawBackground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(-5,0, 0,8, 176, 68);
		CachedFermenterRecipe r = (CachedFermenterRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			int h = (int)(18*((this.cycleticks%r.time)/(float)r.time));
			ClientUtils.drawGradientRect(75,26+h, 82,44, 0xffd4d2ab, 0xffc4c29e);

			if(r.fluid!=null)
			{
				int max = 12000/r.fluid.amount;
				h = (int)Math.max(1,47*( ((this.cycleticks/2)%max)/(float)max ));
				ClientUtils.drawRepeatedFluidIcon(r.fluid.getFluid(), 106,13+47-h, 16, h);
				ClientUtils.bindTexture(getGuiTexture());
			}
		}
		drawTexturedModalRect(104,11, 177,31, 20,51);
	}

}