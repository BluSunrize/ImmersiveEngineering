/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

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
			FluidStack fs = FluidUtil.getFluidContained(stack);
			return fs==null&&this.fluid==null||fs!=null&&fs.containsFluid(fluid);
		}
	}

	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer()
	{
		return IngredientSerializerFluidStack.INSTANCE;
	}
}
