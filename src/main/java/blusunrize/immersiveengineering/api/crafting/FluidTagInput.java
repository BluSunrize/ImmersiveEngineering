/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.utils.IEPacketBuffer;
import blusunrize.immersiveengineering.api.utils.ItemUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidTagInput implements Predicate<FluidStack>
{
	protected final Either<Tag<Fluid>, List<Fluid>> content;
	protected final int amount;
	protected final CompoundNBT nbtTag;

	public FluidTagInput(Either<Tag<Fluid>, List<Fluid>> content, int amount, CompoundNBT nbtTag)
	{
		this.content = content;
		this.amount = amount;
		this.nbtTag = nbtTag;
	}

	public FluidTagInput(List<Fluid> fluids, int amount, CompoundNBT nbtTag)
	{
		this(Either.right(fluids), amount, nbtTag);
	}

	public FluidTagInput(Tag<Fluid> tag, int amount, CompoundNBT nbtTag)
	{
		this(Either.left(tag), amount, nbtTag);
	}

	public FluidTagInput(ResourceLocation resourceLocation, int amount, CompoundNBT nbtTag)
	{
		this(new FluidTags.Wrapper(resourceLocation), amount, nbtTag);
	}

	public FluidTagInput(ResourceLocation resourceLocation, int amount)
	{
		this(resourceLocation, amount, null);
	}

	public static FluidTagInput deserialize(JsonElement input)
	{
		Preconditions.checkArgument(input instanceof JsonObject, "FluidTagWithSize can only be deserialized from a JsonObject");
		JsonObject jsonObject = input.getAsJsonObject();
		ResourceLocation resourceLocation = new ResourceLocation(JSONUtils.getString(jsonObject, "tag"));
		if(!JSONUtils.hasField(jsonObject, "nbt"))
			return new FluidTagInput(resourceLocation, JSONUtils.getInt(jsonObject, "amount"));
		try
		{
			CompoundNBT nbt = ItemUtils.parseNbtFromJson(jsonObject.get("nbt"));
			return new FluidTagInput(resourceLocation, JSONUtils.getInt(jsonObject, "amount"), nbt);
		} catch(CommandSyntaxException e)
		{
			throw new JsonParseException(e);
		}
	}

	public FluidTagInput withAmount(int amount)
	{
		return new FluidTagInput(this.content, amount, this.nbtTag);
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
		if(!content.map(
				tag -> tag.contains(fluidStack.getFluid()),
				list -> list.contains(fluidStack.getFluid())
		))
			return false;
		if(this.nbtTag!=null)
			return fluidStack.hasTag()&&fluidStack.getTag().equals(this.nbtTag);
		return true;
	}

	@Nonnull
	public List<FluidStack> getMatchingFluidStacks()
	{
		return getMatchingFluids()
				.stream()
				.map(fluid -> new FluidStack(fluid, FluidTagInput.this.amount, FluidTagInput.this.nbtTag))
				.collect(Collectors.toList());
	}

	private Collection<Fluid> getMatchingFluids() {
		return content.map(
				tag -> {
					if(tag==null)
						return ImmutableList.of();
					return tag.getAllElements();
				},
				Function.identity()
		);
	}

	@Nonnull
	public JsonElement serialize()
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("tag", this.content.orThrow().getId().toString());
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

	public void write(PacketBuffer out)
	{
		IEPacketBuffer.wrap(out).writeList(
				getMatchingFluids(), (f, buffer) -> buffer.writeRegistryIdUnsafe(ForgeRegistries.FLUIDS, f)
		);
		out.writeVarInt(this.amount);
		out.writeBoolean(this.nbtTag!=null);
		if(this.nbtTag!=null)
			out.writeCompoundTag(this.nbtTag);
	}

	public static FluidTagInput read(PacketBuffer input)
	{
		List<Fluid> fluids = IEPacketBuffer.wrap(input).readList(
				buffer -> buffer.readRegistryIdUnsafe(ForgeRegistries.FLUIDS)
		);
		int amount = input.readVarInt();
		CompoundNBT nbt = input.readBoolean()?input.readCompoundTag(): null;
		return new FluidTagInput(fluids, amount, nbt);
	}
}
