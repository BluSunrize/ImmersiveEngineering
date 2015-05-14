package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.List;

import blusunrize.immersiveengineering.api.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.common.util.Utils;
import minetweaker.IUndoableAction;
import minetweaker.MineTweakerAPI;
import minetweaker.api.item.IIngredient;
import minetweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

@ZenClass("mods.immersiveengineering.BlastFurnace")
public class BlastFurnace
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, int time)
	{
		Object oInput = MTHelper.toObject(input);
		if(oInput==null)
			return;

		BlastFurnaceRecipe r = new BlastFurnaceRecipe(MTHelper.toStack(output), oInput, time);
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
		MineTweakerAPI.apply(new Remove(MTHelper.toStack(output)));
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
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(BlastFurnaceRecipe recipe : removedRecipes)
					if(recipe!=null)
						BlastFurnaceRecipe.recipeList.add(recipe);
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
		Object oInput = MTHelper.toObject(input);
		if(oInput==null)
			return;

		MineTweakerAPI.apply(new AddFuel(oInput, time));
	}
	private static class AddFuel implements IUndoableAction
	{
		private final Object fuel;
		private final int burnTime;
		public AddFuel(Object fuel, int burnTime)
		{
			this.fuel = fuel;
			this.burnTime = burnTime;
		}
		@Override
		public void apply()
		{
			BlastFurnaceRecipe.addBlastFuel(fuel, burnTime);
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
		@Override
		public void undo()
		{
			if(fuel instanceof String)
				BlastFurnaceRecipe.blastFuels.remove((String)fuel);
			else if(fuel instanceof ItemStack)
				BlastFurnaceRecipe.blastFuels.remove(Utils.nameFromStack((ItemStack)fuel));
		}
		@Override
		public String describe()
		{
			return "Adding "+(fuel instanceof ItemStack?((ItemStack)fuel).getDisplayName() : (String)fuel)+" as Blast Furnace Fuel";
		}
		@Override
		public String describeUndo()
		{
			return "Removing "+(fuel instanceof ItemStack?((ItemStack)fuel).getDisplayName() : (String)fuel)+" as Blast Furnace Fuel";
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
		MineTweakerAPI.apply(new RemoveFuel(MTHelper.toStack(output)));
	}
	private static class RemoveFuel implements IUndoableAction
	{
		private final String ident;
		int removedTime;
		public RemoveFuel(Object fuel)
		{
			this.ident = (fuel instanceof ItemStack?Utils.nameFromStack((ItemStack)fuel) : (String)fuel);
		}
		@Override
		public void apply()
		{
			if( BlastFurnaceRecipe.blastFuels.containsKey(ident))
			{
				removedTime = BlastFurnaceRecipe.blastFuels.get(ident);
				BlastFurnaceRecipe.blastFuels.remove(ident);
			}
		}
		@Override
		public void undo()
		{
			BlastFurnaceRecipe.blastFuels.put(ident, removedTime);
		}
		@Override
		public String describe()
		{
			return "Removing "+ident+" as Blast Furnace Fuel";
		}
		@Override
		public String describeUndo()
		{
			return "Re-Adding "+ident+" as Blast Furnace Fuel";
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
