/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public class SqueezerRecipeSerializer extends IERecipeSerializer<SqueezerRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, SqueezerRecipe> CODECS = DualCompositeMapCodecs.composite(
			optionalFluidOutput("fluid"), r -> r.fluidOutput,
			optionalItemOutput("result"), r -> r.itemOutput,
			IngredientWithSize.CODECS.fieldOf("input"), r -> r.input,
			DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
			SqueezerRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, SqueezerRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.SQUEEZER.iconStack();
	}
}
