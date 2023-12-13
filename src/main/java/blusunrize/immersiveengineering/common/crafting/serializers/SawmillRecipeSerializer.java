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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class SawmillRecipeSerializer extends IERecipeSerializer<SawmillRecipe>
{
	public static final Codec<SawmillRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			optionalItemOutput("stripped").forGetter(r -> r.stripped),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getTotalProcessEnergy),
			ExtraCodecs.strictOptionalField(TagOutputList.CODEC, "strippingSecondaries", TagOutputList.EMPTY).forGetter(r -> r.secondaryStripping),
			ExtraCodecs.strictOptionalField(TagOutputList.CODEC, "secondaryOutputs", TagOutputList.EMPTY).forGetter(r -> r.secondaryOutputs)
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
		TagOutput output = readLazyStack(buffer);
		TagOutput stripped = readLazyStack(buffer);
		Ingredient input = Ingredient.fromNetwork(buffer);
		int energy = buffer.readInt();
		int secondaryCount = buffer.readInt();
		List<TagOutput> strippingOutputs = new ArrayList<>();
		for(int i = 0; i < secondaryCount; i++)
			strippingOutputs.add(readLazyStack(buffer));
		List<TagOutput> secondaryOutputs = new ArrayList<>();
		secondaryCount = buffer.readInt();
		for(int i = 0; i < secondaryCount; i++)
			secondaryOutputs.add(readLazyStack(buffer));
		return new SawmillRecipe(
				output, stripped, input, energy, new TagOutputList(secondaryOutputs), new TagOutputList(strippingOutputs)
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
