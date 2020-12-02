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
import net.minecraft.tags.ITag.INamedTag;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.fluids.FluidUtil;
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
		// Copy the stack here, that allows us to be (more) sure that we can actually extract the required amount (and
		// won't run into issues where extracting from one tank affects the other one)
		Optional<IFluidHandlerItem> handler = FluidUtil.getFluidHandler(stack.copy()).resolve();
		if(!handler.isPresent())
			return false;
		return fluidTagInput.extractFrom(handler.get());
	}

	//TODO this is a bit problematic
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
			fluidTagInput.extractFrom(handler);
			//TODO somehow handle failure to extract?
			return handler.getContainer();
		}
		//TODO throw XCP if not present?
		return input;
	}
}
