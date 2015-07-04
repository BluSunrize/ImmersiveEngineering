package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.DieselHandler.RefineryRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIRefineryHandler extends TemplateRecipeHandler
{
	public class CachedRefineryRecipe extends CachedRecipe
	{
		FluidStack fluid0;
		FluidStack fluid1;
		FluidStack fluidOut;
		public CachedRefineryRecipe(RefineryRecipe recipe)
		{
			fluid0 = recipe.input0;
			fluid1 = recipe.input1;
			fluidOut = recipe.output;
		}
		@Override
		public PositionedStack getIngredient()
		{
			return null;
		}
		@Override
		public PositionedStack getResult()
		{
			return null;
		}
	}

	@Override
	public void loadTransferRects()
	{
		transferRects.add(new RecipeTransferRect(new Rectangle(72,26, 30,18), "ieRefinery"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId==getOverlayIdentifier())
			for(RefineryRecipe r : DieselHandler.refineryList)
				if(r!=null)
					this.arecipes.add(new CachedRefineryRecipe(r));

		FluidStack fs = null;
		if(outputId == "liquid" && results!=null && results.length>0 && results[0] instanceof FluidStack)
			fs = (FluidStack)results[0];
		if(outputId == "item" && results!=null && results.length>0 && results[0] instanceof ItemStack && FluidContainerRegistry.isFilledContainer((ItemStack) results[0]))
			fs = FluidContainerRegistry.getFluidForFilledItem((ItemStack)results[0]);

		if(fs!=null)
			for(RefineryRecipe r : DieselHandler.refineryList)
				if(r!=null && r.output.isFluidEqual(fs))
					this.arecipes.add(new CachedRefineryRecipe(r));

	}
	@Override
	public void loadUsageRecipes(String inputId, Object... ingredients)
	{
		FluidStack fs = null;
		if(inputId == "liquid" && ingredients!=null && ingredients.length>0 && ingredients[0] instanceof FluidStack)
			fs = (FluidStack)ingredients[0];
		if(inputId == "item" && ingredients!=null && ingredients.length>0 && ingredients[0] instanceof ItemStack && FluidContainerRegistry.isFilledContainer((ItemStack) ingredients[0]))
			fs = FluidContainerRegistry.getFluidForFilledItem((ItemStack)ingredients[0]);

		if(fs!=null)
			for(RefineryRecipe r : DieselHandler.refineryList)
				if(r!=null && (r.input0.isFluidEqual(fs)||r.input1.isFluidEqual(fs)))
					this.arecipes.add(new CachedRefineryRecipe(r));
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.metalMultiblock.refinery.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/refinery.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieRefinery";
	}
	@Override
	public int recipiesPerPage()
	{
		return 2;
	}

	@Override
	public boolean mouseClicked(GuiRecipe gui, int button, int recipe)
	{
		CachedRefineryRecipe r = (CachedRefineryRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			Point localPoint = GuiDraw.getMousePosition();
			int gl = (gui.width-176)/2;
			int gt = (gui.height-176)/2;
			FluidStack fs = null;
			if(localPoint.x>gl+12 && localPoint.x<=gl+12+16  &&  localPoint.y>gt+(64*(recipe%2))+11 && localPoint.y<=gt+(64*(recipe%2))+11+47)
				fs = r.fluid0;
			else if(localPoint.x>gl+60 && localPoint.x<=gl+60+16  &&  localPoint.y>gt+(64*(recipe%2))+11 && localPoint.y<=gt+(64*(recipe%2))+11+47)
				fs = r.fluid1;
			else if(localPoint.x>gl+108 && localPoint.x<=gl+108+16  &&  localPoint.y>gt+(64*(recipe%2))+11 && localPoint.y<=gt+(64*(recipe%2))+1+47)
				fs = r.fluidOut;

			if(fs!=null)
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
		return super.mouseClicked(gui, button, recipe);
	}

	@Override
	public List<String> handleTooltip(GuiRecipe gui, List<String> list, int recipe)
	{
		CachedRefineryRecipe r = (CachedRefineryRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			Point localPoint = GuiDraw.getMousePosition();
			int gl = (gui.width-176)/2;
			int gt = (gui.height-134)/2;
			if(r.fluid0!=null && localPoint.x>gl+12 && localPoint.x<=gl+12+16  &&  localPoint.y>gt+(64*(recipe%2))+11 && localPoint.y<=gt+(64*(recipe%2))+11+47)
			{
				list.add(r.fluid0.getLocalizedName());
				list.add(r.fluid0.amount+" mB");
			}
			if(r.fluid1!=null && localPoint.x>gl+60 && localPoint.x<=gl+60+16  &&  localPoint.y>gt+(64*(recipe%2))+11 && localPoint.y<=gt+(64*(recipe%2))+11+47)
			{
				list.add(r.fluid1.getLocalizedName());
				list.add(r.fluid1.amount+" mB");
			}
			if(r.fluidOut!=null && localPoint.x>gl+108 && localPoint.x<=gl+108+16  &&  localPoint.y>gt+(64*(recipe%2))+11 && localPoint.y<=gt+(64*(recipe%2))+11+47)
			{
				list.add(r.fluidOut.getLocalizedName());
				list.add(r.fluidOut.amount+" mB");
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
		CachedRefineryRecipe r = (CachedRefineryRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			if(r.fluid0!=null)
			{
				int h = (int)Math.max(1,47*(r.fluid0.amount/(float)12000));
				ClientUtils.drawRepeatedFluidIcon(r.fluid0.getFluid(), 8,12+47-h, 16, h);
			}
			if(r.fluid1!=null)
			{
				int h = (int)Math.max(1,47*(r.fluid1.amount/(float)12000));
				ClientUtils.drawRepeatedFluidIcon(r.fluid1.getFluid(), 56,12+47-h, 16, h);
			}
			if(r.fluidOut!=null)
			{
				int h = (int)Math.max(1,47*(r.fluidOut.amount/(float)12000));
				ClientUtils.drawRepeatedFluidIcon(r.fluidOut.getFluid(), 104,12+47-h, 16, h);
			}
		}
		ClientUtils.bindTexture(getGuiTexture());
		drawTexturedModalRect(6,10, 177,31, 20,51);
		drawTexturedModalRect(54,10, 177,31, 20,51);
		drawTexturedModalRect(102,10, 177,31, 20,51);
	}

}