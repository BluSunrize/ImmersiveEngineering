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
import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FluidTagInput implements Predicate<FluidStack>
{
	// Generally left on the server, right on the client
	protected final Either<Tag<Fluid>, List<ResourceLocation>> fluidTag;
	protected final int amount;
	protected final CompoundTag nbtTag;

	public FluidTagInput(Either<Tag<Fluid>, List<ResourceLocation>> matching, int amount, CompoundTag nbtTag)
	{
		this.fluidTag = matching;
		this.amount = amount;
		this.nbtTag = nbtTag;
	}

	public FluidTagInput(Tag<Fluid> fluidTag, int amount, CompoundTag nbtTag)
	{
		this(Either.left(fluidTag), amount, nbtTag);
	}

	public FluidTagInput(ResourceLocation resourceLocation, int amount, CompoundTag nbtTag)
	{
		this(getTagCollection().getTag(resourceLocation), amount, nbtTag);
	}

	public FluidTagInput(ResourceLocation resourceLocation, int amount)
	{
		this(resourceLocation, amount, null);
	}

	public FluidTagInput(Tag<Fluid> tag, int amount)
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
				t -> t.contains(fluidStack.getFluid()),
				l -> l.contains(fluidStack.getFluid().getRegistryName())
		))
			return false;
		if(this.nbtTag!=null)
			return fluidStack.hasTag()&&fluidStack.getTag().equals(this.nbtTag);
		return true;
	}

	@Nonnull
	public List<FluidStack> getMatchingFluidStacks()
	{
		return fluidTag.map(
				t -> t.getValues().stream(),
				l -> l.stream().map(ForgeRegistries.FLUIDS::getValue)
		)
				.map(fluid -> new FluidStack(fluid, FluidTagInput.this.amount, FluidTagInput.this.nbtTag))
				.collect(Collectors.toList());
	}

	@Nonnull
	public JsonElement serialize()
	{
		JsonObject jsonObject = new JsonObject();
		Tag<Fluid> unnamedTag = this.fluidTag.orThrow();
		ResourceLocation name = getTagCollection().getIdOrThrow(unnamedTag);
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
		List<ResourceLocation> matching = fluidTag.map(
				f -> f.getValues().stream().map(Fluid::getRegistryName).collect(Collectors.toList()),
				l -> l
		);
		out.writeVarInt(matching.size());
		for(ResourceLocation rl : matching)
			out.writeResourceLocation(rl);
		out.writeInt(this.amount);
		out.writeBoolean(this.nbtTag!=null);
		if(this.nbtTag!=null)
			out.writeNbt(this.nbtTag);
	}

	private static TagCollection<Fluid> getTagCollection()
	{
		return SerializationTags.getInstance().getFluids();
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
