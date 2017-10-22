/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import javax.annotation.Nonnull;

/**
 * @author BluSunrize
 * @since 09.07.2017
 */
public class IngredientFactoryFluidStack implements IIngredientFactory
{
	@Nonnull
	@Override
	public Ingredient parse(JsonContext context, JsonObject json)
	{
		String name = JsonUtils.getString(json, "fluid");
		int amount = JsonUtils.getInt(json, "amount", 1000);
		Fluid fluid = FluidRegistry.getFluid(name);
		if(fluid==null)
			throw new JsonSyntaxException("Fluid with name "+name+" could not be found");
		return new IngredientFluidStack(fluid, amount);
	}
}
