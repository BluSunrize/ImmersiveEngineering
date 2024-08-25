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
import blusunrize.immersiveengineering.api.utils.codec.DualMapCodec;
import blusunrize.immersiveengineering.common.register.IEIngredients;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author BluSunrize - 03.07.2017
 */
public record IngredientFluidStack(FluidTagInput fluidTagInput) implements ICustomIngredient
{
	public static final DualMapCodec<RegistryFriendlyByteBuf, IngredientFluidStack> CODEC = FluidTagInput.MAP_CODECS.map(
			IngredientFluidStack::new, IngredientFluidStack::getFluidTagInput
	);

	public IngredientFluidStack(TagKey<Fluid> tag, int amount)
	{
		this(new FluidTagInput(tag, amount, DataComponentPredicate.EMPTY));
	}

	public FluidTagInput getFluidTagInput()
	{
		return fluidTagInput;
	}

	@Nonnull
	@Override
	public Stream<ItemStack> getItems()
	{
		return this.fluidTagInput.getMatchingFluidStacks()
				.stream()
				.map(FluidUtil::getFilledBucket)
				.filter(s -> !s.isEmpty());
	}

	@Override
	public boolean test(@Nullable ItemStack stack)
	{
		if(stack==null||stack.isEmpty())
			return false;
		Optional<IFluidHandlerItem> handler = FluidUtil.getFluidHandler(stack);
		return handler.isPresent()&&fluidTagInput.extractFrom(handler.get(), FluidAction.SIMULATE);
	}

	@Override
	public boolean isSimple()
	{
		return false;
	}

	public ItemStack getExtractedStack(ItemStack input)
	{
		IFluidHandlerItem handler = input.copyWithCount(1).getCapability(FluidHandler.ITEM);
		if(handler!=null)
		{
			fluidTagInput.extractFrom(handler, FluidAction.EXECUTE);
			return handler.getContainer();
		}
		return input.getCraftingRemainingItem();
	}

	@Override
	public IngredientType<?> getType()
	{
		return IEIngredients.FLUID_STACK.value();
	}
}
