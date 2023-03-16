/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.lib.manual.utils;

import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.PositionedItemStack;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

public class ManualRecipeRef
{
	private final ItemStack output;
	private final PositionedItemStack[] layout;
	private final ResourceLocation recipeName;

	public ManualRecipeRef(ItemStack output)
	{
		this.output = Objects.requireNonNull(output);
		this.layout = null;
		this.recipeName = null;
	}

	public ManualRecipeRef(PositionedItemStack[] layout)
	{
		this.output = null;
		this.layout = Objects.requireNonNull(layout);
		this.recipeName = null;
	}

	public ManualRecipeRef(ResourceLocation recipeName)
	{
		this.output = null;
		this.layout = null;
		this.recipeName = Objects.requireNonNull(recipeName);
	}

	public boolean isLayout()
	{
		return layout!=null;
	}

	public PositionedItemStack[] getLayout()
	{
		return Objects.requireNonNull(layout);
	}

	public boolean isResult()
	{
		return output!=null;
	}

	public ItemStack getResult()
	{
		return Objects.requireNonNull(output);
	}

	public boolean isRecipeName()
	{
		return recipeName!=null;
	}

	public ResourceLocation getRecipeName()
	{
		return Objects.requireNonNull(recipeName);
	}

	public <C extends Container, R extends Recipe<C>>
	void forEachMatchingRecipe(RecipeType<R> type, Consumer<R> out)
	{
		RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
		forEachMatchingRecipe(type, PrivateAccess.getRecipes(recipeManager, type).values(), out);
	}

	public <C extends Container, R extends Recipe<C>>
	void forEachMatchingRecipe(RecipeType<R> type, Collection<R> allRecipes, Consumer<R> out)
	{
		if(isRecipeName())
		{
			RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
			R recipe = PrivateAccess.getRecipes(recipeManager, type).get(getRecipeName());
			if(recipe!=null&&matches(recipe))
				out.accept(recipe);
		}
		else
			for(R recipe : allRecipes)
				if(matches(recipe))
					out.accept(recipe);
	}

	public boolean matches(Recipe<?> rec)
	{
		if(isResult()&&ManualUtils.stackMatchesObject(
				rec.getResultItem(Minecraft.getInstance().level.registryAccess()), getResult()
		))
			return true;
		return isRecipeName()&&getRecipeName().equals(rec.getId());
	}
}
