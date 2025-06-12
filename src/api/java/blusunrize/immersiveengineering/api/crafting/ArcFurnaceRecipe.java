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
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.DeferredHolder;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * @author BluSunrize - 23.03.2015
 * <br>
 * The recipe for the arc furnace
 */
public class ArcFurnaceRecipe extends MultiblockRecipe
{
	public static DeferredHolder<RecipeSerializer<?>, IERecipeSerializer<ArcFurnaceRecipe>> SERIALIZER;
	public static final SetRestrictedField<RecipeMultiplier> MULTIPLIERS = SetRestrictedField.common();

	public final IngredientWithSize input;
	public final List<IngredientWithSize> additives;
	public final TagOutputList output;
	public final List<StackWithChance> secondaryOutputs;
	@Nonnull
	public final TagOutput slag;
	@Nonnull
	public final String specialRecipeType;

	public static final CachedRecipeList<ArcFurnaceRecipe> RECIPES = new CachedRecipeList<>(IERecipeTypes.ARC_FURNACE);

	public ArcFurnaceRecipe(
			TagOutputList output, @Nonnull TagOutput slag, List<StackWithChance> secondaryOutputs,
			int time, int energy,
			IngredientWithSize input,
			List<IngredientWithSize> additives,
			@Nonnull String specialRecipeType
	)
	{
		super(output.getLazyList().get(0), IERecipeTypes.ARC_FURNACE, time, energy, MULTIPLIERS);
		this.output = output;
		this.secondaryOutputs = secondaryOutputs;
		this.input = input;
		this.slag = slag;
		this.additives = additives;
		this.specialRecipeType = specialRecipeType;

		List<IngredientWithSize> inputList = Lists.newArrayList(this.input);
		if(!this.additives.isEmpty())
			inputList.addAll(Lists.newArrayList(this.additives));
		setInputListWithSizes(inputList);
		this.outputList = this.output;
	}

	public ArcFurnaceRecipe(
			TagOutputList output, @Nonnull TagOutput slag, List<StackWithChance> secondaryOutputs,
			int time, int energy,
			IngredientWithSize input,
			List<IngredientWithSize> additives
	)
	{
		this(output, slag, secondaryOutputs, time, energy, input, additives, "");
	}

	@Override
	protected IERecipeSerializer<ArcFurnaceRecipe> getIESerializer()
	{
		return SERIALIZER.get();
	}

	@Override
	public int getMultipleProcessTicks()
	{
		return 0;
	}

	public NonNullList<ItemStack> getBaseOutputs()
	{
		return this.output.get();
	}

	public NonNullList<ItemStack> generateActualOutput(ItemStack input, NonNullList<ItemStack> additives, long seed)
	{
		Random random = new Random(seed);
		var output = this.output.get();
		int remainingIndex = output.size();
		NonNullList<ItemStack> actualOutput = NonNullList.withSize(output.size()+secondaryOutputs.size(), ItemStack.EMPTY);
		for(int i = 0; i < output.size(); ++i)
			actualOutput.set(i, output.get(i).copy());
		for(StackWithChance secondary : secondaryOutputs)
		{
			if(secondary.chance() < random.nextFloat())
				continue;
			ItemStack remaining = secondary.stack().get();
			for(ItemStack existing : actualOutput)
				if(ItemStack.isSameItemSameComponents(remaining, existing))
				{
					existing.grow(remaining.getCount());
					remaining = ItemStack.EMPTY;
					break;
				}
			if(!remaining.isEmpty())
			{
				actualOutput.set(remainingIndex, remaining);
				remainingIndex++;
			}
		}
		return actualOutput;
	}

	public boolean matches(ItemStack input, NonNullList<ItemStack> additives)
	{
		if(this.input!=null&&this.input.test(input))
		{
			int[] consumed = getConsumedAdditives(additives, false);
			return consumed!=null;
		}

		return false;
	}

	public int[] getConsumedAdditives(NonNullList<ItemStack> additives, boolean consume)
	{
		int[] consumed = new int[additives.size()];
		for(IngredientWithSize add : this.additives)
			if(add!=null)
			{
				int addAmount = add.getCount();
				Iterator<ItemStack> it = additives.iterator();
				int i = 0;
				while(it.hasNext())
				{
					ItemStack query = it.next();
					if(!query.isEmpty())
					{
						if(add.test(query))
						{
							if(query.getCount() > addAmount)
							{
								query.shrink(addAmount);
								consumed[i] = addAmount;
								addAmount = 0;
							}
							else
							{
								addAmount -= query.getCount();
								consumed[i] = query.getCount();
								query.setCount(0);
							}
						}
						if(addAmount <= 0)
							break;
					}
					i++;
				}

				if(addAmount > 0)
				{
					for(int j = 0; j < consumed.length; j++)
						additives.get(j).grow(consumed[j]);
					return null;
				}
			}
		if(!consume)
			for(int j = 0; j < consumed.length; j++)
				additives.get(j).grow(consumed[j]);
		return consumed;
	}


	public boolean isValidInput(ItemStack stack)
	{
		return this.input!=null&&this.input.test(stack);
	}

	public boolean isValidAdditive(ItemStack stack)
	{
		for(IngredientWithSize add : additives)
			if(add!=null&&add.test(stack))
				return true;
		return false;
	}

	public boolean isSpecialType(String checkFor)
	{
		return this.specialRecipeType.equals(checkFor);
	}

	public boolean isNotSpecialType()
	{
		return this.specialRecipeType.isEmpty();
	}

	public static RecipeHolder<ArcFurnaceRecipe> findRecipe(Level level, ItemStack input, NonNullList<ItemStack> additives)
	{
		for(RecipeHolder<ArcFurnaceRecipe> recipe : RECIPES.getRecipes(level))
			if(recipe.value().matches(input, additives))
				return recipe;
		return null;
	}

	public static boolean isValidRecipeInput(Level level, ItemStack stack)
	{
		for(RecipeHolder<ArcFurnaceRecipe> recipe : RECIPES.getRecipes(level))
			if(recipe.value().isValidInput(stack))
				return true;
		return false;
	}

	public static boolean isValidRecipeAdditive(Level level, ItemStack stack)
	{
		for(RecipeHolder<ArcFurnaceRecipe> recipe : RECIPES.getRecipes(level))
			if(recipe!=null&&recipe.value().isValidAdditive(stack))
				return true;
		return false;
	}
}
