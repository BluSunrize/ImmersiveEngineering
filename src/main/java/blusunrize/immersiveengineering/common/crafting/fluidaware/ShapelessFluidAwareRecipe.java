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
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.RecipeMatcher;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShapelessFluidAwareRecipe extends AbstractFluidAwareRecipe<MatchLocation>
{
	public ShapelessFluidAwareRecipe(
			ResourceLocation idIn, String groupIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn
	)
	{
		super(idIn, groupIn, recipeItemsIn, recipeOutputIn);
	}

	public ShapelessFluidAwareRecipe(ShapelessRecipe in)
	{
		this(in.getId(), in.getGroup(), in.getIngredients(), in.getRecipeOutput());
	}

	@Nullable
	@Override
	protected MatchLocation findMatch(CraftingInventory inv)
	{
		List<ItemStack> inputs = new ArrayList<>();
		int[] slotToInput = new int[inv.getSizeInventory()];
		Arrays.fill(slotToInput, -1);

		for(int i = 0; i < inv.getSizeInventory(); ++i)
		{
			ItemStack itemstack = inv.getStackInSlot(i);
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
		int[][] slotToIngredient = new int[inv.getWidth()][inv.getHeight()];
		for(int x = 0; x < inv.getWidth(); ++x)
			for(int y = 0; y < inv.getHeight(); ++y)
			{
				int inputId = slotToInput[x+inv.getWidth()*y];
				if(inputId >= 0)
					slotToIngredient[x][y] = matchMap[inputId];
				else
					slotToIngredient[x][y] = -1;
			}
		return new MatchLocation(slotToIngredient);
	}

	public ShapelessRecipe toVanilla()
	{
		return new ShapelessRecipe(getId(), getGroup(), getRecipeOutput(), getIngredients());
	}

	@Override
	public boolean canFit(int width, int height)
	{
		return width*height >= getIngredients().size();
	}

	@Nonnull
	@Override
	public IRecipeSerializer<?> getSerializer()
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
