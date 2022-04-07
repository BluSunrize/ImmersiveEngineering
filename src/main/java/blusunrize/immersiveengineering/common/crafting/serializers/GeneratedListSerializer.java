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
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GeneratedListSerializer extends IERecipeSerializer<GeneratedListRecipe<?, ?>>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Misc.WIRE_COILS.get(WireType.COPPER));
	}

	@Override
	public GeneratedListRecipe<?, ?> readFromJson(ResourceLocation recipeId, JsonObject json, IContext context)
	{
		return GeneratedListRecipe.from(recipeId);
	}

	@Nullable
	@Override
	public GeneratedListRecipe<?, ?> fromNetwork(@Nonnull ResourceLocation recipeId, @Nonnull FriendlyByteBuf buffer)
	{
		int length = buffer.readVarInt();
		List<IESerializableRecipe> subRecipes = new ArrayList<>(length);
		ResourceLocation recipeCategory = buffer.readResourceLocation();
		RecipeSerializer<?> deserializer = RecipeSerializers.RECIPE_SERIALIZERS.getEntries()
				.stream()
				.map(RegistryObject::get)
				.filter(ser -> recipeCategory.equals(ser.getRegistryName()))
				.findAny()
				.orElseThrow(RuntimeException::new);
		for(int i = 0; i < length; ++i)
		{
			ResourceLocation recipeName = buffer.readResourceLocation();
			Recipe<?> subRecipe = deserializer.fromNetwork(recipeName, buffer);
			subRecipes.add((IESerializableRecipe)subRecipe);
		}
		return GeneratedListRecipe.resolved(recipeId, subRecipes);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull GeneratedListRecipe<?, ?> recipe)
	{
		List<? extends IESerializableRecipe> recipes = recipe.getSubRecipes();
		buffer.writeVarInt(recipes.size());
		buffer.writeResourceLocation(recipe.getSubSerializer());
		for(IESerializableRecipe r : recipes)
		{
			buffer.writeResourceLocation(r.getId());
			((IERecipeSerializer)r.getSerializer()).toNetwork(buffer, r);
		}
	}
}
