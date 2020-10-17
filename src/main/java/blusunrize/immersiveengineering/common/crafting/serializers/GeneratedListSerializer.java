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
import blusunrize.immersiveengineering.api.utils.IEPacketBuffer;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import blusunrize.immersiveengineering.common.items.IEItems.Misc;
import blusunrize.immersiveengineering.common.util.RecipeSerializers;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GeneratedListSerializer extends IERecipeSerializer<GeneratedListRecipe>
{
	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Misc.wireCoils.get(WireType.COPPER));
	}

	@Override
	public GeneratedListRecipe readFromJson(ResourceLocation recipeId, JsonObject json)
	{
		return new GeneratedListRecipe(recipeId);
	}

	@Nullable
	@Override
	public GeneratedListRecipe read(@Nonnull ResourceLocation recipeId, @Nonnull IEPacketBuffer buffer)
	{
		int length = buffer.readVarInt();
		List<IESerializableRecipe> subRecipes = new ArrayList<>(length);
		ResourceLocation recipeCategory = buffer.readResourceLocation();
		IRecipeSerializer<?> deserializer = RecipeSerializers.RECIPE_SERIALIZERS.getEntries()
				.stream()
				.map(RegistryObject::get)
				.filter(ser -> recipeCategory.equals(ser.getRegistryName()))
				.findAny()
				.orElseThrow(RuntimeException::new);
		for(int i = 0; i < length; ++i)
		{
			ResourceLocation recipeName = buffer.readResourceLocation();
			IRecipe<?> subRecipe = deserializer.read(recipeName, buffer);
			subRecipes.add((IESerializableRecipe)subRecipe);
		}
		return new GeneratedListRecipe(recipeId, subRecipes);
	}

	@Override
	public void write(@Nonnull IEPacketBuffer buffer, @Nonnull GeneratedListRecipe recipe)
	{
		List<? extends IESerializableRecipe> recipes = recipe.getSubRecipes();
		buffer.writeVarInt(recipes.size());
		buffer.writeResourceLocation(recipe.getSubSerializer());
		for(IESerializableRecipe r : recipes)
		{
			buffer.writeResourceLocation(r.getId());
			((IERecipeSerializer)r.getSerializer()).write(buffer, r);
		}
	}
}
