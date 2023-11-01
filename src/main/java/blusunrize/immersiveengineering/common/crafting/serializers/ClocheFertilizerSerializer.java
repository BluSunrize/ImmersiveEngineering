/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;

public class ClocheFertilizerSerializer extends IERecipeSerializer<ClocheFertilizer>
{
	public static final Codec<ClocheFertilizer> CODEC = RecordCodecBuilder.create(inst -> inst.group(
					Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
					Codec.FLOAT.fieldOf("growthModifier").forGetter(r -> r.growthModifier)
			).apply(inst, ClocheFertilizer::new)
	);

	@Override
	public Codec<ClocheFertilizer> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Items.BONE_MEAL);
	}

	@Nullable
	@Override
	public ClocheFertilizer fromNetwork(FriendlyByteBuf buffer)
	{
		Ingredient input = Ingredient.fromNetwork(buffer);
		float growthModifier = buffer.readFloat();
		return new ClocheFertilizer(input, growthModifier);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, ClocheFertilizer recipe)
	{
		recipe.input.toNetwork(buffer);
		buffer.writeFloat(recipe.growthModifier);
	}
}
