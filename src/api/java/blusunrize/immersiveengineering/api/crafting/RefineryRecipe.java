/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import com.google.common.collect.Lists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * @author BluSunrize - 02.03.2016
 * <p>
 * The recipe for the Refinery
 */
public class RefineryRecipe extends MultiblockRecipe
{
	public static RecipeType<RefineryRecipe> TYPE;
	public static RegistryObject<IERecipeSerializer<RefineryRecipe>> SERIALIZER;

	public final FluidStack output;
	public final FluidTagInput input0;
	public final FluidTagInput input1;
	public final Ingredient catalyst;

	public RefineryRecipe(ResourceLocation id, FluidStack output, FluidTagInput input0, FluidTagInput input1, Ingredient catalyst, int energy)
	{
		super(ItemStack.EMPTY, TYPE, id);
		this.output = output;
		this.input0 = input0;
		this.input1 = input1;
		this.catalyst = catalyst;
		setTimeAndEnergy(1, energy);

		this.fluidInputList = Lists.newArrayList(this.input0, this.input1);
		this.fluidOutputList = Lists.newArrayList(this.output);
	}

	@Override
	protected IERecipeSerializer<RefineryRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	// Initialized by reload listener
	public static Map<ResourceLocation, RefineryRecipe> recipeList = Collections.emptyMap();

	public static RefineryRecipe findRecipe(FluidStack input0, FluidStack input1)
	{
		for(RefineryRecipe recipe : recipeList.values())
		{
			if(input0!=null)
			{
				if(recipe.input0!=null&&recipe.input0.test(input0))
				{
					if((recipe.input1==null&&input1==null)||(recipe.input1!=null&&input1!=null&&recipe.input1.test(input1)))
						return recipe;
				}

				if(recipe.input1!=null&&recipe.input1.test(input0))
				{
					if((recipe.input0==null&&input1==null)||(recipe.input0!=null&&input1!=null&&recipe.input0.test(input1)))
						return recipe;
				}
			}
			else if(input1!=null)
			{
				if(recipe.input0!=null&&recipe.input0.test(input1)&&recipe.input1==null)
					return recipe;
				if(recipe.input1!=null&&recipe.input1.test(input1)&&recipe.input0==null)
					return recipe;
			}
		}
		return null;
	}

	public static Optional<RefineryRecipe> findIncompleteRefineryRecipe(@Nonnull FluidStack input0, @Nonnull FluidStack input1)
	{
		if(input0.isEmpty()&&input1.isEmpty())
			return Optional.empty();
		for(RefineryRecipe recipe : recipeList.values())
		{
			if(!input0.isEmpty()&&input1.isEmpty())
			{
				if(recipe.input0.testIgnoringAmount(input0)||recipe.input1.testIgnoringAmount(input0))
					return Optional.of(recipe);
			}
			else if(input0.isEmpty()&&!input1.isEmpty())
			{
				if(recipe.input0.testIgnoringAmount(input1)||recipe.input1.testIgnoringAmount(input1))
					return Optional.of(recipe);
			}
			else if((recipe.input0.testIgnoringAmount(input0)&&recipe.input1.testIgnoringAmount(input1))
					||(recipe.input1.testIgnoringAmount(input0)&&recipe.input0.testIgnoringAmount(input1)))
				return Optional.of(recipe);
		}
		return Optional.empty();
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}
}