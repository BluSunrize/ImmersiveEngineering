/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.data.recipes;

import blusunrize.immersiveengineering.mixin.accessors.ShapedRecipeAccess;
import net.minecraft.advancements.Advancement.Builder;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.neoforged.neoforge.common.conditions.ICondition;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public record WrappingRecipeOutput<R extends Recipe<?>>(RecipeOutput wrapped,
														Function<R, Recipe<?>> transform) implements RecipeOutput
{
	public static WrappingRecipeOutput<ShapedRecipe> replaceShapedOutput(RecipeOutput out, ItemStack newResult)
	{
		return new WrappingRecipeOutput<>(out, recipe -> new ShapedRecipe(
				recipe.getGroup(), recipe.category(), ((ShapedRecipeAccess)recipe).getPattern(), newResult.copy(), false
		));
	}

	@Override
	public Builder advancement()
	{
		return wrapped.advancement();
	}

	@Override
	public void accept(ResourceLocation id, Recipe<?> recipe, @Nullable AdvancementHolder advancement, ICondition... conditions)
	{
		wrapped.accept(id, transform.apply((R)recipe), advancement, conditions);
	}
}
