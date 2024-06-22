/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.fluidaware.AbstractShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.List;

public class TurnAndCopyRecipeSerializer implements RecipeSerializer<TurnAndCopyRecipe>
{
	private record AdditionalData(List<Integer> copySlots, boolean quarter, boolean eights)
	{
		private static final MapCodec<AdditionalData> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
				Codec.INT.listOf().optionalFieldOf("copyNBT", List.of()).forGetter(AdditionalData::copySlots),
				Codec.BOOL.optionalFieldOf("quarter_turn", false).forGetter(AdditionalData::quarter),
				Codec.BOOL.optionalFieldOf("eight_turn", false).forGetter(AdditionalData::eights)
		).apply(inst, AdditionalData::new));
		private static final StreamCodec<ByteBuf, AdditionalData> STREAM_CODEC = StreamCodec.composite(
				ByteBufCodecs.INT.apply(ByteBufCodecs.list()), AdditionalData::copySlots,
				ByteBufCodecs.BOOL, AdditionalData::quarter,
				ByteBufCodecs.BOOL, AdditionalData::eights,
				AdditionalData::new
		);

		public AdditionalData(TurnAndCopyRecipe recipe)
		{
			this(recipe.getCopyTargets(), recipe.isQuarterTurn(), recipe.isEightTurn());
		}

		public TurnAndCopyRecipe apply(ShapedRecipe base)
		{
			TurnAndCopyRecipe result = new TurnAndCopyRecipe(base, copySlots());
			if(quarter())
				result.allowQuarterTurn();
			if(eights())
				result.allowEighthTurn();
			return result;
		}
	}

	public static final MapCodec<TurnAndCopyRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			AdditionalData.CODEC.forGetter(AdditionalData::new),
			RecipeSerializer.SHAPED_RECIPE.codec().forGetter(AbstractShapedRecipe::toVanilla)
	).apply(inst, AdditionalData::apply));

	public static final StreamCodec<RegistryFriendlyByteBuf, TurnAndCopyRecipe> STREAM_CODEC = StreamCodec.composite(
			AdditionalData.STREAM_CODEC, AdditionalData::new,
			RecipeSerializer.SHAPED_RECIPE.streamCodec(), AbstractShapedRecipe::toVanilla,
			AdditionalData::apply
	);

	@Override
	public MapCodec<TurnAndCopyRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, TurnAndCopyRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}
}