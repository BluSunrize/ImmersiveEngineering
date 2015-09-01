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
import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;

@ZenClass("mods.immersiveengineering.ArcFurnace")
public class ArcFurnace
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, IItemStack slag, int time, int energyPerTick, @Optional IIngredient[] additives)
	{
		Object oInput = MTHelper.toObject(input);
		if(oInput==null)
			return;

		Object[] adds = new Object[additives.length];
		for(int i=0; i<additives.length; i++)
			adds[i] = MTHelper.toObject(additives[i]);
		
		ArcFurnaceRecipe r = new ArcFurnaceRecipe(MTHelper.toStack(output), oInput, MTHelper.toStack(slag), time, energyPerTick, adds);
		MineTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final ArcFurnaceRecipe recipe;
		public Add(ArcFurnaceRecipe recipe)
		{
			this.recipe = recipe;
		}
		@Override
		public void apply()
		{
			ArcFurnaceRecipe.recipeList.add(recipe);
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
		@Override
		public void undo()
		{
			ArcFurnaceRecipe.recipeList.remove(recipe);
		}
		@Override
		public String describe()
		{
			return "Adding ArcFurnace Recipe for " + recipe.output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Removing ArcFurnace Recipe for " + recipe.output.getDisplayName();
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
		List<ArcFurnaceRecipe> removedRecipes;
		public Remove(ItemStack output)
		{
			this.output = output;
		}
		@Override
		public void apply()
		{
			removedRecipes = ArcFurnaceRecipe.removeRecipes(output);
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(ArcFurnaceRecipe recipe : removedRecipes)
					if(recipe!=null)
						ArcFurnaceRecipe.recipeList.add(recipe);
		}
		@Override
		public String describe()
		{
			return "Removing ArcFurnace Recipe for " + output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Re-Adding ArcFurnace Recipe for " + output.getDisplayName();
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