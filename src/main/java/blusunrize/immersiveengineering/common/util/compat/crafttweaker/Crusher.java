package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.CrusherRecipe;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
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

	private static class Add implements IAction
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
		public String describe()
		{
			return "Adding Crusher Recipe for " + recipe.output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeRecipe(IItemStack output)
	{
		CraftTweakerAPI.apply(new Remove(CraftTweakerHelper.toStack(output)));
	}

	private static class Remove implements IAction
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
		public String describe()
		{
			return "Removing Crusher Recipe for " + output.getDisplayName();
		}
	}
}