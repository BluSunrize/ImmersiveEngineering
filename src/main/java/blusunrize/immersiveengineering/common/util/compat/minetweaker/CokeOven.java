package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.List;

import blusunrize.immersiveengineering.api.CokeOvenRecipe;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersiveengineering.CokeOven")
public class CokeOven
{
	@ZenMethod
	public static void addRecipe(IItemStack output, int fuelOutput, IIngredient input, int time)
	{
		Object oInput = MTHelper.toObject(input);
		if(oInput==null)
			return;

		CokeOvenRecipe r = new CokeOvenRecipe(MTHelper.toStack(output), oInput, time, fuelOutput);
		MineTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final CokeOvenRecipe recipe;
		public Add(CokeOvenRecipe recipe)
		{
			this.recipe = recipe;
		}
		@Override
		public void apply()
		{
			CokeOvenRecipe.recipeList.add(recipe);
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
		@Override
		public void undo()
		{
			CokeOvenRecipe.recipeList.remove(recipe);
		}
		@Override
		public String describe()
		{
			return "Adding Coke Oven Recipe for " + recipe.output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Removing Coke Oven Recipe for " + recipe.output.getDisplayName();
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
		List<CokeOvenRecipe> removedRecipes;
		public Remove(ItemStack output)
		{
			this.output = output;
		}
		@Override
		public void apply()
		{
			removedRecipes = CokeOvenRecipe.removeRecipes(output);
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(CokeOvenRecipe recipe : removedRecipes)
					if(recipe!=null)
						CokeOvenRecipe.recipeList.add(recipe);
		}
		@Override
		public String describe()
		{
			return "Removing Coke Oven Recipe for " + output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Re-Adding Coke Oven Recipe for " + output.getDisplayName();
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
