/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
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

public class AlloyRecipeSerializer extends IERecipeSerializer<AlloyRecipe>
{
	private static final MapCodec<AlloyRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			IngredientWithSize.CODEC.fieldOf("input0").forGetter(r -> r.input0),
			IngredientWithSize.CODEC.fieldOf("input1").forGetter(r -> r.input1),
			Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.time)
	).apply(inst, AlloyRecipe::new));

	private static final StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> STREAM_CODEC = StreamCodec.composite(
			TagOutput.STREAM_CODEC, r -> r.output,
			IngredientWithSize.STREAM_CODEC, r -> r.input0,
			IngredientWithSize.STREAM_CODEC, r -> r.input1,
			ByteBufCodecs.INT, r -> r.time,
			AlloyRecipe::new
	);

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.ALLOY_SMELTER.iconStack();
	}

	@Override
	public MapCodec<AlloyRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}
