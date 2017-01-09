package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ZenClass("mods.immersiveengineering.BlastFurnace")
public class BlastFurnace
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, int time, @Optional IItemStack slag)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput == null)
			return;

		BlastFurnaceRecipe r = new BlastFurnaceRecipe(CraftTweakerHelper.toStack(output), oInput, time, CraftTweakerHelper.toStack(slag));
		MineTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final BlastFurnaceRecipe recipe;

		public Add(BlastFurnaceRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			BlastFurnaceRecipe.recipeList.add(recipe);
			MineTweakerAPI.getIjeiRecipeRegistry().addRecipe(recipe);
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public void undo()
		{
			BlastFurnaceRecipe.recipeList.remove(recipe);
			MineTweakerAPI.getIjeiRecipeRegistry().removeRecipe(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Blast Furnace Recipe for " + recipe.output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Removing Blast Furnace Recipe for " + recipe.output.getDisplayName();
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
		MineTweakerAPI.apply(new Remove(CraftTweakerHelper.toStack(output)));
	}

	private static class Remove implements IUndoableAction
	{
		private final ItemStack output;
		List<BlastFurnaceRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = BlastFurnaceRecipe.removeRecipes(output);
			for(BlastFurnaceRecipe recipe : removedRecipes)
				MineTweakerAPI.getIjeiRecipeRegistry().removeRecipe(recipe);
		}

		@Override
		public void undo()
		{
			if(removedRecipes != null)
				for(BlastFurnaceRecipe recipe : removedRecipes)
					if(recipe != null)
					{
						BlastFurnaceRecipe.recipeList.add(recipe);
						MineTweakerAPI.getIjeiRecipeRegistry().addRecipe(recipe);
					}
		}

		@Override
		public String describe()
		{
			return "Removing Blast Furnace Recipe for " + output.getDisplayName();
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding Blast Furnace Recipe for " + output.getDisplayName();
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
	public static void addFuel(IIngredient input, int time)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput == null)
			return;

		MineTweakerAPI.apply(new AddFuel(oInput, time));
	}

	private static class AddFuel implements IUndoableAction
	{
		private final Object fuel;
		private Object fuelRecipeKey;
		private final int burnTime;

		public AddFuel(Object fuel, int burnTime)
		{
			this.fuel = fuel;
			this.burnTime = burnTime;
		}

		@Override
		public void apply()
		{
			fuelRecipeKey = BlastFurnaceRecipe.addBlastFuel(fuel, burnTime);
			MineTweakerAPI.getIjeiRecipeRegistry().addRecipe(fuelRecipeKey);
		}

		@Override
		public boolean canUndo()
		{
			return true;
		}

		@Override
		public void undo()
		{
			BlastFurnaceRecipe.blastFuels.remove(fuelRecipeKey);
			MineTweakerAPI.getIjeiRecipeRegistry().removeRecipe(fuelRecipeKey);
		}

		@Override
		public String describe()
		{
			return "Adding " + (fuel instanceof ItemStack ? ((ItemStack)fuel).getDisplayName() : (String)fuel) + " as Blast Furnace Fuel";
		}

		@Override
		public String describeUndo()
		{
			return "Removing " + (fuel instanceof ItemStack ? ((ItemStack)fuel).getDisplayName() : (String)fuel) + " as Blast Furnace Fuel";
		}

		@Override
		public Object getOverrideKey()
		{
			return null;
		}
	}

	@ZenMethod
	public static void removeFuel(IItemStack output)
	{
		MineTweakerAPI.apply(new RemoveFuel(CraftTweakerHelper.toStack(output)));
	}

	private static class RemoveFuel implements IUndoableAction
	{
		private final ItemStack stack;
		Object ident;
		int removedTime;

		public RemoveFuel(ItemStack fuel)
		{
			this.stack = fuel;
		}

		@Override
		public void apply()
		{
			Set<Map.Entry<Object, Integer>> set = BlastFurnaceRecipe.blastFuels.entrySet();
			Iterator<Map.Entry<Object, Integer>> it = set.iterator();
			while(it.hasNext())
			{
				Map.Entry<Object, Integer> e = it.next();
				if(ApiUtils.stackMatchesObject(stack, e.getKey()))
				{
					removedTime = e.getValue();
					ident = e.getKey();
					MineTweakerAPI.getIjeiRecipeRegistry().removeRecipe(ident);
					it.remove();
					break;
				}
			}
		}

		@Override
		public void undo()
		{
			if(ident != null)
			{
				BlastFurnaceRecipe.blastFuels.put(ident, removedTime);
				MineTweakerAPI.getIjeiRecipeRegistry().addRecipe(ident);
			}
		}

		@Override
		public String describe()
		{
			return "Removing " + stack + " as Blast Furnace Fuel";
		}

		@Override
		public String describeUndo()
		{
			return "Re-Adding " + stack + " as Blast Furnace Fuel";
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
