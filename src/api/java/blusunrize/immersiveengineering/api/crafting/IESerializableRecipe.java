/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.crafting;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public abstract class IESerializableRecipe implements Recipe<RecipeInput>
{
	protected final TagOutput outputDummy;
	protected final RecipeType<?> type;

	protected <T extends Recipe<?>>
	IESerializableRecipe(TagOutput outputDummy, IERecipeTypes.TypeWithClass<T> type)
	{
		this.outputDummy = outputDummy;
		this.type = type.get();
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

	@Override
	public ItemStack getToastSymbol()
	{
		return getIESerializer().getIcon();
	}

	@Override
	public boolean matches(RecipeInput inv, Level worldIn)
	{
		return false;
	}

	@Override
	public ItemStack assemble(RecipeInput inv, Provider access)
	{
		return this.outputDummy.get();
	}

	@Override
	public boolean canCraftInDimensions(int width, int height)
	{
		return false;
	}

	@Override
	public RecipeSerializer<?> getSerializer()
	{
		return getIESerializer();
	}

	protected abstract IERecipeSerializer<?> getIESerializer();

	@Override
	public RecipeType<?> getType()
	{
		return this.type;
	}
}
