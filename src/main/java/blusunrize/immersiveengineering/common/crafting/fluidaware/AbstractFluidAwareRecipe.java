/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.common.crafting.fluidaware.AbstractFluidAwareRecipe.IMatchLocation;
import blusunrize.immersiveengineering.common.util.IELogger;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractFluidAwareRecipe<MatchLocation extends IMatchLocation> implements ICraftingRecipe
{
	protected final static boolean[] BOOLEANS = {true, false};

	private final NonNullList<Ingredient> recipeItems;
	private final ItemStack recipeOutput;
	private final ResourceLocation id;
	private final String group;

	public AbstractFluidAwareRecipe(
			ResourceLocation idIn, String groupIn, NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn
	)
	{
		this.id = idIn;
		this.group = groupIn;
		this.recipeItems = recipeItemsIn;
		this.recipeOutput = recipeOutputIn;
	}

	@Nonnull
	@Override
	public ResourceLocation getId()
	{
		return this.id;
	}

	@Nonnull
	@Override
	public String getGroup()
	{
		return this.group;
	}

	@Nonnull
	@Override
	public ItemStack getRecipeOutput()
	{
		return this.recipeOutput;
	}

	@Nonnull
	@Override
	public NonNullList<Ingredient> getIngredients()
	{
		return this.recipeItems;
	}

	@Override
	public boolean matches(@Nonnull CraftingInventory inv, @Nonnull World worldIn)
	{
		return findMatch(inv)!=null;
	}

	@Nullable
	protected abstract MatchLocation findMatch(CraftingInventory inv);

	@Nonnull
	@Override
	public ItemStack getCraftingResult(@Nonnull CraftingInventory inv)
	{
		return this.getRecipeOutput().copy();
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInventory inv)
	{
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		final MatchLocation offset = findMatch(inv);
		if(offset==null)
		{
			IELogger.logger.error("IRecipe#getRemainingItems was called with an inventory that does not match the recipe");
			IELogger.logger.error("according to IRecipe#matches. This is probably a bug in some mod in the following stacktrace,");
			IELogger.logger.error("if in doubt report it to Immersive Engineering", new IllegalArgumentException());
			return ICraftingRecipe.super.getRemainingItems(inv);
		}

		for(int x = 0; x < inv.getWidth(); ++x)
			for(int y = 0; y < inv.getHeight(); ++y)
			{
				final int invIndex = getInventoryIndex(inv, x, y);
				final int ingrIndex = offset.getListIndex(x, y);
				if(ingrIndex >= 0&&ingrIndex < getIngredients().size())
				{
					Ingredient ingr = getIngredients().get(ingrIndex);
					final ItemStack item = inv.getStackInSlot(invIndex);
					ItemStack result = ItemStack.EMPTY;
					if(ingr instanceof IngredientFluidStack)
						result = ((IngredientFluidStack)ingr).getExtractedStack(item.copy());
					else if(item.hasContainerItem())
						result = item.getContainerItem();
					if(result==item)
						result = result.copy();
					remaining.set(invIndex, result);
				}
			}

		return remaining;
	}

	private int getInventoryIndex(CraftingInventory inv, int x, int y)
	{
		return x+y*inv.getWidth();
	}

	public interface IMatchLocation
	{
		int getListIndex(int x, int y);
	}
}
