package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;

@ZenClass("mods.immersiveengineering.Refinery")
public class Refinery
{
	@ZenMethod
	public static void addRecipe(ILiquidStack output, ILiquidStack input0, ILiquidStack input1, int energy)
	{
		FluidStack fOut = CraftTweakerHelper.toFluidStack(output);
		FluidStack fIn0 = CraftTweakerHelper.toFluidStack(input0);
		FluidStack fIn1 = CraftTweakerHelper.toFluidStack(input1);

		if(fOut==null||fIn0==null||fIn1==null)
			return;

		RefineryRecipe r = new RefineryRecipe(fOut, fIn0, fIn1, energy);
		MineTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final RefineryRecipe recipe;

		public Add(RefineryRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			RefineryRecipe.recipeList.add(recipe);
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
			RefineryRecipe.recipeList.remove(recipe);
			IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Refinery Recipe for " + recipe.output.getLocalizedName();
		}

		@Override
		public String describeUndo()
		{
			return "Removing Refinery Recipe for " + recipe.output.getLocalizedName();
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}

	@ZenMethod
	public static void removeRecipe(ILiquidStack output)
	{
		if(CraftTweakerHelper.toFluidStack(output) != null)
			MineTweakerAPI.apply(new Remove(CraftTweakerHelper.toFluidStack(output)));
	}

	private static class Remove implements IUndoableAction
	{
		private final FluidStack output;
		ArrayList<RefineryRecipe> removedRecipes = new ArrayList<RefineryRecipe>();

		public Remove(FluidStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			Iterator<RefineryRecipe> it = RefineryRecipe.recipeList.iterator();
			while(it.hasNext())
			{
				RefineryRecipe r = it.next();
				if(r != null && r.output.isFluidEqual(output))
				{
					removedRecipes.add(r);
					IECompatModule.jeiRemoveFunc.accept(r);
					it.remove();
				}
			}
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
				for(RefineryRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						RefineryRecipe.recipeList.add(recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Refinery Recipes for " + output.getLocalizedName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Refinery Recipes for " + output.getLocalizedName();
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
