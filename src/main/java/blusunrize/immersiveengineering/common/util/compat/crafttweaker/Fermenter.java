/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.item.IItemStack;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ZenClass("mods.immersiveengineering.Fermenter")
public class Fermenter
{
	@ZenMethod
	public static void addRecipe(IItemStack output, ILiquidStack fluid, IIngredient input, int energy)
	{
		if(CraftTweakerHelper.toObject(input)==null)
			return;
		//Either output or fluid must not be null. 
		if(CraftTweakerHelper.toStack(output).isEmpty()&&(CraftTweakerHelper.toFluidStack(fluid)==null||CraftTweakerHelper.toFluidStack(fluid).getFluid()==null))
			return;

		FermenterRecipe r = new FermenterRecipe(CraftTweakerHelper.toFluidStack(fluid), CraftTweakerHelper.toStack(output), CraftTweakerHelper.toObject(input), energy);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
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
		}

		@Override
		public String describe()
		{
			String fluid = recipe.fluidOutput!=null?recipe.fluidOutput.getLocalizedName(): "null";
			String out = !recipe.itemOutput.isEmpty()?recipe.itemOutput.getDisplayName(): "null";
			return "Adding Fermenter Recipe for Fluid "+fluid+" and Item "+out;
		}
	}

	@ZenMethod
	public static void removeFluidRecipe(ILiquidStack fluid)
	{
		if(CraftTweakerHelper.toFluidStack(fluid)!=null)
			CraftTweakerAPI.apply(new RemoveFluid(CraftTweakerHelper.toFluidStack(fluid)));
	}

	private static class RemoveFluid implements IAction
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
				if(r!=null&&r.fluidOutput!=null&&r.fluidOutput.isFluidEqual(output))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
		}

		@Override
		public String describe()
		{
			return "Removing Fermenter Recipes for Fluid "+output.getLocalizedName();
		}
	}

	@ZenMethod
	public static void removeItemRecipe(IItemStack stack)
	{
		if(!CraftTweakerHelper.toStack(stack).isEmpty())
			CraftTweakerAPI.apply(new RemoveStack(CraftTweakerHelper.toStack(stack)));
	}

	private static class RemoveStack implements IAction
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
				if(r!=null&&OreDictionary.itemMatches(output, r.itemOutput, false))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
		}

		@Override
		public String describe()
		{
			return "Removing Fermenter Recipes for ItemStack "+output.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeByInput(IItemStack stack)
	{
		if(CraftTweakerHelper.toStack(stack)!=null)
			CraftTweakerAPI.apply(new RemoveByInput(CraftTweakerHelper.toStack(stack)));
	}

	private static class RemoveByInput implements IAction
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
				if(r!=null&&r.input.matchesItemStack(input))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
		}

		@Override
		public String describe()
		{
			return "Removing Fermenter Recipes for input "+input.getDisplayName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		List<FermenterRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList<>(FermenterRecipe.recipeList);
			FermenterRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Fermenter Recipes";
		}
	}
}
