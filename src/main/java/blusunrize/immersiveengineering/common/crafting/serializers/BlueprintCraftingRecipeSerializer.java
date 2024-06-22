/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.api.crafting.BlueprintCraftingRecipe;
import blusunrize.immersiveengineering.api.crafting.IERecipeSerializer;
import blusunrize.immersiveengineering.api.crafting.IngredientWithSize;
import blusunrize.immersiveengineering.api.crafting.TagOutput;
import blusunrize.immersiveengineering.api.utils.codec.DualCodecs;
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEBlocks.WoodenDevices;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class BlueprintCraftingRecipeSerializer extends IERecipeSerializer<BlueprintCraftingRecipe>
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, BlueprintCraftingRecipe> CODECS = DualMapCodec.composite(
			DualCodecs.STRING.fieldOf("category"), r -> r.blueprintCategory,
			TagOutput.CODECS.fieldOf("result"), r -> r.output,
			IngredientWithSize.CODECS.listOf().fieldOf("inputs"), r -> r.inputs,
			BlueprintCraftingRecipe::new
	);

	@Override
	protected DualMapCodec<RegistryFriendlyByteBuf, BlueprintCraftingRecipe> codecs()
	{
		return CODECS;
	}

	@Override
	public ItemStack getIcon()
	{
		return new ItemStack(WoodenDevices.WORKBENCH);
	}
}
