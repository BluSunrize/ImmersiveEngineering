/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.common.register.IEItems;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;

public class BlastFurnaceFuelSerializer extends IERecipeSerializer<BlastFurnaceFuel>
{
	public static final Codec<BlastFurnaceFuel> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Ingredient.CODEC.fieldOf("input").forGetter(f -> f.input),
			Codec.INT.fieldOf("time").forGetter(f -> f.burnTime)
	).apply(inst, BlastFurnaceFuel::new));

	@Override
	public Codec<BlastFurnaceFuel> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(IEItems.Ingredients.COAL_COKE);
	}

	@Nullable
	@Override
	public BlastFurnaceFuel fromNetwork(FriendlyByteBuf buffer)
	{
		Ingredient input = Ingredient.fromNetwork(buffer);
		int time = buffer.readInt();
		return new BlastFurnaceFuel(input, time);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, BlastFurnaceFuel recipe)
	{
		recipe.input.toNetwork(buffer);
		buffer.writeInt(recipe.burnTime);
	}
}
