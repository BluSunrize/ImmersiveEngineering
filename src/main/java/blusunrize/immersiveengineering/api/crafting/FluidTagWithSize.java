/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidTagWithSize implements Predicate<FluidStack>
{
	protected final Tag<Fluid> fluidTag;
	protected final int amount;
	protected final CompoundNBT nbtTag;

	public FluidTagWithSize(Tag<Fluid> fluidTag, int amount, CompoundNBT nbtTag)
	{
		this.fluidTag = fluidTag;
		this.amount = amount;
		this.nbtTag = nbtTag;
	}

	public FluidTagWithSize(Tag<Fluid> fluidTag, int amount)
	{
		this(fluidTag, amount, null);
	}

	public static FluidTagWithSize deserialize(JsonElement input)
	{
		Preconditions.checkArgument(input instanceof JsonObject, "FluidTagWithSize can only be deserialized from a JsonObject");
		JsonObject jsonObject = input.getAsJsonObject();
		ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getString(jsonObject, "tag"));
		Tag<Fluid> tag = FluidTags.getCollection().get(resourcelocation);
		if(!JSONUtils.hasField(jsonObject, "nbt"))
			return new FluidTagWithSize(tag, JSONUtils.getInt(jsonObject, "amount"));
		try
		{
			CompoundNBT nbt = JsonToNBT.getTagFromJson(JSONUtils.getString(jsonObject, "nbt"));
			return new FluidTagWithSize(tag, JSONUtils.getInt(jsonObject, "amount"), nbt);
		} catch(CommandSyntaxException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static FluidTagWithSize read(PacketBuffer input)
	{
		Tag<Fluid> tag = FluidTags.getCollection().get(input.readResourceLocation());
		int amount = input.readInt();
		CompoundNBT nbt = input.readBoolean()?input.readCompoundTag(): null;
		return new FluidTagWithSize(tag, amount, nbt);
	}

	public FluidTagWithSize withAmount(int amount)
	{
		return new FluidTagWithSize(this.fluidTag, amount, this.nbtTag);
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
		if(!fluidTag.contains(fluidStack.getFluid()))
			return false;
		if(this.nbtTag!=null)
			return fluidStack.hasTag()&&fluidStack.getTag().equals(this.nbtTag);
		return true;
	}

	@Nonnull
	public List<FluidStack> getMatchingFluidStacks()
	{
		return this.fluidTag.getAllElements().stream()
				.map(fluid -> new FluidStack(fluid, FluidTagWithSize.this.amount, FluidTagWithSize.this.nbtTag))
				.collect(Collectors.toList());
	}

	@Nonnull
	public JsonElement serialize()
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("tag", this.fluidTag.getId().toString());
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
		out.writeResourceLocation(this.fluidTag.getId());
		out.writeInt(this.amount);
		out.writeBoolean(this.nbtTag!=null);
		if(this.nbtTag!=null)
			out.writeCompoundTag(this.nbtTag);
	}
}
