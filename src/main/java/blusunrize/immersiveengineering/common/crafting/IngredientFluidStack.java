/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.crafting;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.Tag;
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
	private final FluidTagInput fluidTagInput;

	public IngredientFluidStack(FluidTagInput fluidTagInput)
	{
		super(Stream.empty());
		this.fluidTagInput = fluidTagInput;
	}

	public IngredientFluidStack(Tag<Fluid> tag, int amount)
	{
		this(new FluidTagInput(tag.getId(), amount));
	}

	public FluidTagInput getFluidTagInput()
	{
		return fluidTagInput;
	}

	ItemStack[] cachedStacks;

	@Nonnull
	@Override
	public ItemStack[] getMatchingStacks()
	{
		if(cachedStacks==null)
		{
			cachedStacks = this.fluidTagInput.getMatchingFluidStacks()
					.stream()
					.map(FluidUtil::getFilledBucket)
					.toArray(ItemStack[]::new);
		}
		return this.cachedStacks;
	}

	@Override
	public boolean hasNoMatchingItems()
	{
		return this.fluidTagInput.getMatchingFluidStacks().isEmpty();
	}

	@Override
	public boolean test(@Nullable ItemStack stack)
	{
		if(stack==null)
			return false;
		else
			return this.fluidTagInput.test(FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY));
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
		JsonObject ret = (JsonObject)this.fluidTagInput.serialize();
		ret.addProperty("type", IngredientSerializerFluidStack.NAME.toString());
		return ret;
	}

	@Override
	public boolean isSimple()
	{
		return false;
	}
}
