/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.crafting.fluidaware.AbstractShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import java.util.List;

public class TurnAndCopyRecipeSerializer implements RecipeSerializer<TurnAndCopyRecipe>
{
	private record AdditionalData(List<Integer> copySlots, boolean quarter, boolean eights)
	{
		private static final DualMapCodec<ByteBuf, AdditionalData> CODECS = DualMapCodec.composite(
				DualCodecs.INT.listOf().optionalFieldOf("copyNBT", List.of()), AdditionalData::copySlots,
				DualCodecs.BOOL.optionalFieldOf("quarter_turn", false), AdditionalData::quarter,
				DualCodecs.BOOL.optionalFieldOf("eight_turn", false), AdditionalData::eights,
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

	public static final DualMapCodec<RegistryFriendlyByteBuf, TurnAndCopyRecipe> CODECS = DualMapCodec.composite(
			AdditionalData.CODECS, AdditionalData::new,
			new DualMapCodec<>(RecipeSerializer.SHAPED_RECIPE.codec(), RecipeSerializer.SHAPED_RECIPE.streamCodec()), AbstractShapedRecipe::toVanilla,
			AdditionalData::apply
	);

	@Override
	public MapCodec<TurnAndCopyRecipe> codec()
	{
		return CODECS.mapCodec();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, TurnAndCopyRecipe> streamCodec()
	{
		return CODECS.streamCodec();
	}
}