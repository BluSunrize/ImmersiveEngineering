/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.crafting.DamageToolRecipe;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipe;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ForgeRegistryEntry;

import javax.annotation.Nonnull;

public class DamageToolRecipeSerializer extends ForgeRegistryEntry<IRecipeSerializer<?>> implements IRecipeSerializer<DamageToolRecipe>
{
	public static final IRecipeSerializer<DamageToolRecipe> INSTANCE = IRecipeSerializer.register(
			ImmersiveEngineering.MODID+":damage_tool", new DamageToolRecipeSerializer()
	);

	@Nonnull
	@Override
	public DamageToolRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull JsonObject json)
	{
		NonNullList<Ingredient> defIngredients = readIngredients(json.getAsJsonArray("ingredients"));
		Ingredient tool = Ingredient.deserialize(json.get("tool"));
		String group = json.get("group").getAsString();
		ItemStack result = ShapedRecipe.deserializeItem(json.getAsJsonObject("result"));
		return new DamageToolRecipe(recipeId, group, result, tool, defIngredients);
	}

	@Nonnull
	@Override
	public DamageToolRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		int stdCount = buffer.readInt();
		NonNullList<Ingredient> stdIngr = NonNullList.create();
		for(int i = 0; i < stdCount; ++i)
			stdIngr.add(Ingredient.read(buffer));
		Ingredient tool = Ingredient.read(buffer);
		String group = buffer.readString(512);
		ItemStack output = buffer.readItemStack();
		return new DamageToolRecipe(recipeId, group, output, tool, stdIngr);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull DamageToolRecipe recipe)
	{
		int standardCount = recipe.getIngredients().size()-1;
		buffer.writeInt(standardCount);
		for(int i = 0; i < standardCount; ++i)
			CraftingHelper.write(buffer, recipe.getIngredients().get(i));
		CraftingHelper.write(buffer, recipe.getTool());
		buffer.writeString(recipe.getGroup());
		buffer.writeItemStack(recipe.getRecipeOutput());
	}

	private static NonNullList<Ingredient> readIngredients(JsonArray all)
	{
		NonNullList<Ingredient> ret = NonNullList.create();

		for(int i = 0; i < all.size(); ++i)
		{
			Ingredient ingredient = Ingredient.deserialize(all.get(i));
			if(!ingredient.hasNoMatchingItems())
				ret.add(ingredient);
		}

		return ret;
	}
}