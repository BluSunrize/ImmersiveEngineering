/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.DamageToolRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;

public class DamageToolRecipeSerializer implements RecipeSerializer<DamageToolRecipe>
{
	@Nonnull
	@Override
	public DamageToolRecipe fromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		NonNullList<Ingredient> defIngredients = readIngredients(json.getAsJsonArray("ingredients"));
		Ingredient tool = Ingredient.fromJson(json.get("tool"));
		String group = json.get("group").getAsString();
		ItemStack result = ShapedRecipe.itemStackFromJson(json.getAsJsonObject("result"));
		return new DamageToolRecipe(recipeId, group, result, tool, defIngredients);
	}

	@Nonnull
	@Override
	public DamageToolRecipe fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
	{
		int stdCount = buffer.readInt();
		NonNullList<Ingredient> stdIngr = NonNullList.create();
		for(int i = 0; i < stdCount; ++i)
			stdIngr.add(Ingredient.fromNetwork(buffer));
		Ingredient tool = Ingredient.fromNetwork(buffer);
		String group = buffer.readUtf(512);
		ItemStack output = buffer.readItem();
		return new DamageToolRecipe(recipeId, group, output, tool, stdIngr);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull DamageToolRecipe recipe)
	{
		int standardCount = recipe.getIngredients().size()-1;
		buffer.writeInt(standardCount);
		for(int i = 0; i < standardCount; ++i)
			CraftingHelper.write(buffer, recipe.getIngredients().get(i));
		CraftingHelper.write(buffer, recipe.getTool());
		buffer.writeUtf(recipe.getGroup());
		buffer.writeItem(recipe.getResultItem(null));
	}

	private static NonNullList<Ingredient> readIngredients(JsonArray all)
	{
		NonNullList<Ingredient> ret = NonNullList.create();

		for(int i = 0; i < all.size(); ++i)
		{
			Ingredient ingredient = Ingredient.fromJson(all.get(i));
			if(!ingredient.isEmpty())
				ret.add(ingredient);
		}

		return ret;
	}
}