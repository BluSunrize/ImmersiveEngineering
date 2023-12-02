/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.Optional;

public class RefineryRecipeSerializer extends IERecipeSerializer<RefineryRecipe>
{
	public static final Codec<RefineryRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			FluidStack.CODEC.fieldOf("result").forGetter(r -> r.output),
			FluidTagInput.CODEC.fieldOf("input0").forGetter(r -> r.input0),
			ExtraCodecs.strictOptionalField(FluidTagInput.CODEC, "input1").forGetter(r -> Optional.ofNullable(r.input1)),
			ExtraCodecs.strictOptionalField(Ingredient.CODEC, "catalyst", Ingredient.EMPTY).forGetter(r -> r.catalyst),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getTotalProcessEnergy)
	).apply(inst, RefineryRecipe::new));

	@Override
	public Codec<RefineryRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.REFINERY.iconStack();
	}

	@Nullable
	@Override
	public RefineryRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		FluidStack output = buffer.readFluidStack();
		FluidTagInput input0 = FluidTagInput.read(buffer);
		FluidTagInput input1 = buffer.readBoolean()?FluidTagInput.read(buffer): null;
		Ingredient catalyst = Ingredient.fromNetwork(buffer);
		int energy = buffer.readInt();
		return new RefineryRecipe(output, input0, input1, catalyst, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, RefineryRecipe recipe)
	{
		buffer.writeFluidStack(recipe.output);
		recipe.input0.write(buffer);
		if(recipe.input1!=null)
		{
			buffer.writeBoolean(true);
			recipe.input1.write(buffer);
		}
		else
			buffer.writeBoolean(false);
		recipe.catalyst.toNetwork(buffer);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
