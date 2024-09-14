/*
 * BluSunrize
 * Copyright (c) 2021
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.serializers;

import blusunrize.immersiveengineering.common.crafting.INoContainersRecipe;
import blusunrize.immersiveengineering.common.crafting.NoContainersRecipe;
import blusunrize.immersiveengineering.common.crafting.NoContainersShapedRecipe;
import com.mojang.serialization.MapCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualMapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public class NoContainerSerializer implements RecipeSerializer<INoContainersRecipe>
{
	public static final String BASE_RECIPE = "baseRecipe";

	public static final DualMapCodec<RegistryFriendlyByteBuf, INoContainersRecipe> CODECS = DualCodecs.RECIPE.fieldOf(BASE_RECIPE).map(inner -> {
		if(inner instanceof ShapedRecipe shaped)
			return new NoContainersShapedRecipe<>(shaped);
		else
			return new NoContainersRecipe<>((CraftingRecipe)inner);
	}, INoContainersRecipe::baseRecipe);

	@Override
	public MapCodec<INoContainersRecipe> codec()
	{
		return CODECS.mapCodec();
	}

	@Override
	public StreamCodec<RegistryFriendlyByteBuf, INoContainersRecipe> streamCodec()
	{
		return CODECS.streamCodec();
	}
}
