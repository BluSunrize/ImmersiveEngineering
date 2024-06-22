/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlastFurnaceFuel;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class BlastFurnaceFuelSerializer extends IERecipeSerializer<BlastFurnaceFuel>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, BlastFurnaceFuel> CODECS = DualMapCodec.composite(
			DualCodecs.INGREDIENT.fieldOf("input"), f -> f.input,
			DualCodecs.INT.fieldOf("time"), f -> f.burnTime,
			BlastFurnaceFuel::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, BlastFurnaceFuel> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(IEItems.Ingredients.COAL_COKE);
	}
}
