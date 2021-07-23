/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.common.crafting.fluidaware.IngredientFluidStack;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;

import javax.annotation.Nonnull;

/**
 * @author BluSunrize
 * @since 09.07.2017
 */
public class IngredientSerializerFluidStack implements IIngredientSerializer<IngredientFluidStack>
{
	public static ResourceLocation NAME = new ResourceLocation(ImmersiveEngineering.MODID, "fluid");
	public static IIngredientSerializer<IngredientFluidStack> INSTANCE = new IngredientSerializerFluidStack();

	@Nonnull
	@Override
	public IngredientFluidStack parse(@Nonnull FriendlyByteBuf buffer)
	{
		return new IngredientFluidStack(FluidTagInput.read(buffer));
	}

	@Nonnull
	@Override
	public IngredientFluidStack parse(@Nonnull JsonObject json)
	{
		return new IngredientFluidStack(FluidTagInput.deserialize(json));
	}

	@Override
	public void write(@Nonnull FriendlyByteBuf buffer, @Nonnull IngredientFluidStack ingredient)
	{
		ingredient.getFluidTagInput().write(buffer);
	}
}
