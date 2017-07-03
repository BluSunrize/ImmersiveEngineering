package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import minetweaker.IUndoableAction;
import minetweaker.CraftTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.List;

@ZenClass("mods.immersiveengineering.MetalPress")
public class MetalPress
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, IItemStack mold, int energy, @Optional int inputSize)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput == null)
			return;
		ItemStack sOut = CraftTweakerHelper.toStack(output);
		ItemStack sMold = CraftTweakerHelper.toStack(mold);
		if(!sOut.isEmpty() && !sMold.isEmpty())
		{
			MetalPressRecipe r = new MetalPressRecipe(sOut, oInput, sMold, energy);
			if(inputSize > 0)
				r.setInputSize(inputSize);
			CraftTweakerAPI.apply(new Add(r));
		}
	}

	private static class Add implements IUndoableAction
	{
		private final MetalPressRecipe recipe;

		public Add(MetalPressRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			MetalPressRecipe.recipeList.put(recipe.mold, recipe);
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
			MetalPressRecipe.recipeList.remove(recipe.mold, recipe);
			IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Metal Press Recipe for " + recipe.output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Removing Metal Press Recipe for " + recipe.output.getDisplayName();
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
		List<MetalPressRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = MetalPressRecipe.removeRecipes(output);
			for(MetalPressRecipe recipe : removedRecipes)
				IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
				for(MetalPressRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						MetalPressRecipe.recipeList.put(recipe.mold, recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Metal Press Recipes for " + output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Metal Press Recipes for " + output.getDisplayName();
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

	@ZenMethod
	public static void removeRecipeByMold(IItemStack mold)
	{
		CraftTweakerAPI.apply(new RemoveByMold(CraftTweakerHelper.toStack(mold)));
	}

	private static class RemoveByMold implements IUndoableAction
	{
		private final ComparableItemStack mold;
		List<MetalPressRecipe> removedRecipes;

		public RemoveByMold(ItemStack mold)
		{
			this.mold = ApiUtils.createComparableItemStack(mold);
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList(MetalPressRecipe.recipeList.get(mold));
			MetalPressRecipe.recipeList.removeAll(mold);
			for(MetalPressRecipe recipe : removedRecipes)
				IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
			{
				MetalPressRecipe.recipeList.putAll(mold, removedRecipes);
				for(MetalPressRecipe recipe : removedRecipes)
					IECompatModule.jeiAddFunc.accept(recipe);
			}
		}

		@Override
		public String describe()
		{
			return "Removing Metal Press Recipes for Mold: " + mold.stack.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Metal Press Recipes for Mold: " + mold.stack.getDisplayName();
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