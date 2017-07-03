package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import minetweaker.IUndoableAction;
import minetweaker.CraftTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.List;

@ZenClass("mods.immersiveengineering.Crusher")
public class Crusher
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, int energy, @Optional IItemStack secondaryOutput, @Optional double secondaryChance)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput == null)
		{
			CraftTweakerAPI.getLogger().logError("Did not add crusher recipe for " + output.getDisplayName() + ", input was null");
			return;
		}

		CrusherRecipe r = new CrusherRecipe(CraftTweakerHelper.toStack(output), oInput, energy);
		if(r.input == null)
		{
			CraftTweakerAPI.getLogger().logError("Did not add crusher recipe for " + output.getDisplayName() + ", converted input was null");
			return;
		}
		if(secondaryOutput != null)
			r.addToSecondaryOutput(CraftTweakerHelper.toStack(secondaryOutput), (float)secondaryChance);
		CraftTweakerAPI.apply(new Add(r));
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
			CrusherRecipe.recipeList.remove(recipe);
			IECompatModule.jeiRemoveFunc.accept(recipe);
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
		CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toStack(output)));
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
			for(CrusherRecipe recipe : removedRecipes)
				IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
				for(CrusherRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						CrusherRecipe.recipeList.add(recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
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