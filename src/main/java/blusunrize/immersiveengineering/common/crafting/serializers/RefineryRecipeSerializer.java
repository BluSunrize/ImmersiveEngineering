/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.MultiblockRecipe;
import blusunrize.immersiveengineering.api.crafting.RefineryRecipe;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeMapCodecs;
import malte0811.dualcodecs.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEMultiblockLogic;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Optional;

public class RefineryRecipeSerializer extends IERecipeSerializer<RefineryRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, RefineryRecipe> CODECS = DualCompositeMapCodecs.composite(
			IEDualCodecs.FLUID_STACK.fieldOf("result"), r -> r.output,
			FluidTagInput.CODECS.fieldOf("input0"), r -> r.input0,
			FluidTagInput.CODECS.optionalFieldOf("input1"), r -> Optional.ofNullable(r.input1),
			DualCodecs.INGREDIENT.optionalFieldOf("catalyst", Ingredient.EMPTY), r -> r.catalyst,
			DualCodecs.INT.fieldOf("energy"), MultiblockRecipe::getBaseEnergy,
			RefineryRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, RefineryRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return IEMultiblockLogic.REFINERY.iconStack();
	}
}
