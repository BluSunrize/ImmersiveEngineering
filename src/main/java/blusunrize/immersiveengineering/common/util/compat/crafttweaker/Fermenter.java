package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import minetweaker.IUndoableAction;
import minetweaker.CraftTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.liquid.ILiquidStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;

@ZenClass("mods.immersiveengineering.Fermenter")
public class Fermenter
{
	@ZenMethod
	public static void addRecipe(IItemStack output, ILiquidStack fluid, IIngredient input, int energy)
	{
		if(CraftTweakerHelper.toObject(input) == null)
			return;
		//Either output or fluid must not be null. 
		if(CraftTweakerHelper.toStack(output).isEmpty() && (CraftTweakerHelper.toFluidStack(fluid) == null || CraftTweakerHelper.toFluidStack(fluid).getFluid() == null))
			return;

		FermenterRecipe r = new FermenterRecipe(CraftTweakerHelper.toFluidStack(fluid), CraftTweakerHelper.toStack(output), CraftTweakerHelper.toObject(input), energy);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final FermenterRecipe recipe;

		public Add(FermenterRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			FermenterRecipe.recipeList.add(recipe);
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
			FermenterRecipe.recipeList.remove(recipe);
			IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public String describe()
		{
			String fluid = recipe.fluidOutput != null ? recipe.fluidOutput.getLocalizedName() : "null";
			String out = !recipe.itemOutput.isEmpty() ? recipe.itemOutput.getDisplayName() : "null";
			return "Adding Fermenter Recipe for Fluid " + fluid + " and Item " + out;
		}

		@Override
		public String describeUndo()
		{
			String fluid = recipe.fluidOutput != null ? recipe.fluidOutput.getLocalizedName() : "null";
			String out = !recipe.itemOutput.isEmpty() ? recipe.itemOutput.getDisplayName() : "null";
			return "Removing Fermenter Recipe for Fluid " + fluid + " and Item " + out;
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}

	@ZenMethod
	public static void removeFluidRecipe(ILiquidStack fluid)
	{
		if(CraftTweakerHelper.toFluidStack(fluid) != null)
			CraftTweakerAPI.apply(new RemoveFluid(CraftTweakerHelper.toFluidStack(fluid)));
	}

	private static class RemoveFluid implements IUndoableAction
	{
		private final FluidStack output;
		ArrayList<FermenterRecipe> removedRecipes = new ArrayList<FermenterRecipe>();

		public RemoveFluid(FluidStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			Iterator<FermenterRecipe> it = FermenterRecipe.recipeList.iterator();
			while(it.hasNext())
			{
				FermenterRecipe r = it.next();
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
				for(FermenterRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						FermenterRecipe.recipeList.add(recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Fermenter Recipes for Fluid " + output.getLocalizedName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Fermenter Recipes for Fluid " + output.getLocalizedName();
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
	public static void removeItemRecipe(IItemStack stack)
	{
		if(!CraftTweakerHelper.toStack(stack).isEmpty())
			CraftTweakerAPI.apply(new RemoveStack(CraftTweakerHelper.toStack(stack)));
	}

	private static class RemoveStack implements IUndoableAction
	{
		private final ItemStack output;
		ArrayList<FermenterRecipe> removedRecipes = new ArrayList<FermenterRecipe>();

		public RemoveStack(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			Iterator<FermenterRecipe> it = FermenterRecipe.recipeList.iterator();
			while(it.hasNext())
			{
				FermenterRecipe r = it.next();
				if(r != null && OreDictionary.itemMatches(output, r.itemOutput, false))
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
				for(FermenterRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						FermenterRecipe.recipeList.add(recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Fermenter Recipes for ItemStack " + output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Fermenter Recipes for ItemStack " + output.getDisplayName();
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
	public static void removeByInput(IItemStack stack)
	{
		if(CraftTweakerHelper.toStack(stack) != null)
			CraftTweakerAPI.apply(new RemoveByInput(CraftTweakerHelper.toStack(stack)));
	}

	private static class RemoveByInput implements IUndoableAction
	{
		private final ItemStack input;
		ArrayList<FermenterRecipe> removedRecipes = new ArrayList<FermenterRecipe>();

		public RemoveByInput(ItemStack input)
		{
			this.input = input;
		}

		@Override
		public void apply()
		{
			Iterator<FermenterRecipe> it = FermenterRecipe.recipeList.iterator();
			while(it.hasNext())
			{
				FermenterRecipe r = it.next();
				if(r != null && r.input.matchesItemStack(input))
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
				for(FermenterRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						FermenterRecipe.recipeList.add(recipe);
						IECompatModule.jeiAddFunc.accept(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Fermenter Recipes for input " + input.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Fermenter Recipes for input " + input.getDisplayName();
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
