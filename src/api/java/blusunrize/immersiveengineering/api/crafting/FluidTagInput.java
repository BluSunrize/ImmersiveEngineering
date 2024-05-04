/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import blusunrize.immersiveengineering.api.utils.TagUtils;
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidTagInput implements Predicate<FluidStack>
{
	public static final Codec<FluidTagInput> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.mapEither(
					TagKey.codec(Registries.FLUID).fieldOf("tag"),
					ResourceLocation.CODEC.listOf().fieldOf("fluids")
			).forGetter(t -> t.fluidTag),
			Codec.INT.fieldOf("amount").forGetter(t -> t.amount),
			CompoundTag.CODEC.optionalFieldOf("nbt").forGetter(t -> Optional.ofNullable(t.nbtTag))
	).apply(inst, (tag, amount, nbt) -> new FluidTagInput(tag, amount, nbt.orElse(null))));
	public static final StreamCodec<RegistryFriendlyByteBuf, FluidTagInput> STREAM_CODEC = StreamCodec.composite(
			ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs.list()), FluidTagInput::getMatchingFluidNames,
			ByteBufCodecs.INT, t -> t.amount,
			// TODO this is probably broken, but has to be replaced anyway
			ByteBufCodecs.COMPOUND_TAG, t -> t.nbtTag,
			(names, amount, tag) -> new FluidTagInput(Either.right(names), amount, tag)
	);

	protected final Either<TagKey<Fluid>, List<ResourceLocation>> fluidTag;
	protected final int amount;
	protected final CompoundTag nbtTag;

	public FluidTagInput(Either<TagKey<Fluid>, List<ResourceLocation>> matching, int amount, CompoundTag nbtTag)
	{
		this.fluidTag = matching;
		this.amount = amount;
		this.nbtTag = nbtTag;
	}

	public FluidTagInput(TagKey<Fluid> fluidTag, int amount, CompoundTag nbtTag)
	{
		this(Either.left(fluidTag), amount, nbtTag);
	}

	public FluidTagInput(ResourceLocation resourceLocation, int amount, CompoundTag nbtTag)
	{
		this(TagKey.create(Registries.FLUID, resourceLocation), amount, nbtTag);
	}

	public FluidTagInput(ResourceLocation resourceLocation, int amount)
	{
		this(resourceLocation, amount, null);
	}

	public FluidTagInput(TagKey<Fluid> tag, int amount)
	{
		this(tag, amount, null);
	}

	public static FluidTagInput deserialize(JsonElement input)
	{
		Preconditions.checkArgument(input instanceof JsonObject, "FluidTagWithSize can only be deserialized from a JsonObject");
		JsonObject jsonObject = input.getAsJsonObject();
		ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "tag"));
		if(!GsonHelper.isValidNode(jsonObject, "nbt"))
			return new FluidTagInput(resourceLocation, GsonHelper.getAsInt(jsonObject, "amount"));
		try
		{
			CompoundTag nbt = ItemUtils.parseNbtFromJson(jsonObject.get("nbt"));
			return new FluidTagInput(resourceLocation, GsonHelper.getAsInt(jsonObject, "amount"), nbt);
		} catch(CommandSyntaxException e)
		{
			throw new JsonParseException(e);
		}
	}

	public FluidTagInput withAmount(int amount)
	{
		return new FluidTagInput(this.fluidTag, amount, this.nbtTag);
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
		if(this.nbtTag!=null)
			throw new UnsupportedOperationException();
			//return fluidStack.hasTag()&&fluidStack.getTag().equals(this.nbtTag);
		return true;
	}

	@Nonnull
	public List<FluidStack> getMatchingFluidStacks()
	{
		throw new UnsupportedOperationException();
		//return fluidTag.map(
		//		// TODO less global?
		//		t -> TagUtils.elementStream(BuiltInRegistries.FLUID, t),
		//		l -> l.stream().map(BuiltInRegistries.FLUID::get)
		//)
		//		.map(fluid -> new FluidStack(fluid, FluidTagInput.this.amount, FluidTagInput.this.nbtTag))
		//		.collect(Collectors.toList());
	}

	@Nonnull
	public JsonElement serialize()
	{
		JsonObject jsonObject = new JsonObject();
		ResourceLocation name = this.fluidTag.orThrow().location();
		jsonObject.addProperty("tag", name.toString());
		jsonObject.addProperty("amount", this.amount);
		if(this.nbtTag!=null)
			jsonObject.addProperty("nbt", this.nbtTag.toString());
		return jsonObject;
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

	public static FluidTagInput read(FriendlyByteBuf input)
	{
		int numMatching = input.readVarInt();
		List<ResourceLocation> matching = new ArrayList<>(numMatching);
		for(int i = 0; i < numMatching; ++i)
			matching.add(input.readResourceLocation());
		int amount = input.readInt();
		CompoundTag nbt = input.readBoolean()?input.readNbt(): null;
		return new FluidTagInput(Either.right(matching), amount, nbt);
	}

	public void write(FriendlyByteBuf out)
	{
		List<ResourceLocation> matching = getMatchingFluidNames();
		out.writeVarInt(matching.size());
		for(ResourceLocation rl : matching)
			out.writeResourceLocation(rl);
		out.writeInt(this.amount);
		out.writeBoolean(this.nbtTag!=null);
		if(this.nbtTag!=null)
			out.writeNbt(this.nbtTag);
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
				FluidStack toExtract = FluidUtils.copyFluidStackWithAmount(inTank, this.amount);
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
}
