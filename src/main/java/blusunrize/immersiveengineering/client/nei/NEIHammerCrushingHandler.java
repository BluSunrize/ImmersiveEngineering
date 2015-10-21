package blusunrize.immersiveengineering.client.nei;

import java.awt.Rectangle;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraftforge.oredict.OreDictionary;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Lib;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIHammerCrushingHandler extends TemplateRecipeHandler
{
	public class CachedHammerCrushingRecipe extends CachedRecipe
	{
		PositionedStack[] inputs;
		PositionedStack output;
		public CachedHammerCrushingRecipe(String inputType, String oreName)
		{
			inputs = new PositionedStack[2];
			for(int j=0;j<inputs.length;j++)
				inputs[j]= new PositionedStack(j==0?new ItemStack(IEContent.itemTool):OreDictionary.getOres(inputType+oreName) ,25+(j%3)*18, 6+(j/3)*18, j!=0);
			ItemStack dust = Utils.copyStackWithAmount(IEApi.getPreferredOreStack("dust"+oreName), ("ore".equals(inputType)?2:1));
			output = new PositionedStack(dust, 119,24);
		}
		@Override
		public List<PositionedStack> getIngredients()
		{
			return this.getCycledIngredients(NEIHammerCrushingHandler.this.cycleticks/20, Arrays.asList(inputs[(NEIHammerCrushingHandler.this.cycleticks/20)%inputs.length]));
		}
		@Override
		public PositionedStack getIngredient()
		{
			return null;
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
		transferRects.add(new RecipeTransferRect(new Rectangle(84,23, 24,18), "ieHammerCrushing"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId == getOverlayIdentifier())
		{
			for(String oreName : IEContent.validCrushingOres)
			{
				this.arecipes.add(new CachedHammerCrushingRecipe("ore",oreName));
				this.arecipes.add(new CachedHammerCrushingRecipe("ingot",oreName));
			}
		}
		else
		{
			super.loadCraftingRecipes(outputId, results);
		}
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("recipe.ImmersiveEngineering.hammerCrushing");
	}
	@Override
	public String getGuiTexture()
	{
		return "textures/gui/container/crafting_table.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieHammerCrushing";
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
			for(String oreName : IEContent.validCrushingOres)
				if(Utils.compareToOreName(result, "dust"+oreName))
				{
					this.arecipes.add(new CachedHammerCrushingRecipe("ore",oreName));
					this.arecipes.add(new CachedHammerCrushingRecipe("ingot",oreName));
				}
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
			if(ingredient.getItem().getToolClasses(ingredient).contains(Lib.TOOL_HAMMER))
				for(String oreName : IEContent.validCrushingOres)
				{
					this.arecipes.add(new CachedHammerCrushingRecipe("ore",oreName));
					this.arecipes.add(new CachedHammerCrushingRecipe("ingot",oreName));
				}
			else
			{
				for(String oreName : IEContent.validCrushingOres)
					if(Utils.compareToOreName(ingredient, "ore"+oreName))
						this.arecipes.add(new CachedHammerCrushingRecipe("ore",oreName));
					else if(Utils.compareToOreName(ingredient, "ingot"+oreName))
						this.arecipes.add(new CachedHammerCrushingRecipe("ingot",oreName));
			}

	}
	@Override
	public void drawBackground(int recipe)
	{
		super.drawBackground(recipe);
	}

}