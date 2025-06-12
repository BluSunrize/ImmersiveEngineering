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
import blusunrize.immersiveengineering.common.register.IEBlocks.MetalDevices;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ClocheRecipeSerializer extends IERecipeSerializer<ClocheRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, ClocheRecipe> CODEC = DualCompositeMapCodecs.composite(
			CHANCE_LIST_CODECS.fieldOf("results"), r -> r.outputs,
			DualCodecs.INGREDIENT.fieldOf("input"), r -> r.seed,
			DualCodecs.INGREDIENT.fieldOf("soil"), r -> r.soil,
			DualCodecs.INT.fieldOf("time"), r -> r.time,
			ClocheRenderFunction.CODECS.fieldOf("render"), r -> r.renderFunction,
			ClocheRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, ClocheRecipe> codecs()
	{
		return CODEC;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(MetalDevices.CLOCHE);
	}
}
