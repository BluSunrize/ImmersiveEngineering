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

import java.util.List;

public class ArcFurnaceRecipeSerializer extends IERecipeSerializer<ArcFurnaceRecipe>
{
	public static final MapCodec<ArcFurnaceRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutputList.CODEC.fieldOf("results").forGetter(r -> r.output),
			TagOutput.CODEC.optionalFieldOf("slag", TagOutput.EMPTY).forGetter(r -> r.slag),
			CHANCE_LIST_CODEC.optionalFieldOf("secondaries", List.of()).forGetter(r -> r.secondaryOutputs),
			Codec.INT.fieldOf("time").forGetter(MultiblockRecipe::getBaseTime),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			IngredientWithSize.CODEC.listOf().fieldOf("additives").forGetter(r -> r.additives)
	).apply(inst, ArcFurnaceRecipe::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ArcFurnaceRecipe> STREAM_CODEC = StreamCodec.composite(
			Output.STREAM_CODEC, Output::new,
			ByteBufCodecs.INT, MultiblockRecipe::getBaseTime,
			ByteBufCodecs.INT, MultiblockRecipe::getBaseEnergy,
			IngredientWithSize.STREAM_CODEC, r -> r.input,
			IngredientWithSize.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.additives,
			(output, time, energy, input, add) -> new ArcFurnaceRecipe(
					output.output, output.slag, output.secondaryOutputs, time, energy, input, add
			)
	);

	@Override
	public MapCodec<ArcFurnaceRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ArcFurnaceRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.ARC_FURNACE.iconStack();
	}

	private record Output(TagOutputList output, List<StackWithChance> secondaryOutputs, TagOutput slag)
	{
		private static final StreamCodec<RegistryFriendlyByteBuf, Output> STREAM_CODEC = StreamCodec.composite(
				TagOutputList.STREAM_CODEC, r -> r.output,
				StackWithChance.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.secondaryOutputs,
				TagOutput.STREAM_CODEC, r -> r.slag,
				Output::new
		);

		private Output(ArcFurnaceRecipe r)
		{
			this(r.output, r.secondaryOutputs, r.slag);
		}
	}
}
