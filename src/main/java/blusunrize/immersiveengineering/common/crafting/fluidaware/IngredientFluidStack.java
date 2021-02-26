/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.api.crafting.FluidTagInput;
import blusunrize.immersiveengineering.common.crafting.IngredientSerializerFluidStack;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraftforge.common.crafting.IIngredientSerializer;
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
		this(new FluidTagInput(tag, amount));
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
		Optional<IFluidHandlerItem> handler = FluidUtil.getFluidHandler(stack).resolve();
		return handler.isPresent()&&fluidTagInput.extractFrom(handler.get(), FluidAction.SIMULATE);
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

	public ItemStack getExtractedStack(ItemStack input)
	{
		Optional<IFluidHandlerItem> handlerOpt = FluidUtil.getFluidHandler(input).resolve();
		if(handlerOpt.isPresent())
		{
			IFluidHandlerItem handler = handlerOpt.get();
			fluidTagInput.extractFrom(handler, FluidAction.EXECUTE);
			return handler.getContainer();
		}
		return input;
	}
}
