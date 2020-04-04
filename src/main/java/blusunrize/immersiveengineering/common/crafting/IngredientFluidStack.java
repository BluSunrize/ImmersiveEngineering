/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.stream.Stream;

/**
 * @author BluSunrize - 03.07.2017
 */
public class IngredientFluidStack extends Ingredient
{
	private final FluidStack fluid;

	public IngredientFluidStack(FluidStack fluid)
	{
		super(Stream.empty());
		this.fluid = fluid;
	}

	public IngredientFluidStack(Fluid fluid, int amount)
	{
		this(new FluidStack(fluid, amount));
	}

	public FluidStack getFluid()
	{
		return fluid;
	}

	ItemStack[] cachedStacks;

	@Nonnull
	@Override
	public ItemStack[] getMatchingStacks()
	{
		if(cachedStacks==null)
		{
			cachedStacks = new ItemStack[]{FluidUtil.getFilledBucket(fluid)};
		}
		return this.cachedStacks;
	}

	@Override
	public boolean test(@Nullable ItemStack stack)
	{
		if(stack==null)
		{
			return false;
		}
		else
		{
			return FluidUtil.getFluidContained(stack)
					.map(fs -> fs.containsFluid(fluid))
					.orElse(false);
		}
	}

	@Nonnull
	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer()
	{
		return IngredientSerializerFluidStack.INSTANCE;
	}

	@Nonnull
	@Override
	public JsonElement serialize()
	{
		JsonObject ret = new JsonObject();
		ret.addProperty("amount", fluid.getAmount());
		ret.addProperty("fluid", fluid.getFluid().getRegistryName().toString());
		ret.addProperty("type", IngredientSerializerFluidStack.NAME.toString());
		return ret;
	}

	@Override
	public boolean isSimple()
	{
		return false;
	}
}
