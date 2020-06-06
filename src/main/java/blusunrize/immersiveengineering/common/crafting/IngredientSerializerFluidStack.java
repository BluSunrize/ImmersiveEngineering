/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.fluid.Fluid;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

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
	public IngredientFluidStack parse(@Nonnull PacketBuffer buffer)
	{
		ResourceLocation name = new ResourceLocation(buffer.readString(512));
		int amount = buffer.readInt();
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(name);
		if(fluid==null)
			throw new JsonSyntaxException("Fluid with name "+name+" could not be found");
		return new IngredientFluidStack(fluid, amount);
	}

	@Nonnull
	@Override
	public IngredientFluidStack parse(@Nonnull JsonObject json)
	{
		ResourceLocation name = new ResourceLocation(JSONUtils.getString(json, "fluid"));
		int amount = JSONUtils.getInt(json, "amount", 1000);
		Fluid fluid = ForgeRegistries.FLUIDS.getValue(name);
		if(fluid==null)
			throw new JsonSyntaxException("Fluid with name "+name+" could not be found");
		return new IngredientFluidStack(fluid, amount);
	}

	@Override
	public void write(@Nonnull PacketBuffer buffer, @Nonnull IngredientFluidStack ingredient)
	{
		buffer.writeString(ingredient.getFluid().getFluid().getRegistryName().toString());
		buffer.writeInt(ingredient.getFluid().getAmount());
		//TODO NBT?
	}
}
