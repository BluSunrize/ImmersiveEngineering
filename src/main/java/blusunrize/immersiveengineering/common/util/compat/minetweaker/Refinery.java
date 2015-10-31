package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.ArrayList;
import java.util.Iterator;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.DieselHandler.RefineryRecipe;

@ZenClass("mods.immersiveengineering.Refinery")
public class Refinery
{
	@ZenMethod
	public static void addRecipe(ILiquidStack output, ILiquidStack input0, ILiquidStack input1)
	{
		if(MTHelper.toFluidStack(input0)==null||MTHelper.toFluidStack(input0).getFluid()==null)
			return;
		if(MTHelper.toFluidStack(input1)==null||MTHelper.toFluidStack(input1).getFluid()==null)
			return;
		if(MTHelper.toFluidStack(output)==null||MTHelper.toFluidStack(output).getFluid()==null)
			return;

		RefineryRecipe r = new RefineryRecipe(MTHelper.toFluidStack(input0), MTHelper.toFluidStack(input1), MTHelper.toFluidStack(output));
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
			DieselHandler.refineryList.add(recipe);
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
		@Override
		public void undo()
		{
			DieselHandler.refineryList.remove(recipe);
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
		if(MTHelper.toFluidStack(output)!=null)
			MineTweakerAPI.apply(new Remove(MTHelper.toFluidStack(output)));
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
			Iterator<RefineryRecipe> it = DieselHandler.refineryList.iterator();
			while(it.hasNext())
			{
				RefineryRecipe r = it.next();
				if(r!=null && r.output.isFluidEqual(output))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(RefineryRecipe recipe : removedRecipes)
					if(recipe!=null)
						DieselHandler.refineryList.add(recipe);
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
