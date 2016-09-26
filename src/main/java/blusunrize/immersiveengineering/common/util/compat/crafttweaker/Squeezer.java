package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.SqueezerRecipe;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
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

@ZenClass("mods.immersiveengineering.Squeezer")
public class Squeezer
{
	@ZenMethod
	public static void addRecipe(IItemStack output, ILiquidStack fluid, IIngredient input, int energy)
	{
		if(CraftTweakerHelper.toObject(input) == null)
			return;
		//Either output or fluid must not be null. 
		if(CraftTweakerHelper.toStack(output) == null && (CraftTweakerHelper.toFluidStack(fluid) == null || CraftTweakerHelper.toFluidStack(fluid).getFluid() == null))
			return;

		SqueezerRecipe r = new SqueezerRecipe(CraftTweakerHelper.toFluidStack(fluid), CraftTweakerHelper.toStack(output), CraftTweakerHelper.toObject(input), energy);
		MineTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final SqueezerRecipe recipe;

		public Add(SqueezerRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			SqueezerRecipe.recipeList.add(recipe);
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public void undo()
		{
			SqueezerRecipe.recipeList.remove(recipe);
		}

		@Override
		public String describe()
		{
			String fluid = recipe.fluidOutput != null ? recipe.fluidOutput.getLocalizedName() : "null";
			String out = recipe.itemOutput != null ? recipe.itemOutput.getDisplayName() : "null";
			return "Adding Squeezer Recipe for Fluid " + fluid + " and Item " + out;
		}

		@Override
		public String describeUndo()
		{
			String fluid = recipe.fluidOutput != null ? recipe.fluidOutput.getLocalizedName() : "null";
			String out = recipe.itemOutput != null ? recipe.itemOutput.getDisplayName() : "null";
			return "Removing Squeezer Recipe for Fluid " + fluid + " and Item " + out;
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
			MineTweakerAPI.apply(new RemoveFluid(CraftTweakerHelper.toFluidStack(fluid)));
	}

	private static class RemoveFluid implements IUndoableAction
	{
		private final FluidStack output;
		ArrayList<SqueezerRecipe> removedRecipes = new ArrayList<SqueezerRecipe>();

		public RemoveFluid(FluidStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			Iterator<SqueezerRecipe> it = SqueezerRecipe.recipeList.iterator();
			while(it.hasNext())
			{
				SqueezerRecipe r = it.next();
				if(r != null && r.fluidOutput != null && r.fluidOutput.isFluidEqual(output))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
				for(SqueezerRecipe recipe : removedRecipes)
					if(recipe != null)
						SqueezerRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe()
		{
			return "Removing Squeezer Recipes for Fluid " + output.getLocalizedName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Squeezer Recipes for Fluid " + output.getLocalizedName();
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
		if(CraftTweakerHelper.toStack(stack) != null)
			MineTweakerAPI.apply(new RemoveStack(CraftTweakerHelper.toStack(stack)));
	}

	private static class RemoveStack implements IUndoableAction
	{
		private final ItemStack output;
		ArrayList<SqueezerRecipe> removedRecipes = new ArrayList<SqueezerRecipe>();

		public RemoveStack(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			Iterator<SqueezerRecipe> it = SqueezerRecipe.recipeList.iterator();
			while(it.hasNext())
			{
				SqueezerRecipe r = it.next();
				if(r != null && OreDictionary.itemMatches(output, r.itemOutput, false))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
				for(SqueezerRecipe recipe : removedRecipes)
					if(recipe != null)
						SqueezerRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe()
		{
			return "Removing Squeezer Recipes for ItemStack " + output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Squeezer Recipes for ItemStack " + output.getDisplayName();
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
