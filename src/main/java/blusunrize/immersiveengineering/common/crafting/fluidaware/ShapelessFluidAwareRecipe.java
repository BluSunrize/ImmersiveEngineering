/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.common.crafting.fluidaware.ShapelessFluidAwareRecipe.MatchLocation;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.neoforged.neoforge.common.util.RecipeMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShapelessFluidAwareRecipe extends AbstractFluidAwareRecipe<MatchLocation>
{
	public ShapelessFluidAwareRecipe(
			String groupIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn
	)
	{
		super(groupIn, recipeItemsIn, recipeOutputIn);
	}

	public ShapelessFluidAwareRecipe(ShapelessRecipe in)
	{
		this(in.getGroup(), in.getIngredients(), in.getResultItem(null));
	}

	@Nullable
	@Override
	public MatchLocation findMatch(CraftingInput inv)
	{
		List<ItemStack> inputs = new ArrayList<>();
		int[] slotToInput = new int[inv.size()];
		Arrays.fill(slotToInput, -1);

		for(int i = 0; i < inv.size(); ++i)
		{
			ItemStack itemstack = inv.getItem(i);
			if(!itemstack.isEmpty())
			{
				slotToInput[i] = inputs.size();
				inputs.add(itemstack);
			}
		}

		if(inputs.size()!=getIngredients().size())
			return null;
		//inventory[i] = inputs[slotToInput[i]] = ingredients[matchMap[slotToInput[i]]]
		int[] matchMap = RecipeMatcher.findMatches(inputs, getIngredients());
		if(matchMap==null)
			return null;
		int[][] slotToIngredient = new int[inv.width()][inv.height()];
		for(int x = 0; x < inv.width(); ++x)
			for(int y = 0; y < inv.height(); ++y)
			{
				int inputId = slotToInput[x+inv.width()*y];
				if(inputId >= 0)
					slotToIngredient[x][y] = matchMap[inputId];
				else
					slotToIngredient[x][y] = -1;
			}
		return new MatchLocation(slotToIngredient);
	}

	public ShapelessRecipe toVanilla()
	{
		return new ShapelessRecipe(getGroup(), category(), getResultItem(null), getIngredients());
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return width*height >= getIngredients().size();
	}

	@Nonnull
	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.IE_SHAPELESS_SERIALIZER.get();
	}

	public static class MatchLocation implements AbstractFluidAwareRecipe.IMatchLocation
	{
		private final int[][] map;

		public MatchLocation(int[][] map)
		{
			this.map = map;
		}

		@Override
		public int getListIndex(int x, int y)
		{
			return map[x][y];
		}
	}
}
