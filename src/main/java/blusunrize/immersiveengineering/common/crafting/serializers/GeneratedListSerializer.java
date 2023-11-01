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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GeneratedListSerializer extends IERecipeSerializer<GeneratedListRecipe<?, ?>>
{
	public static final Codec<GeneratedListRecipe<?, ?>> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ResourceLocation.CODEC.fieldOf("generatorID").forGetter(GeneratedListRecipe::getGeneratorID)
	).apply(inst, GeneratedListRecipe::from));

	@Override
	public Codec<GeneratedListRecipe<?, ?>> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Misc.WIRE_COILS.get(WireType.COPPER));
	}

	@Nullable
	@Override
	public GeneratedListRecipe<?, ?> fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		int length = buffer.readVarInt();
		List<IESerializableRecipe> subRecipes = new ArrayList<>(length);
		ResourceLocation recipeCategory = buffer.readResourceLocation();
		RecipeSerializer<?> deserializer = Objects.requireNonNull(BuiltInRegistries.RECIPE_SERIALIZER.get(recipeCategory));
		for(int i = 0; i < length; ++i)
		{
			Recipe<?> subRecipe = deserializer.fromNetwork(buffer);
			subRecipes.add((IESerializableRecipe)subRecipe);
		}
		ResourceLocation serializerID = buffer.readResourceLocation();
		return GeneratedListRecipe.resolved(serializerID, subRecipes);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull GeneratedListRecipe<?, ?> recipe)
	{
		List<? extends IESerializableRecipe> recipes = recipe.getSubRecipes();
		buffer.writeVarInt(recipes.size());
		buffer.writeResourceLocation(recipe.getSubSerializer());
		for(IESerializableRecipe r : recipes)
			((IERecipeSerializer)r.getSerializer()).toNetwork(buffer, r);
		buffer.writeResourceLocation(recipe.getGeneratorID());
	}
}
