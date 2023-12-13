/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.common.network.PacketUtils;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.List;

public class CrusherRecipeSerializer extends IERecipeSerializer<CrusherRecipe>
{
	public static final Codec<CrusherRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getTotalProcessEnergy),
			ExtraCodecs.strictOptionalField(CHANCE_LIST, "secondaries", List.of()).forGetter(r -> r.secondaryOutputs)
	).apply(inst, CrusherRecipe::new));

	@Override
	public Codec<CrusherRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.CRUSHER.iconStack();
	}

	@Nullable
	@Override
	public CrusherRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		ItemStack output = buffer.readItem();
		Ingredient input = Ingredient.fromNetwork(buffer);
		int energy = buffer.readInt();
		List<StackWithChance> secondaries = PacketUtils.readList(buffer, StackWithChance::read);
		return new CrusherRecipe(new TagOutput(output), input, energy, secondaries);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, CrusherRecipe recipe)
	{
		buffer.writeItem(recipe.output.get());
		recipe.input.toNetwork(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
		PacketUtils.writeList(buffer, recipe.secondaryOutputs, StackWithChance::write);
	}
}
