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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class MixerRecipeSerializer extends IERecipeSerializer<MixerRecipe>
{
	public static final MapCodec<MixerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			FluidStack.CODEC.fieldOf("result").forGetter(r -> r.fluidOutput),
			FluidTagInput.CODEC.fieldOf("fluid").forGetter(r -> r.fluidInput),
			IngredientWithSize.CODEC.listOf().fieldOf("inputs").forGetter(r -> r.itemInputs),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy)
	).apply(inst, MixerRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, MixerRecipe> STREAM_CODEC = StreamCodec.composite(
			FluidStack.STREAM_CODEC, r -> r.fluidOutput,
			FluidTagInput.STREAM_CODEC, r -> r.fluidInput,
			IngredientWithSize.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.itemInputs,
			ByteBufCodecs.INT, MultiblockRecipe::getBaseEnergy,
			MixerRecipe::new
	);

	@Override
	public MapCodec<MixerRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, MixerRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.MIXER.iconStack();
	}
}
