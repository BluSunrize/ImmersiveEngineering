package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.List;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;

@ZenClass("mods.immersiveengineering.Crusher")
public class Crusher
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, int energy, @Optional IItemStack secondaryOutput, @Optional double secondaryChance)
	{
		Object oInput = MTHelper.toObject(input);
		if(oInput==null)
			return;

		CrusherRecipe r = new CrusherRecipe(MTHelper.toStack(output), oInput, energy);
		if(secondaryOutput!=null)
			r.addToSecondaryOutput(MTHelper.toStack(secondaryOutput), (float)secondaryChance);
		MineTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final CrusherRecipe recipe;
		public Add(CrusherRecipe recipe)
		{
			this.recipe = recipe;
		}
		@Override
		public void apply()
		{
			CrusherRecipe.recipeList.add(recipe);
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
		@Override
		public void undo()
		{
			CrusherRecipe.recipeList.remove(recipe);
		}
		@Override
		public String describe()
		{
			return "Adding Crusher Recipe for " + recipe.output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Removing Crusher Recipe for " + recipe.output.getDisplayName();
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
		List<CrusherRecipe> removedRecipes;
		public Remove(ItemStack output)
		{
			this.output = output;
		}
		@Override
		public void apply()
		{
			removedRecipes = CrusherRecipe.removeRecipes(output);
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(CrusherRecipe recipe : removedRecipes)
					if(recipe!=null)
						CrusherRecipe.recipeList.add(recipe);
		}
		@Override
		public String describe()
		{
			return "Removing Crusher Recipe for " + output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Re-Adding Crusher Recipe for " + output.getDisplayName();
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