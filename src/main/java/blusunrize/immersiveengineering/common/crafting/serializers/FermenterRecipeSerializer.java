/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.FermenterRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;

public class FermenterRecipeSerializer extends IERecipeSerializer<FermenterRecipe>
{
	public static final Codec<FermenterRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			optionalFluidOutput("fluid").forGetter(r -> r.fluidOutput),
			optionalItemOutput("result").forGetter(r -> r.itemOutput),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getTotalProcessEnergy)
	).apply(inst, FermenterRecipe::new));

	@Override
	public Codec<FermenterRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.FERMENTER.iconStack();
	}

	@Nullable
	@Override
	public FermenterRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		FluidStack fluidOutput = buffer.readFluidStack();
		Lazy<ItemStack> itemOutput = readLazyStack(buffer);
		IngredientWithSize input = IngredientWithSize.read(buffer);
		int energy = buffer.readInt();
		return new FermenterRecipe(fluidOutput, itemOutput, input, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, FermenterRecipe recipe)
	{
		buffer.writeFluidStack(recipe.fluidOutput);
		buffer.writeItem(recipe.itemOutput.get());
		recipe.input.write(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
