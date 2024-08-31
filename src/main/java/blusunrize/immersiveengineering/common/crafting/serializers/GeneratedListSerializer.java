/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.crafting.GeneratedListRecipe;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class GeneratedListSerializer extends IERecipeSerializer<GeneratedListRecipe<?, ?>>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, GeneratedListRecipe<?, ?>> CODECS = DualCodecs.RESOURCE_LOCATION
			.<RegistryFriendlyByteBuf>castStream()
			.fieldOf("generatorID")
			.map(GeneratedListRecipe::from, GeneratedListRecipe::getGeneratorID);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, GeneratedListRecipe<?, ?>> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(Misc.WIRE_COILS.get(WireType.COPPER));
	}
}
