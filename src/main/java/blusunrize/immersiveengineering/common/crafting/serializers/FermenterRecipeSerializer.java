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

public class FermenterRecipeSerializer extends IERecipeSerializer<FermenterRecipe>
{
	public static final MapCodec<FermenterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			optionalFluidOutput("fluid").forGetter(r -> r.fluidOutput),
			optionalItemOutput("result").forGetter(r -> r.itemOutput),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy)
	).apply(inst, FermenterRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, FermenterRecipe> STREAM_CODEC = StreamCodec.composite(
			FluidStack.STREAM_CODEC, r -> r.fluidOutput,
			TagOutput.STREAM_CODEC, r -> r.itemOutput,
			IngredientWithSize.STREAM_CODEC, r -> r.input,
			ByteBufCodecs.INT, MultiblockRecipe::getBaseEnergy,
			FermenterRecipe::new
	);

	@Override
	public MapCodec<FermenterRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, FermenterRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.FERMENTER.iconStack();
	}
}
