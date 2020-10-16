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
import blusunrize.immersiveengineering.common.items.IEItems;
import com.google.gson.JsonObject;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;

import javax.annotation.Nonnull;

public class HammerCrushingRecipeSerializer extends IERecipeSerializer<ShapelessRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Blocks.CRAFTING_TABLE);
	}

	@Nonnull
	@Override
	public ShapelessRecipe readFromJson(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		ItemStack output = readOutput(json.get("result"));
		Ingredient input = Ingredient.deserialize(JSONUtils.getJsonObject(json, "input"));
		NonNullList<Ingredient> ingredients = NonNullList.from(Ingredient.EMPTY, input, Ingredient.fromItems(IEItems.Tools.hammer));
		return new ShapelessRecipe(recipeId, "", output, ingredients);
	}

	@Nonnull
	@Override
	public ShapelessRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		int count = buffer.readVarInt();
		NonNullList<Ingredient> ingredients = NonNullList.withSize(count, Ingredient.EMPTY);
		for(int i = 0; i < count; i++)
			ingredients.set(i, Ingredient.read(buffer));
		ItemStack output = buffer.readItemStack();
		return new ShapelessRecipe(recipeId, "", output, ingredients);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull ShapelessRecipe recipe)
	{
		NonNullList<Ingredient> ingredients = recipe.getIngredients();
		buffer.writeVarInt(ingredients.size());
		for(Ingredient ingredient : ingredients)
			CraftingHelper.write(buffer, ingredient);
		buffer.writeItemStack(recipe.getRecipeOutput());
	}
}
