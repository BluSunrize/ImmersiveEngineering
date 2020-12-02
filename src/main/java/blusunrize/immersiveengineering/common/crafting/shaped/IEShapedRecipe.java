/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.shaped;

import blusunrize.immersiveengineering.common.crafting.IngredientFluidStack;
import blusunrize.immersiveengineering.common.crafting.shaped.IEShapedRecipe.IMatchLocation;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.common.base.Preconditions;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class IEShapedRecipe<MatchLocation extends IMatchLocation> implements ICraftingRecipe
{
	protected final static boolean[] BOOLEANS = {true, false};

	private final int recipeWidth;
	private final int recipeHeight;
	private final NonNullList<Ingredient> recipeItems;
	private final ItemStack recipeOutput;
	private final ResourceLocation id;
	private final String group;

	public IEShapedRecipe(
			ResourceLocation idIn, String groupIn, int recipeWidthIn, int recipeHeightIn,
			NonNullList<Ingredient> recipeItemsIn, ItemStack recipeOutputIn
	)
	{
		this.id = idIn;
		this.group = groupIn;
		this.recipeWidth = recipeWidthIn;
		this.recipeHeight = recipeHeightIn;
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
	public IRecipeSerializer<?> getSerializer()
	{
		return RecipeSerializers.IE_SHAPED_SERIALIZER.get();
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
	public boolean canFit(int width, int height)
	{
		return width >= this.recipeWidth&&height >= this.recipeHeight;
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

	public int getWidth()
	{
		return this.recipeWidth;
	}

	public int getHeight()
	{
		return this.recipeHeight;
	}

	@Nonnull
	@Override
	public NonNullList<ItemStack> getRemainingItems(@Nonnull CraftingInventory inv)
	{
		NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
		final MatchLocation offset = findMatch(inv);
		Preconditions.checkNotNull(offset);

		for(int x = 0; x < inv.getWidth(); ++x)
			for(int y = 0; y < inv.getHeight(); ++y)
			{
				final int invIndex = getInventoryIndex(inv, x, y);
				final int ingrIndex = offset.getListIndex(x, y, getWidth(), getHeight());
				if(ingrIndex >= 0)
				{
					Ingredient ingr = getIngredients().get(ingrIndex);
					final ItemStack item = inv.getStackInSlot(invIndex);
					ItemStack result = ItemStack.EMPTY;
					if(ingr instanceof IngredientFluidStack)
						result = ((IngredientFluidStack)ingr).getExtractedStack(item);
					else if(item.hasContainerItem())
						result = item.getContainerItem();
					remaining.set(invIndex, result);
				}
			}

		return remaining;
	}

	private int getInventoryIndex(CraftingInventory inv, int x, int y)
	{
		return x+y*inv.getWidth();
	}

	public ShapedRecipe toVanilla()
	{
		return new ShapedRecipe(getId(), getGroup(), getWidth(), getHeight(), getIngredients(), getRecipeOutput());
	}

	public interface IMatchLocation
	{
		int getListIndex(int x, int y, int width, int height);
	}
}
