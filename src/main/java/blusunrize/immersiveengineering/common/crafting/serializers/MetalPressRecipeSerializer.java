/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class MetalPressRecipeSerializer extends IERecipeSerializer<MetalPressRecipe>
{
	public static final MapCodec<MetalPressRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("mold").forGetter(r -> r.mold),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy)
	).apply(inst, MetalPressRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, MetalPressRecipe> STREAM_CODEC = StreamCodec.composite(
			TagOutput.STREAM_CODEC, r -> r.output,
			IngredientWithSize.STREAM_CODEC, r -> r.input,
			ByteBufCodecs.registry(Registries.ITEM), r -> r.mold,
			ByteBufCodecs.INT, MultiblockRecipe::getBaseEnergy,
			MetalPressRecipe::new
	);

	@Override
	public MapCodec<MetalPressRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, MetalPressRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.METAL_PRESS.iconStack();
	}
}
