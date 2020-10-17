/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.utils.IEPacketBuffer;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IIEBufferRecipeSerializer<R extends IRecipe<?>> extends IRecipeSerializer<R>
{
	@Nullable
	@Override
	default R read(@Nonnull ResourceLocation recipeId, @Nonnull PacketBuffer buffer)
	{
		return read(recipeId, IEPacketBuffer.wrap(buffer));
	}

	@Override
	default void write(@Nonnull PacketBuffer buffer, @Nonnull R recipe)
	{
		write(IEPacketBuffer.wrap(buffer), recipe);
	}

	R read(ResourceLocation recipeId, IEPacketBuffer buffer);

	void write(IEPacketBuffer buffer, R recipe);
}
