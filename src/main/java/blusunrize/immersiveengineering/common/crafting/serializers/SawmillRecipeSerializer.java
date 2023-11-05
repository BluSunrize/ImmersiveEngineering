/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.SawmillRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nullable;

public class SawmillRecipeSerializer extends IERecipeSerializer<SawmillRecipe>
{
	public static final Codec<SawmillRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			LAZY_OUTPUT_CODEC.fieldOf("result").forGetter(r -> r.output),
			optionalItemOutput("stripped").forGetter(r -> r.stripped),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getTotalProcessEnergy),
			LAZY_OUTPUTS_CODEC.optionalFieldOf("strippingSecondaries", EMPTY_LAZY_OUTPUTS).forGetter(r -> r.secondaryStripping),
			LAZY_OUTPUTS_CODEC.optionalFieldOf("secondaryOutputs", EMPTY_LAZY_OUTPUTS).forGetter(r -> r.secondaryOutputs)
	).apply(inst, SawmillRecipe::new));

	@Override
	public Codec<SawmillRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.SAWMILL.iconStack();
	}

	@Nullable
	@Override
	public SawmillRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		Lazy<ItemStack> output = readLazyStack(buffer);
		Lazy<ItemStack> stripped = readLazyStack(buffer);
		Ingredient input = Ingredient.fromNetwork(buffer);
		int energy = buffer.readInt();
		int secondaryCount = buffer.readInt();
		NonNullList<ItemStack> strippingOutputs = NonNullList.create();
		for(int i = 0; i < secondaryCount; i++)
			strippingOutputs.add(buffer.readItem());
		NonNullList<ItemStack> secondaryOutputs = NonNullList.create();
		secondaryCount = buffer.readInt();
		for(int i = 0; i < secondaryCount; i++)
			secondaryOutputs.add(buffer.readItem());
		return new SawmillRecipe(
				output, stripped, input, energy, Lazy.of(() -> secondaryOutputs), Lazy.of(() -> strippingOutputs)
		);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, SawmillRecipe recipe)
	{
		writeLazyStack(buffer, recipe.output);
		buffer.writeItem(recipe.stripped.get());
		recipe.input.toNetwork(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
		buffer.writeInt(recipe.secondaryStripping.get().size());
		for(ItemStack secondaryOutput : recipe.secondaryStripping.get())
			buffer.writeItem(secondaryOutput);
		buffer.writeInt(recipe.secondaryOutputs.get().size());
		for(ItemStack secondaryOutput : recipe.secondaryOutputs.get())
			buffer.writeItem(secondaryOutput);
	}
}
