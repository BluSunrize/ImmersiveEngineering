/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IESerializableRecipe;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nullable;

public class BlastFurnaceRecipeSerializer extends IERecipeSerializer<BlastFurnaceRecipe>
{
	public static final Codec<BlastFurnaceRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			LAZY_OUTPUT_CODEC.fieldOf("result").forGetter(r -> r.output),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			ExtraCodecs.strictOptionalField(Codec.INT, "time", 200).forGetter(r -> r.time),
			optionalItemOutput("slag").forGetter(r -> r.slag)
	).apply(inst, BlastFurnaceRecipe::new));

	@Override
	public Codec<BlastFurnaceRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.BLAST_FURNACE.iconStack();
	}

	@Nullable
	@Override
	public BlastFurnaceRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		Lazy<ItemStack> output = readLazyStack(buffer);
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int time = buffer.readInt();
		Lazy<ItemStack> slag = IESerializableRecipe.LAZY_EMPTY;
		if(buffer.readBoolean())
			slag = readLazyStack(buffer);
		return new BlastFurnaceRecipe(output, input, time, slag);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, BlastFurnaceRecipe recipe)
	{
		writeLazyStack(buffer, recipe.output);
		recipe.input.write(buffer);
		buffer.writeInt(recipe.time);
		buffer.writeBoolean(!recipe.slag.get().isEmpty());
		if(!recipe.slag.get().isEmpty())
			buffer.writeItem(recipe.slag.get());
	}
}
