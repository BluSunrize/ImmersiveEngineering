/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public class BlueprintCraftingRecipeSerializer extends IERecipeSerializer<BlueprintCraftingRecipe>
{
	public static final MapCodec<BlueprintCraftingRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.STRING.fieldOf("category").forGetter(r -> r.blueprintCategory),
			TagOutput.CODEC.fieldOf("result").forGetter(r -> r.output),
			IngredientWithSize.CODEC.listOf().fieldOf("inputs").forGetter(r -> r.inputs)
	).apply(inst, BlueprintCraftingRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, BlueprintCraftingRecipe> STREAM_CODEC = StreamCodec.composite(
			ByteBufCodecs.stringUtf8(128), r -> r.blueprintCategory,
			TagOutput.STREAM_CODEC, r -> r.output,
			IngredientWithSize.STREAM_CODEC.apply(ByteBufCodecs.list()), r -> r.inputs,
			BlueprintCraftingRecipe::new
	);

	@Override
	public MapCodec<BlueprintCraftingRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, BlueprintCraftingRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(WoodenDevices.WORKBENCH);
	}
}
