/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.AlloyRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;

import javax.annotation.Nullable;

public class AlloyRecipeSerializer extends IERecipeSerializer<AlloyRecipe>
{
	private static final Codec<AlloyRecipe> CODEC = RecordCodecBuilder.create(
			inst -> inst.group(
					LAZY_OUTPUT_CODEC.fieldOf("result").forGetter(r -> r.output),
					IngredientWithSize.CODEC.fieldOf("input0").forGetter(r -> r.input0),
					IngredientWithSize.CODEC.fieldOf("input1").forGetter(r -> r.input1),
					ExtraCodecs.strictOptionalField(Codec.INT, "time", 200).forGetter(r -> r.time)
			).apply(inst, AlloyRecipe::new)
	);

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.ALLOY_SMELTER.iconStack();
	}

	@Override
	public Codec<AlloyRecipe> codec()
	{
		return CODEC;
	}

	@Nullable
	@Override
	public AlloyRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		Lazy<ItemStack> output = readLazyStack(buffer);
		IngredientWithSize input0 = IngredientWithSize.read(buffer);
		IngredientWithSize input1 = IngredientWithSize.read(buffer);
		int time = buffer.readInt();
		return new AlloyRecipe(output, input0, input1, time);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, AlloyRecipe recipe)
	{
		writeLazyStack(buffer, recipe.output);
		recipe.input0.write(buffer);
		recipe.input1.write(buffer);
		buffer.writeInt(recipe.time);
	}
}
