package blusunrize.immersiveengineering.client.nei;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry.ShaderRegistryEntry;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.TemplateRecipeHandler;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;

public class NEIShaderBagHandler extends TemplateRecipeHandler
{
	public class CachedShaderBagRecipe extends CachedRecipe
	{
		PositionedStack input;
		PositionedStack output;
		public CachedShaderBagRecipe(EnumRarity outputRarity, boolean inputBag)
		{
			ArrayList<EnumRarity> upperRarities = ShaderRegistry.getHigherRarities(outputRarity);
			ArrayList<ItemStack> inputList = new ArrayList();
			if(inputBag)
				for(EnumRarity r : upperRarities)
				{
					ItemStack bag = new ItemStack(IEContent.itemShaderBag);
					ItemNBTHelper.setString(bag, "rarity",  r.toString());
					inputList.add(bag);
				}
			else
				for(ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
					if(upperRarities.contains(entry.getRarity()))
					{
						ItemStack shader = new ItemStack(IEContent.itemShader);
						ItemNBTHelper.setString(shader, "shader_name",  entry.getName());
						inputList.add(shader);
					}
			this.input = new PositionedStack(inputList, 25, 6, true);

			ItemStack bag = new ItemStack(IEContent.itemShaderBag, inputBag?2:1);
			ItemNBTHelper.setString(bag, "rarity", outputRarity.toString());
			this.output = new PositionedStack(bag, 119,24);
		}
		@Override
		public List<PositionedStack> getIngredients()
		{
			input.setPermutationToRender(NEIShaderBagHandler.this.cycleticks/20 % input.items.length);
			return Arrays.asList(input);
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
		transferRects.add(new RecipeTransferRect(new Rectangle(84,23, 24,18), "ieShaderBag"));
	}
	@Override
	public void loadCraftingRecipes(String outputId, Object... results)
	{
		if(outputId == getOverlayIdentifier())
			for(int i=1; i<ShaderRegistry.sortedRarityMap.size(); i++)
			{
				this.arecipes.add(new CachedShaderBagRecipe(ShaderRegistry.sortedRarityMap.get(i),true));
				this.arecipes.add(new CachedShaderBagRecipe(ShaderRegistry.sortedRarityMap.get(i),false));
			}
		else
			super.loadCraftingRecipes(outputId, results);
	}
	@Override
	public String getRecipeName()
	{
		return StatCollector.translateToLocal("item.ImmersiveEngineering.shaderBag.name");
	}
	@Override
	public String getGuiTexture()
	{
		return "textures/gui/container/crafting_table.png";
	}
	@Override
	public String getOverlayIdentifier()
	{
		return "ieShaderBag";
	}
	@Override
	public int recipiesPerPage()
	{
		return 2;
	}
	@Override
	public void loadCraftingRecipes(ItemStack result)
	{
		if(result!=null && IEContent.itemShaderBag.equals(result.getItem()))
		{
			ArrayList<EnumRarity> list = ShaderRegistry.getHigherRarities(result.getRarity());
			if(!list.isEmpty())
			{
				this.arecipes.add(new CachedShaderBagRecipe(result.getRarity(),true));
				for(EnumRarity r : list)
					if(ShaderRegistry.totalWeight.containsKey(r) && ShaderRegistry.totalWeight.get(r)>0)
						this.arecipes.add(new CachedShaderBagRecipe(result.getRarity(),false));
			}
		}
	}
	@Override
	public void loadUsageRecipes(ItemStack ingredient)
	{
		if(ingredient!=null)
			if(IEContent.itemShaderBag.equals(ingredient.getItem()) || IEContent.itemShader.equals(ingredient.getItem()))
			{
				EnumRarity r = ShaderRegistry.getLowerRarity(ingredient.getRarity());
				if(r!=null)
					this.arecipes.add(new CachedShaderBagRecipe(r,IEContent.itemShaderBag.equals(ingredient.getItem())));
			}
	}
	@Override
	public void drawBackground(int recipe)
	{
		super.drawBackground(recipe);
	}

}