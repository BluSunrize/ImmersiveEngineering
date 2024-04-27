/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.CokeOvenRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class CokeOvenRecipeSerializer extends IERecipeSerializer<CokeOvenRecipe>
{
	public static final MapCodec<CokeOvenRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.time),
			Codec.INT.fieldOf("creosote").forGetter(r -> r.creosoteOutput)
	).apply(inst, CokeOvenRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, CokeOvenRecipe> STREAM_CODEC = StreamCodec.composite(
			TagOutput.STREAM_CODEC, r -> r.output,
			IngredientWithSize.STREAM_CODEC, r -> r.input,
			ByteBufCodecs.INT, r -> r.time,
			ByteBufCodecs.INT, r -> r.creosoteOutput,
			CokeOvenRecipe::new
	);

	@Override
	public MapCodec<CokeOvenRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, CokeOvenRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.COKE_OVEN.iconStack();
	}
}
