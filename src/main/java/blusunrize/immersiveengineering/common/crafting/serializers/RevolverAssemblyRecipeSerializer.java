/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.RevolverAssemblyRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.AbstractShapedRecipe;
import blusunrize.immersiveengineering.common.crafting.fluidaware.TurnAndCopyRecipe;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

import javax.annotation.Nonnull;
import java.util.List;

public class RevolverAssemblyRecipeSerializer implements RecipeSerializer<RevolverAssemblyRecipe>
{
	public static final Codec<RevolverAssemblyRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			RecipeSerializer.SHAPED_RECIPE.codec().fieldOf("base").forGetter(AbstractShapedRecipe::toVanilla),
			Codec.INT.listOf().optionalFieldOf("copyNBT", List.of()).forGetter(TurnAndCopyRecipe::getCopyTargets)
	).apply(inst, RevolverAssemblyRecipe::new));

	@Override
	public Codec<RevolverAssemblyRecipe> codec()
	{
		return CODEC;
	}

	@Nonnull
	@Override
	public RevolverAssemblyRecipe fromNetwork(@Nonnull FriendlyByteBuf buffer)
	{
		ShapedRecipe basic = RecipeSerializer.SHAPED_RECIPE.fromNetwork(buffer);
		List<Integer> copySlots = buffer.readList(FriendlyByteBuf::readVarInt);
		return new RevolverAssemblyRecipe(basic, copySlots);
	}

	@Override
	public void toNetwork(@Nonnull FriendlyByteBuf buffer, @Nonnull RevolverAssemblyRecipe recipe)
	{
		RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe.toVanilla());
		buffer.writeCollection(recipe.getCopyTargets(), FriendlyByteBuf::writeVarInt);
	}
}