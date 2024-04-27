/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
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

public class BlastFurnaceRecipeSerializer extends IERecipeSerializer<BlastFurnaceRecipe>
{
	public static final MapCodec<BlastFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.time),
			optionalItemOutput("slag").forGetter(r -> r.slag)
	).apply(inst, BlastFurnaceRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BlastFurnaceRecipe> STREAM_CODEC = StreamCodec.composite(
			TagOutput.STREAM_CODEC, r -> r.output,
			IngredientWithSize.STREAM_CODEC, r -> r.input,
			ByteBufCodecs.INT, r -> r.time,
			TagOutput.STREAM_CODEC, r -> r.slag,
			BlastFurnaceRecipe::new
	);

	@Override
	public MapCodec<BlastFurnaceRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, BlastFurnaceRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.BLAST_FURNACE.iconStack();
	}
}
