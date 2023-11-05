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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.List;

public class MixerRecipeSerializer extends IERecipeSerializer<MixerRecipe>
{
	public static final Codec<MixerRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			FluidStack.CODEC.fieldOf("result").forGetter(r -> r.fluidOutput),
			FluidTagInput.CODEC.fieldOf("fluid").forGetter(r -> r.fluidInput),
			IngredientWithSize.CODEC.listOf().fieldOf("inputs").forGetter(r -> r.itemInputs),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getTotalProcessEnergy)
	).apply(inst, MixerRecipe::new));

	@Override
	public Codec<MixerRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.MIXER.iconStack();
	}

	@Nullable
	@Override
	public MixerRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		FluidStack fluidOutput = buffer.readFluidStack();
		FluidTagInput fluidInput = FluidTagInput.read(buffer);
		List<IngredientWithSize> itemInputs = PacketUtils.readList(buffer, IngredientWithSize::read);
		int energy = buffer.readInt();
		return new MixerRecipe(fluidOutput, fluidInput, itemInputs, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MixerRecipe recipe)
	{
		buffer.writeFluidStack(recipe.fluidOutput);
		recipe.fluidInput.write(buffer);
		PacketUtils.writeList(buffer, recipe.itemInputs, IngredientWithSize::write);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
