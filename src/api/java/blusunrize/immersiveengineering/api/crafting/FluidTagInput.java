/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.utils.TagUtils;
import blusunrize.immersiveengineering.api.utils.codec.DualCodec;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidTagInput implements Predicate<FluidStack>
{
	public static final MapCodec<FluidTagInput> MAP_CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
			Codec.mapEither(
					TagKey.codec(Registries.FLUID).fieldOf("tag"),
					ResourceLocation.CODEC.listOf().fieldOf("fluids")
			).forGetter(t -> t.fluidTag),
			Codec.INT.fieldOf("amount").forGetter(t -> t.amount),
			DataComponentPredicate.CODEC.optionalFieldOf("nbt", DataComponentPredicate.EMPTY).forGetter(t -> t.predicate)
	).apply(inst, FluidTagInput::new));
	public static final Codec<FluidTagInput> CODEC = MAP_CODEC.codec();
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidTagInput> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), FluidTagInput::getMatchingFluidNames,
			ByteBufCodecs.INT, t -> t.amount,
			DataComponentPredicate.STREAM_CODEC, t -> t.predicate,
			(names, amount, tag) -> new FluidTagInput(Either.right(names), amount, tag)
	);
	public static final DualCodec<RegistryFriendlyByteBuf, FluidTagInput> CODECS = new DualCodec<>(CODEC, STREAM_CODEC);

	protected final Either<TagKey<Fluid>, List<ResourceLocation>> fluidTag;
	protected final int amount;
	protected final DataComponentPredicate predicate;

	public FluidTagInput(Either<TagKey<Fluid>, List<ResourceLocation>> matching, int amount, DataComponentPredicate predicate)
	{
		this.fluidTag = matching;
		this.amount = amount;
		this.predicate = predicate;
	}

	public FluidTagInput(TagKey<Fluid> fluidTag, int amount, @Nonnull DataComponentPredicate predicate)
	{
		this(Either.left(fluidTag), amount, predicate);
	}

	public FluidTagInput(ResourceLocation resourceLocation, int amount, @Nonnull DataComponentPredicate predicate)
	{
		this(TagKey.create(Registries.FLUID, resourceLocation), amount, predicate);
	}

	public FluidTagInput(ResourceLocation resourceLocation, int amount)
	{
		this(resourceLocation, amount, DataComponentPredicate.EMPTY);
	}

	public FluidTagInput(TagKey<Fluid> tag, int amount)
	{
		this(tag, amount, DataComponentPredicate.EMPTY);
	}

	public FluidTagInput withAmount(int amount)
	{
		return new FluidTagInput(this.fluidTag, amount, this.predicate);
	}

	@Override
	public boolean test(@Nullable FluidStack fluidStack)
	{
		return testIgnoringAmount(fluidStack)&&fluidStack.getAmount() >= this.amount;
	}

	public boolean testIgnoringAmount(@Nullable FluidStack fluidStack)
	{
		if(fluidStack==null)
			return false;
		if(!fluidTag.map(
				t -> fluidStack.getFluid().is(t),
				l -> l.contains(BuiltInRegistries.FLUID.getKey(fluidStack.getFluid()))
		))
			return false;
		return predicate.test(fluidStack);
	}

	@Nonnull
	public List<FluidStack> getMatchingFluidStacks()
	{
		return fluidTag.map(
						// TODO less global?
						t -> TagUtils.elementStream(BuiltInRegistries.FLUID, t),
						l -> l.stream().map(BuiltInRegistries.FLUID::get)
				)
				// TODO include components/nbt
				.map(fluid -> {
					final FluidStack matchingStack = new FluidStack(fluid, FluidTagInput.this.amount);
					matchingStack.applyComponents(predicate.asPatch());
					return matchingStack;
				})
				.collect(Collectors.toList());
	}

	public int getAmount()
	{
		return amount;
	}

	public FluidStack getRandomizedExampleStack(int rand)
	{
		List<FluidStack> all = getMatchingFluidStacks();
		return all.get((rand/20)%all.size());
	}

	private List<ResourceLocation> getMatchingFluidNames()
	{
		return fluidTag.map(
				f -> TagUtils.holderStream(BuiltInRegistries.FLUID, f)
						.map(Holder::unwrapKey)
						.map(Optional::orElseThrow)
						.map(ResourceKey::location)
						.collect(Collectors.toList()),
				l -> l
		);
	}

	public boolean extractFrom(IFluidHandler handler, FluidAction action)
	{
		// This is not ideal, but probably the best possible without issues with other mods:
		// - This does not handle the case where an item contains two separate tanks of matching fluids, neither of
		// them large enough to fulfill the input, but both combined are sufficient
		// - However handling that will result in one of two issues in simulation calls:
		//   - Either it will not detect one tank affecting the other (because it only uses simulation calls)
		//   - Or we actually drain (EXECUTE), which will break for Endertank-style items even if we run the code on a
		//     copy of the item stack
		for(int tank = 0; tank < handler.getTanks(); tank++)
		{
			FluidStack inTank = handler.getFluidInTank(tank);
			if(testIgnoringAmount(inTank))
			{
				FluidStack toExtract = inTank.copyWithAmount(this.amount);
				FluidStack extractedSim = handler.drain(toExtract, FluidAction.SIMULATE);
				if(extractedSim.getAmount() >= this.amount)
				{
					if(action!=FluidAction.SIMULATE)
						handler.drain(toExtract, action);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this==o) return true;
		if(o==null||getClass()!=o.getClass()) return false;
		FluidTagInput that = (FluidTagInput)o;
		return amount==that.amount&&Objects.equals(fluidTag, that.fluidTag)&&Objects.equals(predicate, that.predicate);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(fluidTag, amount, predicate);
	}
}
