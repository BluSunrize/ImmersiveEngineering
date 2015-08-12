package blusunrize.immersiveengineering.client.nei;

import static codechicken.lib.gui.GuiDraw.changeTexture;
import static codechicken.lib.gui.GuiDraw.drawTexturedModalRect;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.client.ClientUtils;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.Utils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;

public class NEIBlueprintHandler extends TemplateRecipeHandler
{
	public class CachedBlueprintCraftingRecipe extends CachedRecipe
	{
		PositionedStack blueprint;
		PositionedStack[] inputs;
		PositionedStack output;
		public CachedBlueprintCraftingRecipe(int blueprintMeta, BlueprintCraftingRecipe recipe)
		{
			blueprint = new PositionedStack(new ItemStack(IEContent.itemBlueprint,1,blueprintMeta), 19,11);
			ArrayList<Object> formattedInputs = recipe.getFormattedInputs();
			inputs = new PositionedStack[formattedInputs.size()];
			for(int i=0; i<inputs.length; i++)
				if(recipe.inputs[i]!=null)
					inputs[i] = new PositionedStack(formattedInputs.get(i), 75+i%2*18,10+i/2*18);

			output = new PositionedStack(recipe.output, 128,28);
		}
		@Override
		public PositionedStack getOtherStack()
		{
			return blueprint;
		}
		@Override
		public List<PositionedStack> getIngredients()
		{
			if(inputs.length<1)
				return new ArrayList();
			return getCycledIngredients(cycleticks/20, Arrays.asList(inputs));
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
		transferRects.add(new RecipeTransferRect(new Rectangle(4,24, 44,32), "ieBlueprintCrafting"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId == getOverlayIdentifier())
		{
			for(int s=0; s<BlueprintCraftingRecipe.blueprintCategories.size(); s++)
				for(BlueprintCraftingRecipe r : BlueprintCraftingRecipe.recipeList.get(BlueprintCraftingRecipe.blueprintCategories.get(s)))
					this.arecipes.add(new CachedBlueprintCraftingRecipe(s,r));
		}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("tile.ImmersiveEngineering.woodenDevice.modificationWorkbench.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "immersiveengineering:textures/gui/workbench.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieBlueprintCrafting";
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
			for(int s=0; s<BlueprintCraftingRecipe.blueprintCategories.size(); s++)
				for(BlueprintCraftingRecipe r : BlueprintCraftingRecipe.recipeList.get(BlueprintCraftingRecipe.blueprintCategories.get(s)))
					if(r!=null && (Utils.stackMatchesObject(result, r.output)))
						this.arecipes.add(new CachedBlueprintCraftingRecipe(s, r));
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
			if(IEContent.itemBlueprint.equals(ingredient.getItem()))
			{
				for(BlueprintCraftingRecipe r : BlueprintCraftingRecipe.recipeList.get(BlueprintCraftingRecipe.blueprintCategories.get(ingredient.getItemDamage())))
					this.arecipes.add(new CachedBlueprintCraftingRecipe(ingredient.getItemDamage(),r));
			}
			else
				for(int s=0; s<BlueprintCraftingRecipe.blueprintCategories.size(); s++)
					for(BlueprintCraftingRecipe r : BlueprintCraftingRecipe.recipeList.get(BlueprintCraftingRecipe.blueprintCategories.get(s)))
						if(r!=null)
							for(Object o : r.inputs)
								if(Utils.stackMatchesObject(ingredient, o))
								{
									this.arecipes.add(new CachedBlueprintCraftingRecipe(s,r));
									break;
								}
	}

	@Override
	public void drawBackground(int recipe)
	{
		GL11.glColor4f(1, 1, 1, 1);
		changeTexture(getGuiTexture());
		drawTexturedModalRect(0, 0, 5, 11, 166, 63);

		CachedBlueprintCraftingRecipe r = (CachedBlueprintCraftingRecipe) this.arecipes.get(recipe%arecipes.size());
		if(r!=null)
		{
			ClientUtils.drawSlot(19,11, 16,16, 0x33);

			ClientUtils.drawSlot(75,10, 16,16, 0x33);
			ClientUtils.drawSlot(93,10, 16,16, 0x33);
			ClientUtils.drawSlot(75,28, 16,16, 0x33);
			ClientUtils.drawSlot(93,28, 16,16, 0x33);
			ClientUtils.drawSlot(75,46, 16,16, 0x33);
			ClientUtils.drawSlot(93,46, 16,16, 0x33);

			ClientUtils.drawSlot(128,28, 16,16, 0x33);
		}
	}

}