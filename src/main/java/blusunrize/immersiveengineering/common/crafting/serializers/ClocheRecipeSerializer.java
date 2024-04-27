/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.ClocheRecipe;
import blusunrize.immersiveengineering.api.crafting.ClocheRenderFunction;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.TagOutputList;
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ClocheRecipeSerializer extends IERecipeSerializer<ClocheRecipe>
{
	public static final MapCodec<ClocheRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			TagOutputList.CODEC.fieldOf("results").forGetter(r -> r.outputs),
			Ingredient.CODEC.fieldOf("input").forGetter(r -> r.seed),
			Ingredient.CODEC.fieldOf("soil").forGetter(r -> r.soil),
			Codec.INT.fieldOf("time").forGetter(r -> r.time),
			ClocheRenderFunction.CODEC.fieldOf("render").forGetter(r -> r.renderFunction)
	).apply(inst, ClocheRecipe::new));
	public static final StreamCodec<RegistryFriendlyByteBuf, ClocheRecipe> STREAM_CODEC = StreamCodec.composite(
			TagOutputList.STREAM_CODEC, r -> r.outputs,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.seed,
			Ingredient.CONTENTS_STREAM_CODEC, r -> r.soil,
			ByteBufCodecs.INT, r -> r.time,
			ClocheRenderFunction.STREAM_CODEC, r -> r.renderFunction,
			ClocheRecipe::new
	);

	@Override
	public MapCodec<ClocheRecipe> codec()
	{
		return CODEC;
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, ClocheRecipe> streamCodec()
	{
		return STREAM_CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(MetalDevices.CLOCHE);
	}
}
