/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.compat.crafttweaker;

import blusunrize.immersiveengineering.api.crafting.MixerRecipe;
import blusunrize.immersiveengineering.common.crafting.MixerPotionHelper;
import crafttweaker.CraftTweakerAPI;
import crafttweaker.IAction;
import crafttweaker.api.item.IIngredient;
import crafttweaker.api.liquid.ILiquidStack;
import net.minecraftforge.fluids.FluidStack;
import stanhebben.zenscript.annotations.ZenClass;
import stanhebben.zenscript.annotations.ZenMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ZenClass("mods.immersiveengineering.Mixer")
public class Mixer
{
	@ZenMethod
	public static void addRecipe(ILiquidStack output, ILiquidStack fluidInput, IIngredient[] itemInputs, int energy)
	{
		Object[] adds = null;
		if(itemInputs!=null)
		{
			adds = new Object[itemInputs.length];
			for(int i = 0; i < itemInputs.length; i++)
				adds[i] = CraftTweakerHelper.toObject(itemInputs[i]);
		}

		MixerRecipe r = new MixerRecipe(CraftTweakerHelper.toFluidStack(output), CraftTweakerHelper.toFluidStack(fluidInput), adds, energy);
		CraftTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IAction
	{
		private final MixerRecipe recipe;

		public Add(MixerRecipe recipe)
		{
			this.recipe = recipe;
		}

		@Override
		public void apply()
		{
			MixerRecipe.recipeList.add(recipe);
		}

		@Override
		public String describe()
		{
			return "Adding Fermenter Recipe for Fluid "+recipe.fluidOutput.getLocalizedName();
		}
	}

	@ZenMethod
	public static void removeRecipe(ILiquidStack output)
	{
		if(CraftTweakerHelper.toFluidStack(output)!=null)
			CraftTweakerAPI.apply(new RemoveFluid(CraftTweakerHelper.toFluidStack(output)));
	}

	private static class RemoveFluid implements IAction
	{
		private final FluidStack output;
		ArrayList<MixerRecipe> removedRecipes = new ArrayList<MixerRecipe>();

		public RemoveFluid(FluidStack output)
		{
			this.output = output;
		}

		@Override
		public void apply()
		{
			Iterator<MixerRecipe> it = MixerRecipe.recipeList.iterator();
			while(it.hasNext())
			{
				MixerRecipe r = it.next();
				if(r!=null&&r.fluidOutput!=null&&r.fluidOutput.isFluidEqual(output))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
			if(this.output.tag!=null&&this.output.tag.hasKey("Potion"))
				MixerPotionHelper.BLACKLIST.add(this.output.tag.getString("Potion"));
		}

		@Override
		public String describe()
		{
			return "Removing Mixer Recipes for Fluid "+output.getLocalizedName();
		}
	}

	@ZenMethod
	public static void removeAll()
	{
		CraftTweakerAPI.apply(new RemoveAll());
	}

	private static class RemoveAll implements IAction
	{
		List<MixerRecipe> removedRecipes;

		public RemoveAll(){
		}

		@Override
		public void apply()
		{
			removedRecipes = new ArrayList<>(MixerRecipe.recipeList);
			MixerRecipe.recipeList.clear();
		}

		@Override
		public String describe()
		{
			return "Removing all Mixer Recipes";
		}
	}
}
