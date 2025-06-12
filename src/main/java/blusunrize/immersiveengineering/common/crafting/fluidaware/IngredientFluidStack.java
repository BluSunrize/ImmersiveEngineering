/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.crafting.fluidaware;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.common.register.IEIngredients;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.capabilities.Capabilities.FluidHandler;
import net.neoforged.neoforge.common.crafting.ICustomIngredient;
import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.common.util.NeoForgeExtraCodecs;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.crafting.SingleFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import net.neoforged.neoforge.fluids.crafting.TagFluidIngredient;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author BluSunrize - 03.07.2017
 */
public record IngredientFluidStack(SizedFluidIngredient fluidIngredient) implements ICustomIngredient
{
	private static final MapCodec<SizedFluidIngredient> SIMPLE_SIZED_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			NeoForgeExtraCodecs.xor(SingleFluidIngredient.CODEC, TagFluidIngredient.CODEC).forGetter(i ->
					i.ingredient() instanceof SingleFluidIngredient single?Either.left(single): Either.right((TagFluidIngredient)i.ingredient())),
			Codec.INT.fieldOf("amount").forGetter(SizedFluidIngredient::amount)
	).apply(inst, (either, amount) -> either.map(single -> new SizedFluidIngredient(single, amount), tag -> new SizedFluidIngredient(tag, amount))));

	public static final MapCodec<IngredientFluidStack> MAP_CODEC = NeoForgeExtraCodecs.mapWithAlternative(
			SIMPLE_SIZED_CODEC.xmap(
					IngredientFluidStack::new,
					IngredientFluidStack::fluidIngredient
			),
			SizedFluidIngredient.FLAT_CODEC.optionalFieldOf("ingredient").xmap(
					sizedFluidIngredient -> sizedFluidIngredient.map(IngredientFluidStack::new).orElse(null),
					ingredientFluidStack -> Optional.of(ingredientFluidStack.fluidIngredient)
			)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, IngredientFluidStack> STREAM_CODEC = SizedFluidIngredient.STREAM_CODEC.map(
			IngredientFluidStack::new, IngredientFluidStack::fluidIngredient
	);

	public IngredientFluidStack(TagKey<Fluid> tag, int amount)
	{
		this(SizedFluidIngredient.of(tag, amount));
	}

	@Nonnull
	@Override
	public Stream<ItemStack> getItems()
	{
		return Arrays.stream(this.fluidIngredient.getFluids())
				.map(FluidUtil::getFilledBucket)
				.filter(s -> !s.isEmpty());
	}

	@Override
	public boolean test(@Nullable ItemStack stack)
	{
		if(stack==null||stack.isEmpty())
			return false;
		Optional<FluidStack> fluid = FluidUtils.getFluidContained(stack);
		return fluid.isPresent()&&fluidIngredient.test(fluid.get());
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
			handler.drain(fluidIngredient.amount(), FluidAction.EXECUTE);
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
