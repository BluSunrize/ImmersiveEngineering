/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.api.crafting;

import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import com.google.common.base.Functions;
import com.google.common.base.Preconditions;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.Arrays;
import java.util.List;

public record StackWithChance(TagOutput stack, float chance, List<ICondition> conditions)
{
	public static final DualCodec<RegistryFriendlyByteBuf, StackWithChance> CODECS = DualCompositeCodecs.composite(
			TagOutput.CODECS.fieldOf("output"), StackWithChance::stack,
			DualCodecs.FLOAT.optionalFieldOf("chance", 1f), StackWithChance::chance,
			IEDualCodecs.CONDITIONS.optionalFieldOf("conditions", List.of()), StackWithChance::conditions,
			StackWithChance::new
	);
	public static final Codec<StackWithChance> OPTIONAL_BASIC_CODEC = Codec.either(CODECS.codec(), TagOutput.CODECS.codec()).xmap(
			either -> either.map(Functions.identity(), tagOutput -> new StackWithChance(tagOutput, 1f)),
			stack -> stack.conditions().isEmpty()&&stack.chance() >= 1?Either.right(stack.stack()): Either.left(stack)
	);

	public static final StreamCodec<RegistryFriendlyByteBuf, List<StackWithChance>> STREAM_LIST = CODECS.streamCodec().apply(ByteBufCodecs.list());

	public StackWithChance
	{
		Preconditions.checkNotNull(stack);
	}

	public StackWithChance(ItemStack stack, float chance)
	{
		this(new TagOutput(stack), chance, List.of());
	}

	public StackWithChance(TagOutput stack, float chance, ICondition... conditions)
	{
		this(stack, chance, Arrays.asList(conditions));
	}

	public CompoundTag writeToNBT(HolderLookup.Provider provider)
	{
		CompoundTag compoundNBT = new CompoundTag();
		compoundNBT.put("stack", stack.get().save(provider));
		compoundNBT.putFloat("chance", chance);
		return compoundNBT;
	}

	public static StackWithChance readFromNBT(HolderLookup.Provider provider, CompoundTag compoundNBT)
	{
		Preconditions.checkNotNull(compoundNBT);
		Preconditions.checkArgument(compoundNBT.contains("chance"));
		Preconditions.checkArgument(compoundNBT.contains("stack"));
		final ItemStack stack = ItemStack.parse(provider, compoundNBT.getCompound("stack")).orElseThrow();
		final float chance = compoundNBT.getFloat("chance");
		return new StackWithChance(stack, chance);
	}

	public StackWithChance recalculate(double totalChance)
	{
		return new StackWithChance(this.stack, (float)(this.chance/totalChance), this.conditions);
	}
}
