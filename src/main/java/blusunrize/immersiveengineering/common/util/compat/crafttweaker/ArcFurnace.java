package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.ArcFurnaceRecipe;
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

@ZenClass("mods.immersiveengineering.ArcFurnace")
public class ArcFurnace
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, IItemStack slag, int time, int energyPerTick, @Optional IIngredient[] additives, @Optional String specialRecipeType)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput == null)
			return;
		Object[] adds = null;
		if(additives != null)
		{
			adds = new Object[additives.length];
			for(int i = 0; i < additives.length; i++)
				adds[i] = CraftTweakerHelper.toObject(additives[i]);
		}
		ArcFurnaceRecipe r = new ArcFurnaceRecipe(CraftTweakerHelper.toStack(output), oInput, CraftTweakerHelper.toStack(slag), time, energyPerTick, adds);
		if(specialRecipeType != null)
			r.setSpecialRecipeType(specialRecipeType);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
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
			IECompatModule.jeiAddFunc.accept(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding ArcFurnace Recipe for " + recipe.output.getDisplayName();
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
		List<ArcFurnaceRecipe> removedRecipes;

		public Remove(ItemStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			removedRecipes = ArcFurnaceRecipe.removeRecipes(output);
			for(ArcFurnaceRecipe recipe : removedRecipes)
				IECompatModule.jeiRemoveFunc.accept(recipe);
		}

		@Override
		public String describe()
		{
			return "Removing ArcFurnace Recipe for " + output.getDisplayName();
		}
	}
}
