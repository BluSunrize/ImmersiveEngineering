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
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class GeneratedListSerializer extends IERecipeSerializer<GeneratedListRecipe<?, ?>>
{
	public static final MapCodec<GeneratedListRecipe<?, ?>> CODEC = ResourceLocation.CODEC.fieldOf("generatorID").xmap(
			GeneratedListRecipe::from, GeneratedListRecipe::getGeneratorID
	);
	public static final StreamCodec<RegistryFriendlyByteBuf, GeneratedListRecipe<?, ?>> STREAM_CODEC = ResourceLocation.STREAM_CODEC
			.<GeneratedListRecipe<?, ?>>map(GeneratedListRecipe::from, GeneratedListRecipe::getGeneratorID)
			.cast();

	@Override
	public MapCodec<GeneratedListRecipe<?, ?>> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, GeneratedListRecipe<?, ?>> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Misc.WIRE_COILS.get(WireType.COPPER));
	}
}
