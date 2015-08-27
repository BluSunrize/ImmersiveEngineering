package blusunrize.immersiveengineering.common.util.compat.minetweaker;

import java.util.ArrayList;
import java.util.Iterator;

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
import blusunrize.immersiveengineering.api.energy.DieselHandler;
import blusunrize.immersiveengineering.api.energy.DieselHandler.FermenterRecipe;

@ZenClass("mods.immersiveengineering.Fermenter")
public class Fermenter
{
	@ZenMethod
	public static void addRecipe(IItemStack output, ILiquidStack fluid, IIngredient input, int time)
	{
		if(MTHelper.toObject(input)==null)
			return;
		//Either output or fluid must not be null. 
		if(MTHelper.toStack(output)==null && (MTHelper.toFluidStack(fluid)==null||MTHelper.toFluidStack(fluid).getFluid()==null))
			return;

		FermenterRecipe r = new FermenterRecipe(MTHelper.toObject(input), time, MTHelper.toFluidStack(fluid), MTHelper.toStack(output));
		MineTweakerAPI.apply(new Add(r));
	}

	private static class Add implements IUndoableAction
	{
		private final FermenterRecipe recipe;
		public Add(FermenterRecipe recipe)
		{
			this.recipe = recipe;
		}
		@Override
		public void apply()
		{
			DieselHandler.fermenterList.add(recipe);
		}
		@Override
		public boolean canUndo()
		{
			return true;
		}
		@Override
		public void undo()
		{
			DieselHandler.fermenterList.remove(recipe);
		}
		@Override
		public String describe()
		{
			String fluid = recipe.fluid!=null? recipe.fluid.getLocalizedName():"null";
			String out = recipe.output!=null? recipe.output.getDisplayName():"null";
			return "Adding Fermenter Recipe for Fluid "+fluid+" and Item "+out;
		}
		@Override
		public String describeUndo()
		{
			String fluid = recipe.fluid!=null? recipe.fluid.getLocalizedName():"null";
			String out = recipe.output!=null? recipe.output.getDisplayName():"null";
			return "Removing Fermenter Recipe for Fluid "+fluid+" and Item "+out;
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
		if(MTHelper.toFluidStack(fluid)!=null)
			MineTweakerAPI.apply(new RemoveFluid(MTHelper.toFluidStack(fluid)));
	}
	private static class RemoveFluid implements IUndoableAction
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
			Iterator<FermenterRecipe> it = DieselHandler.fermenterList.iterator();
			while(it.hasNext())
			{
				FermenterRecipe r = it.next();
				if(r.fluid!=null&&r.fluid.isFluidEqual(output))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(FermenterRecipe recipe : removedRecipes)
					if(recipe!=null)
						DieselHandler.fermenterList.add(recipe);
		}
		@Override
		public String describe()
		{
			return "Removing Fermenter Recipes for Fluid " + output.getLocalizedName();
		}
		@Override
		public String describeUndo()
		{
			return "Re-Adding Fermenter Recipes for Fluid " + output.getLocalizedName();
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
		if(MTHelper.toStack(stack)!=null)
			MineTweakerAPI.apply(new RemoveStack(MTHelper.toStack(stack)));
	}
	private static class RemoveStack implements IUndoableAction
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
			Iterator<FermenterRecipe> it = DieselHandler.fermenterList.iterator();
			while(it.hasNext())
			{
				FermenterRecipe r = it.next();
				if(OreDictionary.itemMatches(output, r.output, false))
				{
					removedRecipes.add(r);
					it.remove();
				}
			}
		}
		@Override
		public void undo()
		{
			if(removedRecipes!=null)
				for(FermenterRecipe recipe : removedRecipes)
					if(recipe!=null)
						DieselHandler.fermenterList.add(recipe);
		}
		@Override
		public String describe()
		{
			return "Removing Fermenter Recipes for ItemStack " + output.getDisplayName();
		}
		@Override
		public String describeUndo()
		{
			return "Re-Adding Fermenter Recipes for ItemStack " + output.getDisplayName();
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
