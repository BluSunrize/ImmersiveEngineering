package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import minetweaker.IUndoableAction;
import minetweaker.CraftTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;

@ZenClass("mods.immersiveengineering.Mixer")
public class Mixer
{
	@ZenMethod
	public static void addRecipe(ILiquidStack output, ILiquidStack fluidInput, IIngredient[] itemInputs, int energy)
	{
		Object[] adds = null;
		if(itemInputs != null)
		{
			adds = new Object[itemInputs.length];
			for(int i = 0; i < itemInputs.length; i++)
				adds[i] = CraftTweakerHelper.toObject(itemInputs[i]);
		}

		MixerRecipe r = new MixerRecipe(CraftTweakerHelper.toFluidStack(output), CraftTweakerHelper.toFluidStack(fluidInput), adds, energy);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final MixerRecipe recipe;

		public Add(MixerRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			MixerRecipe.recipeList.add(recipe);
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
			MixerRecipe.recipeList.remove(recipe);
			IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Fermenter Recipe for Fluid " + recipe.fluidOutput.getLocalizedName();
		}

		@Override
		public String describeUndo()
		{
			return "Removing Fermenter Recipe for Fluid " + recipe.fluidOutput.getLocalizedName();
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
			CraftTweakerAPI.apply(new RemoveFluid(CraftTweakerHelper.toFluidStack(output)));
	}

	private static class RemoveFluid implements IUndoableAction
	{
		private final FluidStack output;
		ArrayList<MixerRecipe> removedRecipes = new ArrayList<MixerRecipe>();

		public RemoveFluid(FluidStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			Iterator<MixerRecipe> it = MixerRecipe.recipeList.iterator();
			while(it.hasNext())
			{
				MixerRecipe r = it.next();
				if(r != null && r.fluidOutput != null && r.fluidOutput.isFluidEqual(output))
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
				for(MixerRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						MixerRecipe.recipeList.add(recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Mixer Recipes for Fluid " + output.getLocalizedName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Mixer Recipes for Fluid " + output.getLocalizedName();
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
