/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.*;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class MixerRecipeSerializer extends IERecipeSerializer<MixerRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, MixerRecipe> CODECS = DualCompositeMapCodecs.composite(
			IEDualCodecs.FLUID_STACK.fieldOf("result"), r -> r.fluidOutput,
			IEDualCodecs.SIZED_FLUID_INGREDIENT.fieldOf("fluid"), r -> r.fluidInput,
			IngredientWithSize.CODECS.listOf().fieldOf("inputs"), r -> r.itemInputs,
			DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
			MixerRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, MixerRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.MIXER.iconStack();
	}
}
