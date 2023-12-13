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

import javax.annotation.Nullable;
import java.util.List;

public class BottlingMachineRecipeSerializer extends IERecipeSerializer<BottlingMachineRecipe>
{
	public static final Codec<BottlingMachineRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			TagOutputList.CODEC.fieldOf("results").forGetter(r -> r.output),
			listOrSingle(IngredientWithSize.CODEC, "input", "inputs").forGetter(r -> r.inputs),
					FluidTagInput.CODEC.fieldOf("fluid").forGetter(r -> r.fluidInput)
			).apply(inst, BottlingMachineRecipe::new)
	);

	@Override
	public Codec<BottlingMachineRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.BOTTLING_MACHINE.iconStack();
	}

	@Nullable
	@Override
	public BottlingMachineRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		List<TagOutput> outputs = PacketUtils.readList(buffer, IERecipeSerializer::readLazyStack);
		List<IngredientWithSize> ingredients = PacketUtils.readList(buffer, IngredientWithSize::read);
		FluidTagInput fluidInput = FluidTagInput.read(buffer);
		return new BottlingMachineRecipe(new TagOutputList(outputs), ingredients, fluidInput);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, BottlingMachineRecipe recipe)
	{
		PacketUtils.writeListReverse(buffer, recipe.output.get(), FriendlyByteBuf::writeItem);
		PacketUtils.writeList(buffer, recipe.inputs, IngredientWithSize::write);
		recipe.fluidInput.write(buffer);
	}
}
