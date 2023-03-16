/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.crafting.LazyShapelessRecipe;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.common.util.Lazy;

import javax.annotation.Nonnull;

public class HammerCrushingRecipeSerializer extends IERecipeSerializer<LazyShapelessRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Blocks.CRAFTING_TABLE);
	}

	@Nonnull
	@Override
	public LazyShapelessRecipe readFromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json, IContext context)
	{
		Lazy<ItemStack> output = readOutput(json.get("result"));
		Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
		NonNullList<Ingredient> ingredients = NonNullList.of(Ingredient.EMPTY, input, Ingredient.of(IEItems.Tools.HAMMER));
		return new LazyShapelessRecipe(recipeId, "", output, ingredients, this);
	}

	@Nonnull
	@Override
	public LazyShapelessRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
	{
		int count = buffer.readInt();
		NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
		for(int i = 0; i < count; i++)
			ingredients.set(i, Ingredient.fromNetwork(buffer));
		Lazy<ItemStack> output = readLazyStack(buffer);
		return new LazyShapelessRecipe(recipeId, "", output, ingredients, this);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull LazyShapelessRecipe recipe)
	{
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		buffer.writeInt(ingredients.size());
		for(Ingredient ingredient : ingredients)
			CraftingHelper.write(buffer, ingredient);
		buffer.writeItem(recipe.getResultItem(null));
	}
}
