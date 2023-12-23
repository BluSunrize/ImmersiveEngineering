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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class SqueezerRecipeSerializer extends IERecipeSerializer<SqueezerRecipe>
{
	public static final Codec<SqueezerRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			optionalFluidOutput("fluid").forGetter(r -> r.fluidOutput),
			optionalItemOutput("result").forGetter(r -> r.itemOutput),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getBaseEnergy)
	).apply(inst, SqueezerRecipe::new));

	@Override
	public Codec<SqueezerRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.SQUEEZER.iconStack();
	}

	@Nullable
	@Override
	public SqueezerRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		FluidStack fluidOutput = buffer.readFluidStack();
		TagOutput itemOutput = readLazyStack(buffer);
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int energy = buffer.readInt();
		return new SqueezerRecipe(fluidOutput, itemOutput, input, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, SqueezerRecipe recipe)
	{
		buffer.writeFluidStack(recipe.fluidOutput);
		buffer.writeItem(recipe.itemOutput.get());
		recipe.input.write(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
