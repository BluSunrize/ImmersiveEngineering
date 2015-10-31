package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.List;

import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import minetweaker.api.liquid.ILiquidStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;
import blusunrize.immersiveengineering.api.crafting.BottlingMachineRecipe;

@ZenClass("mods.immersiveengineering.BottlingMachine")
public class BottlingMachine
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, ILiquidStack fluid)
	{
		Object oInput = MTHelper.toObject(input);
		if(oInput==null || output==null || fluid==null)
			return;

		BottlingMachineRecipe r = new BottlingMachineRecipe(MTHelper.toStack(output), oInput, MTHelper.toFluidStack(fluid));
		MineTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final BottlingMachineRecipe recipe;
		public Add(BottlingMachineRecipe recipe)
		{
			this.recipe = recipe;
		}
		@Override
		public void apply()
		{
			BottlingMachineRecipe.recipeList.add(recipe);
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
		@Override
		public void undo()
		{
			BottlingMachineRecipe.recipeList.remove(recipe);
		}
		@Override
		public String describe()
		{
			return "Adding Bottling Machine Recipe for " + recipe.output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Removing Bottling Machine Recipe for " + recipe.output.getDisplayName();
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
		List<BottlingMachineRecipe> removedRecipes;
		public Remove(ItemStack output)
		{
			this.output = output;
		}
		@Override
		public void apply()
		{
			
			removedRecipes = BottlingMachineRecipe.removeRecipes(output);
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(BottlingMachineRecipe recipe : removedRecipes)
					if(recipe!=null)
						BottlingMachineRecipe.recipeList.add(recipe);
		}
		@Override
		public String describe()
		{
			return "Removing Bottling Machine Recipe for " + output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Re-Adding Bottling Machine Recipe for " + output.getDisplayName();
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
