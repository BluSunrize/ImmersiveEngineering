/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import com.mojang.serialization.Codec;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public record SimpleRecipeSerializer<R extends Recipe<?>>(Supplier<R> create) implements RecipeSerializer<R>
{
	@Override
	public Codec<R> codec()
	{
		return Codec.unit(create);
	}

	@Override
	public @Nullable R fromNetwork(FriendlyByteBuf pBuffer)
	{
		return create.get();
	}

	@Override
	public void toNetwork(FriendlyByteBuf pBuffer, R pRecipe)
	{
	}
}
