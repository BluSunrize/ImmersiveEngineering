package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import minetweaker.IUndoableAction;
import minetweaker.CraftTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ZenClass("mods.immersiveengineering.Blueprint")
public class Blueprint
{
	@ZenMethod
	public static void addRecipe(String category, IItemStack output, IIngredient[] inputs)
	{
		Object[] oInputs = new Object[inputs.length];
			for(int i = 0; i < inputs.length; i++)
				oInputs[i] = CraftTweakerHelper.toObject(inputs[i]);
		BlueprintCraftingRecipe r = new BlueprintCraftingRecipe(category, CraftTweakerHelper.toStack(output), oInputs);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final BlueprintCraftingRecipe recipe;

		public Add(BlueprintCraftingRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			System.out.println("NOTIFICATION_IE: ADDING RECIPE FOR"+recipe.output);

			if(!BlueprintCraftingRecipe.blueprintCategories.contains(recipe.blueprintCategory))
				BlueprintCraftingRecipe.blueprintCategories.add(recipe.blueprintCategory);
			BlueprintCraftingRecipe.recipeList.put(recipe.blueprintCategory, recipe);
//			CraftTweakerAPI.getIjeiRecipeRegistry().addRecipe(recipe);
			IECompatModule.jeiAddFunc.accept(recipe);
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public void undo()
		{
			BlueprintCraftingRecipe.recipeList.remove(recipe.blueprintCategory, recipe);
			if(BlueprintCraftingRecipe.recipeList.get(recipe.blueprintCategory).isEmpty())
				BlueprintCraftingRecipe.blueprintCategories.remove(recipe.blueprintCategory);
//			CraftTweakerAPI.getIjeiRecipeRegistry().removeRecipe(recipe);
			IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Blueprint Recipe for " + recipe.output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Removing Blueprint Recipe for " + recipe.output.getDisplayName();
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}

	@ZenMethod
	public static void removeRecipe(IItemStack output)
	{
		CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toStack(output)));
	}

	private static class Remove implements IUndoableAction
	{
		private final ItemStack output;
		List<BlueprintCraftingRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList();
			Iterator<String> itCat = BlueprintCraftingRecipe.blueprintCategories.iterator();
			while(itCat.hasNext())
			{
				String category = itCat.next();
				Iterator<BlueprintCraftingRecipe> it = BlueprintCraftingRecipe.recipeList.get(category).iterator();
				while(it.hasNext())
				{
					BlueprintCraftingRecipe ir = it.next();
					if(OreDictionary.itemMatches(ir.output, output, true))
					{
						removedRecipes.add(ir);
//						CraftTweakerAPI.getIjeiRecipeRegistry().removeRecipe(ir);
						IECompatModule.jeiRemoveFunc.accept(ir);
						it.remove();
					}
				}
				if(BlueprintCraftingRecipe.recipeList.get(category).isEmpty())
					itCat.remove();
			}
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
				for(BlueprintCraftingRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						if(!BlueprintCraftingRecipe.blueprintCategories.contains(recipe.blueprintCategory))
							BlueprintCraftingRecipe.blueprintCategories.add(recipe.blueprintCategory);
						BlueprintCraftingRecipe.recipeList.put(recipe.blueprintCategory, recipe);
//						CraftTweakerAPI.getIjeiRecipeRegistry().addRecipe(recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Blueprint Recipe for " + output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Blueprint Recipe for " + output.getDisplayName();
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}
	}
}
