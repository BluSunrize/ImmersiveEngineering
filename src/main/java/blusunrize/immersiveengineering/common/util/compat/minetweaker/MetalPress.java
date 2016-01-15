package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.List;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import blusunrize.immersiveengineering.api.crafting.MetalPressRecipe;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersiveengineering.MetalPress")
public class MetalPress
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, IItemStack mold, int energy, @Optional int inputSize)
	{
		Object oInput = MTHelper.toObject(input);
		if(oInput==null)
			return;
		ItemStack sOut = MTHelper.toStack(output);
		ItemStack sMold = MTHelper.toStack(mold);
		if(sOut!=null && sMold!=null)
		{
			MetalPressRecipe r = new MetalPressRecipe(sOut, oInput, sMold, energy);
			if(inputSize>0)
				r.setInputSize(inputSize);
			MineTweakerAPI.apply(new Add(r));
		}
		else
			System.out.println("SOut:"+output+"|"+sOut+", sMold:"+mold+"|"+sMold);
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
		MineTweakerAPI.apply(new Remove(MTHelper.toStack(output)));
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
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(MetalPressRecipe recipe : removedRecipes)
					if(recipe!=null)
						MetalPressRecipe.recipeList.put(recipe.mold, recipe);
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
		MineTweakerAPI.apply(new RemoveByMold(MTHelper.toStack(mold)));
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
			removedRecipes = MetalPressRecipe.recipeList.get(mold);
			MetalPressRecipe.recipeList.removeAll(mold);
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				MetalPressRecipe.recipeList.putAll(mold, removedRecipes);
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