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
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
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

	public IngredientFluidStack(INamedTag<Fluid> tag, int amount)
	{
		this(new FluidTagInput(tag.getName(), amount));
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
			cachedStacks = this.fluidTagInput.getMatchingFluidStacks()
					.stream()
					.map(FluidUtil::getFilledBucket)
					.toArray(ItemStack[]::new);
		return this.cachedStacks;
	}

	@Override
	public boolean hasNoMatchingItems()
	{
		return false;
		// todo? I don't think there is a way to do this now, because the tag isn't bound yet on world load
		// this.fluidTagInput.getMatchingFluidStacks().isEmpty();
	}

	@Override
	public boolean test(@Nullable ItemStack stack)
	{
		if(stack==null)
			return false;
		else
			return this.fluidTagInput.test(FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY));
	}

	//TODO this is a bit problematic
	@Nonnull
	@Override
	public IIngredientSerializer<? extends Ingredient> getSerializer()
	{
		throw new UnsupportedOperationException("IngredientFluidStack is for internal use only!");
	}

	@Nonnull
	@Override
	public JsonElement serialize()
	{
		throw new UnsupportedOperationException("IngredientFluidStack is for internal use only!");
	}

	@Override
	public boolean isSimple()
	{
		return false;
	}

	public ItemStack getExtractedStack(ItemStack input)
	{
		Optional<IFluidHandlerItem> handlerOpt = FluidUtil.getFluidHandler(input).resolve();
		if(handlerOpt.isPresent())
		{
			IFluidHandlerItem handler = handlerOpt.get();
			handler.drain(fluidTagInput.getAmount(), FluidAction.EXECUTE);
			return handler.getContainer();
		}
		//TODO throw XCP?
		return input;
	}
}
