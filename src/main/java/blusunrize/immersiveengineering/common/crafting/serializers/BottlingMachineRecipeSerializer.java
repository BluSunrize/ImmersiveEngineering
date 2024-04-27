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
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class BottlingMachineRecipeSerializer extends IERecipeSerializer<BottlingMachineRecipe>
{
	public static final MapCodec<BottlingMachineRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutputList.CODEC.fieldOf("results").forGetter(r -> r.output),
			listOrSingle(IngredientWithSize.CODEC, "input", "inputs").forGetter(r -> r.inputs),
			FluidTagInput.CODEC.fieldOf("fluid").forGetter(r -> r.fluidInput)
	).apply(inst, BottlingMachineRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BottlingMachineRecipe> STREAM_CODEC = StreamCodec.composite(
			TagOutputList.STREAM_CODEC, r -> r.output,
			IngredientWithSize.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.inputs,
			FluidTagInput.STREAM_CODEC, r -> r.fluidInput,
			BottlingMachineRecipe::new
	);

	@Override
	public MapCodec<BottlingMachineRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, BottlingMachineRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.BOTTLING_MACHINE.iconStack();
	}
}
