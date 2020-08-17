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
import net.minecraft.fluid.Fluid;
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


	public FluidTagWithSize(Tag<Fluid> fluidTag, int count)
	{
		this.fluidTag = fluidTag;
		this.amount = count;
	}

	public static FluidTagWithSize deserialize(JsonElement input)
	{
		Preconditions.checkArgument(input instanceof JsonObject, "FluidTagWithSize can only be deserialized from a JsonObject");
		JsonObject jsonObject = input.getAsJsonObject();
		ResourceLocation resourcelocation = new ResourceLocation(JSONUtils.getString(jsonObject, "tag"));
		Tag<Fluid> tag = FluidTags.getCollection().get(resourcelocation);
		return new FluidTagWithSize(tag, JSONUtils.getInt(jsonObject, "amount"));
	}

	public static FluidTagWithSize read(PacketBuffer input)
	{
		Tag<Fluid> tag = FluidTags.getCollection().get(input.readResourceLocation());
		return new FluidTagWithSize(tag, input.readInt());
	}

	@Override
	public boolean test(@Nullable FluidStack fluidStack)
	{
		if(fluidStack==null)
			return false;
		return fluidTag.contains(fluidStack.getFluid())&&fluidStack.getAmount() >= this.amount;
	}

	@Nonnull
	public List<FluidStack> getMatchingFluidStacks()
	{
		return this.fluidTag.getAllElements().stream()
				.map(fluid -> new FluidStack(fluid, FluidTagWithSize.this.amount))
				.collect(Collectors.toList());
	}

	@Nonnull
	public JsonElement serialize()
	{
		JsonObject jsonObject = new JsonObject();
		jsonObject.addProperty("tag", this.fluidTag.getId().toString());
		jsonObject.addProperty("amount", this.amount);
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
	}
}
