/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import com.google.common.collect.Lists;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author BluSunrize - 02.03.2016
 * <p>
 * The recipe for the Refinery
 */
public class RefineryRecipe extends MultiblockRecipe
{
	public static float energyModifier = 1;
	public static float timeModifier = 1;

	public final FluidStack output;
	public final FluidStack input0;
	public final FluidStack input1;

	public RefineryRecipe(FluidStack output, FluidStack input0, FluidStack input1, int energy)
	{
		this.output = output;
		this.input0 = input0;
		this.input1 = input1;
		this.totalProcessEnergy = (int)Math.floor(energy*energyModifier);
		this.totalProcessTime = (int)Math.floor(1*timeModifier);

		this.fluidInputList = Lists.newArrayList(this.input0, this.input1);
		this.fluidOutputList = Lists.newArrayList(this.output);
	}

	public static ArrayList<RefineryRecipe> recipeList = new ArrayList<>();

	public static RefineryRecipe addRecipe(FluidStack output, FluidStack input0, FluidStack input1, int energy)
	{
		RefineryRecipe r = new RefineryRecipe(output, input0, input1, energy);
		recipeList.add(r);
		return r;
	}

	public static RefineryRecipe findRecipe(FluidStack input0, FluidStack input1)
	{
		for(RefineryRecipe recipe : recipeList)
		{
			if(input0!=null)
			{
				if(recipe.input0!=null&&input0.containsFluid(recipe.input0))
				{
					if((recipe.input1==null&&input1==null)||(recipe.input1!=null&&input1!=null&&input1.containsFluid(recipe.input1)))
						return recipe;
				}

				if(recipe.input1!=null&&input0.containsFluid(recipe.input1))
				{
					if((recipe.input0==null&&input1==null)||(recipe.input0!=null&&input1!=null&&input1.containsFluid(recipe.input0)))
						return recipe;
				}
			}
			else if(input1!=null)
			{
				if(recipe.input0!=null&&input1.containsFluid(recipe.input0)&&recipe.input1==null)
					return recipe;
				if(recipe.input1!=null&&input1.containsFluid(recipe.input1)&&recipe.input0==null)
					return recipe;
			}
		}
		return null;
	}

	public static Optional<RefineryRecipe> findIncompleteRefineryRecipe(@Nonnull FluidStack input0, @Nonnull FluidStack input1)
	{
		if(input0.isEmpty()&&input1.isEmpty())
			return Optional.empty();
		for(RefineryRecipe recipe : recipeList)
		{
			if(!input0.isEmpty()&&input1.isEmpty())
			{
				if(input0.isFluidEqual(recipe.input0)||input0.isFluidEqual(recipe.input1))
					return Optional.of(recipe);
			}
			else if(input0.isEmpty()&&!input1.isEmpty())
			{
				if(input1.isFluidEqual(recipe.input0)||input1.isFluidEqual(recipe.input1))
					return Optional.of(recipe);
			}
			else if((input0.isFluidEqual(recipe.input0)&&input1.isFluidEqual(recipe.input1))
					||(input0.isFluidEqual(recipe.input1)&&input1.isFluidEqual(recipe.input0)))
				return Optional.of(recipe);
		}
		return Optional.empty();
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt)
	{
		nbt.put("input0", input0.writeToNBT(new CompoundNBT()));
		nbt.put("input1", input1.writeToNBT(new CompoundNBT()));
		return nbt;
	}

	public static RefineryRecipe loadFromNBT(CompoundNBT nbt)
	{
		FluidStack input0 = FluidStack.loadFluidStackFromNBT(nbt.getCompound("input0"));
		FluidStack input1 = FluidStack.loadFluidStackFromNBT(nbt.getCompound("input1"));
		return findRecipe(input0, input1);
	}
}