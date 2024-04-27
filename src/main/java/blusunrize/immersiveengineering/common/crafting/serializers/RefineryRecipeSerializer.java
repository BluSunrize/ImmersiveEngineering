/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.Optional;

public class RefineryRecipeSerializer extends IERecipeSerializer<RefineryRecipe>
{
	public static final MapCodec<RefineryRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			FluidStack.CODEC.fieldOf("result").forGetter(r -> r.output),
			FluidTagInput.CODEC.fieldOf("input0").forGetter(r -> r.input0),
			FluidTagInput.CODEC.optionalFieldOf("input1").forGetter(r -> Optional.ofNullable(r.input1)),
			Ingredient.CODEC.optionalFieldOf("catalyst", Ingredient.EMPTY).forGetter(r -> r.catalyst),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy)
	).apply(inst, RefineryRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, RefineryRecipe> STREAM_CODEC = StreamCodec.composite(
			FluidStack.STREAM_CODEC, r -> r.output,
			FluidTagInput.STREAM_CODEC, r -> r.input0,
			ByteBufCodecs.optional(FluidTagInput.STREAM_CODEC), r -> Optional.ofNullable(r.input1),
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.catalyst,
			ByteBufCodecs.INT, MultiblockRecipe::getBaseEnergy,
			RefineryRecipe::new
	);

	@Override
	public MapCodec<RefineryRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, RefineryRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.REFINERY.iconStack();
	}
}
