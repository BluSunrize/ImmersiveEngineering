/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe.BlastFurnaceFuel;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import net.minecraft.item.ItemStack;
import stanhebben.zenscript.annotations.Optional;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ZenClass("mods.immersiveengineering.BlastFurnace")
public class BlastFurnace
{
	@ZenMethod
	public static void addRecipe(IItemStack output, IIngredient input, int time, @Optional IItemStack slag)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput==null)
			return;

		BlastFurnaceRecipe r = new BlastFurnaceRecipe(CraftTweakerHelper.toStack(output), oInput, time, CraftTweakerHelper.toStack(slag));
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
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
		public String describe()
		{
			return "Adding Blast Furnace Recipe for "+recipe.output.getDisplayName();
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
		public String describe()
		{
			return "Removing Blast Furnace Recipe for "+output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		List<BlastFurnaceRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList<>(BlastFurnaceRecipe.recipeList);
			BlastFurnaceRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Blast Furnace Recipes";
		}
	}

	@ZenMethod
	public static void addFuel(IIngredient input, int time)
	{
		Object oInput = CraftTweakerHelper.toObject(input);
		if(oInput==null)
			return;

		CraftTweakerAPI.apply(new AddFuel(oInput, time));
	}

	private static class AddFuel implements IAction
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
		}

		@Override
		public String describe()
		{
			return "Adding "+(fuel instanceof ItemStack?((ItemStack)fuel).getDisplayName(): (String)fuel)+" as Blast Furnace Fuel";
		}
	}

	@ZenMethod
	public static void removeFuel(IItemStack output)
	{
		CraftTweakerAPI.apply(new RemoveFuel(CraftTweakerHelper.toStack(output)));
	}

	private static class RemoveFuel implements IAction
	{
		private final ItemStack stack;
		BlastFurnaceFuel removed;

		public RemoveFuel(ItemStack fuel)
		{
			this.stack = fuel;
		}

		@Override
		public void apply()
		{
			Iterator<BlastFurnaceFuel> it = BlastFurnaceRecipe.blastFuels.iterator();
			while(it.hasNext())
			{
				BlastFurnaceFuel e = it.next();
				if(e.input.matchesItemStack(stack))
				{
					removed = e;
					it.remove();
					break;
				}
			}
		}

		@Override
		public String describe()
		{
			return "Removing "+stack+" as Blast Furnace Fuel";
		}
	}
}
