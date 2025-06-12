/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.crafting.cache.CachedRecipeList;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.common.collect.Lists;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author BluSunrize - 02.03.2016
 * <p>
 * The recipe for the Refinery
 */
public class RefineryRecipe extends MultiblockRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<RefineryRecipe>> SERIALIZER;
	public static final CachedRecipeList<RefineryRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.REFINERY);
	public static final SetRestrictedField<RecipeMultiplier> MULTIPLIERS = SetRestrictedField.common();

	public final FluidStack output;
	public final SizedFluidIngredient input0;
	@Nullable
	public final SizedFluidIngredient input1;
	public final Ingredient catalyst;

	public RefineryRecipe(FluidStack output, SizedFluidIngredient input0, Optional<SizedFluidIngredient> input1, Ingredient catalyst, int energy)
	{
		this(output, input0, input1.orElse(null), catalyst, energy);
	}

	public RefineryRecipe(FluidStack output, SizedFluidIngredient input0, @Nullable SizedFluidIngredient input1, Ingredient catalyst, int energy)
	{
		super(TagOutput.EMPTY, IERecipeTypes.REFINERY, 1, energy, MULTIPLIERS);
		this.output = output;
		this.input0 = input0;
		this.input1 = input1;
		this.catalyst = catalyst;

		this.fluidInputList = Lists.newArrayList(this.input0);
		if(this.input1!=null)
			this.fluidInputList.add(this.input1);
		this.fluidOutputList = Lists.newArrayList(this.output);
	}

	@Override
	protected IERecipeSerializer<RefineryRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	public static RecipeHolder<RefineryRecipe> findRecipe(Level level, FluidStack input0, @Nonnull FluidStack input1, @Nonnull ItemStack catalyst)
	{
		for(RecipeHolder<RefineryRecipe> holder : RECIPES.getRecipes(level))
		{
			RefineryRecipe recipe = holder.value();
			if(!recipe.catalyst.test(catalyst))
				continue;
			if(!input0.isEmpty())
			{
				if(recipe.input0!=null&&recipe.input0.test(input0))
				{
					if((recipe.input1==null&&input1.isEmpty())||(recipe.input1!=null&&recipe.input1.test(input1)))
						return holder;
				}

				if(recipe.input1!=null&&recipe.input1.test(input0))
				{
					if((recipe.input0==null&&input1.isEmpty())||(recipe.input0!=null&&recipe.input0.test(input1)))
						return holder;
				}
			}
			else if(!input1.isEmpty())
			{
				if(recipe.input0!=null&&recipe.input0.test(input1)&&recipe.input1==null)
					return holder;
				if(recipe.input1!=null&&recipe.input1.test(input1)&&recipe.input0==null)
					return holder;
			}
		}
		return null;
	}

	public static Optional<RefineryRecipe> findIncompleteRefineryRecipe(Level level, @Nonnull FluidStack input0, @Nonnull FluidStack input1)
	{
		if(input0.isEmpty()&&input1.isEmpty())
			return Optional.empty();
		for(RecipeHolder<RefineryRecipe> holder : RECIPES.getRecipes(level))
		{
			RefineryRecipe recipe = holder.value();
			if(!input0.isEmpty()&&input1.isEmpty())
			{
				if(recipe.input0.ingredient().test(input0)||(recipe.input1!=null&&recipe.input1.ingredient().test(input0)))
					return Optional.of(recipe);
			}
			else if(input0.isEmpty()&&!input1.isEmpty())
			{
				if(recipe.input0.ingredient().test(input1)||(recipe.input1!=null&&recipe.input1.ingredient().test(input1)))
					return Optional.of(recipe);
			}
			else if((recipe.input1!=null&&recipe.input0.ingredient().test(input0)&&recipe.input1.ingredient().test(input1))
					||(recipe.input1!=null&&recipe.input1.ingredient().test(input0)&&recipe.input0.ingredient().test(input1)))
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