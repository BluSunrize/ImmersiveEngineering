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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class MetalPressRecipeSerializer extends IERecipeSerializer<MetalPressRecipe>
{
	public static final Codec<MetalPressRecipe> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			IngredientWithSize.CODEC.fieldOf("input").forGetter(r -> r.input),
			BuiltInRegistries.ITEM.byNameCodec().fieldOf("mold").forGetter(r -> r.mold),
			Codec.INT.fieldOf("energy").forGetter(MultiblockRecipe::getTotalProcessEnergy)
	).apply(inst, MetalPressRecipe::new));

	@Override
	public Codec<MetalPressRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.METAL_PRESS.iconStack();
	}

	@Nullable
	@Override
	public MetalPressRecipe fromNetwork(FriendlyByteBuf buffer)
	{
		TagOutput output = readLazyStack(buffer);
		IngredientWithSize input = IngredientWithSize.read(buffer);
		Item mold = PacketUtils.readRegistryElement(buffer, BuiltInRegistries.ITEM);
		int energy = buffer.readInt();
		return new MetalPressRecipe(output, input, mold, energy);
	}

	@Override
	public void toNetwork(FriendlyByteBuf buffer, MetalPressRecipe recipe)
	{
		writeLazyStack(buffer, recipe.output);
		recipe.input.write(buffer);
		PacketUtils.writeRegistryElement(buffer, BuiltInRegistries.ITEM, recipe.mold);
		buffer.writeInt(recipe.getTotalProcessEnergy());
	}
}
