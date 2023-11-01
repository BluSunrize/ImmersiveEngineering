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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class TurnAndCopyRecipeSerializer implements RecipeSerializer<TurnAndCopyRecipe>
{
	public static final Codec<TurnAndCopyRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			RecipeSerializer.SHAPED_RECIPE.codec().fieldOf("base").forGetter(AbstractShapedRecipe::toVanilla),
			Codec.INT.listOf().optionalFieldOf("copyNBT").forGetter(r -> Optional.ofNullable(r.getCopyTargets())),
			Codec.BOOL.optionalFieldOf("quarter_turn", false).forGetter(TurnAndCopyRecipe::isQuarterTurn),
			Codec.BOOL.optionalFieldOf("eight_turn", false).forGetter(TurnAndCopyRecipe::isEightTurn),
			Codec.STRING.optionalFieldOf("copy_nbt_predicate").forGetter(r -> Optional.ofNullable(r.getBufferPredicate()))
	).apply(inst, (vanilla, copySlots, quarter, eights, predicate) -> {
		TurnAndCopyRecipe result = new TurnAndCopyRecipe(vanilla, copySlots.orElse(null), CraftingBookCategory.MISC);
		if(quarter)
			result.allowQuarterTurn();
		if(eights)
			result.allowEighthTurn();
		if(predicate.isPresent())
			result.setNBTCopyPredicate(predicate.get());
		return result;
	}));

	@Override
	public Codec<TurnAndCopyRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public TurnAndCopyRecipe fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		ShapedRecipe basic = RecipeSerializer.SHAPED_RECIPE.fromNetwork(buffer);
		List<Integer> copySlots = buffer.readList(FriendlyByteBuf::readVarInt);
		TurnAndCopyRecipe recipe = new TurnAndCopyRecipe(basic, copySlots, CraftingBookCategory.MISC);
		if(buffer.readBoolean())
			recipe.setNBTCopyPredicate(buffer.readUtf(512));
		if(buffer.readBoolean())
			recipe.allowQuarterTurn();
		if(buffer.readBoolean())
			recipe.allowEighthTurn();
		return recipe;
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull TurnAndCopyRecipe recipe)
	{
		RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe.toVanilla());
		buffer.writeCollection(
				Objects.requireNonNullElse(recipe.getCopyTargets(), List.of()),
				FriendlyByteBuf::writeVarInt
		);
		if(recipe.hasCopyPredicate())
		{
			buffer.writeBoolean(true);
			buffer.writeUtf(recipe.getBufferPredicate());
		}
		else
			buffer.writeBoolean(false);
		buffer.writeBoolean(recipe.isQuarterTurn());
		buffer.writeBoolean(recipe.isEightTurn());
	}
}