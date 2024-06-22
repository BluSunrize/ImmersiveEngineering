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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MetalPressRecipeSerializer extends IERecipeSerializer<MetalPressRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, MetalPressRecipe> CODECS = DualMapCodec.composite(
			TagOutput.CODECS.fieldOf("result"), r -> r.output,
			IngredientWithSize.CODECS.fieldOf("input"), r -> r.input,
			DualCodecs.registry(BuiltInRegistries.ITEM).fieldOf("mold"), r -> r.mold,
			DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
			MetalPressRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, MetalPressRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.METAL_PRESS.iconStack();
	}
}
