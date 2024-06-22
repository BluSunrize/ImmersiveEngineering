/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class BottlingMachineRecipeSerializer extends IERecipeSerializer<BottlingMachineRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, BottlingMachineRecipe> CODECS = DualMapCodec.composite(
			TagOutputList.CODEC.fieldOf("results"), r -> r.output,
			listOrSingle(IngredientWithSize.CODECS, "input", "inputs"), r -> r.inputs,
			FluidTagInput.CODECS.fieldOf("fluid"), r -> r.fluidInput,
			BottlingMachineRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, BottlingMachineRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.BOTTLING_MACHINE.iconStack();
	}
}
