/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class SawmillRecipeSerializer extends IERecipeSerializer<SawmillRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, SawmillRecipe> CODECS = DualMapCodec.composite(
			TagOutput.CODECS.fieldOf("result"), r -> r.output,
			optionalItemOutput("stripped"), r -> r.stripped,
			DualCodecs.INGREDIENT.fieldOf("input"), r -> r.input,
			DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
			TagOutputList.CODEC.optionalFieldOf("strippingSecondaries", TagOutputList.EMPTY), r -> r.secondaryStripping,
			TagOutputList.CODEC.optionalFieldOf("secondaryOutputs", TagOutputList.EMPTY), r -> r.secondaryOutputs,
			SawmillRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, SawmillRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.SAWMILL.iconStack();
	}
}
