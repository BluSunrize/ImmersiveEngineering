/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.ClocheFertilizer;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ClocheFertilizerSerializer extends IERecipeSerializer<ClocheFertilizer>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, ClocheFertilizer> CODECS = DualMapCodec.composite(
			DualCodecs.INGREDIENT.fieldOf("input"), r -> r.input,
			DualCodecs.FLOAT.fieldOf("growthModifier"), r -> r.growthModifier,
			ClocheFertilizer::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, ClocheFertilizer> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Items.BONE_MEAL);
	}
}
