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
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.conditions.ICondition;

import java.util.List;

public record StackWithChance(TagOutput stack, float chance, List<ICondition> conditions)
{
	public static final Codec<StackWithChance> CODEC = RecordCodecBuilder.create(
			inst -> inst.group(
					TagOutput.CODEC.fieldOf("output").forGetter(StackWithChance::stack),
					Codec.FLOAT.fieldOf("chance").forGetter(StackWithChance::chance),
					ICondition.LIST_CODEC.fieldOf("conditions").forGetter(StackWithChance::conditions)
			).apply(inst, StackWithChance::new)
	);

	public StackWithChance
	{
		Preconditions.checkNotNull(stack);
	}

	public StackWithChance(ItemStack stack, float chance)
	{
		this(new TagOutput(stack), chance, List.of());
	}

	public CompoundTag writeToNBT()
	{
		CompoundTag compoundNBT = new CompoundTag();
		compoundNBT.put("stack", stack.get().save(new CompoundTag()));
		compoundNBT.putFloat("chance", chance);
		return compoundNBT;
	}

	public static StackWithChance readFromNBT(CompoundTag compoundNBT)
	{
		Preconditions.checkNotNull(compoundNBT);
		Preconditions.checkArgument(compoundNBT.contains("chance"));
		Preconditions.checkArgument(compoundNBT.contains("stack"));
		final ItemStack stack = ItemStack.of(compoundNBT.getCompound("stack"));
		final float chance = compoundNBT.getFloat("chance");
		return new StackWithChance(stack, chance);
	}

	public void write(FriendlyByteBuf buffer)
	{
		buffer.writeItem(this.stack.get());
		buffer.writeFloat(this.chance);
	}

	public static StackWithChance read(FriendlyByteBuf buffer)
	{
		return new StackWithChance(buffer.readItem(), buffer.readFloat());
	}

	public StackWithChance recalculate(double totalChance)
	{
		return new StackWithChance(this.stack, (float)(this.chance/totalChance), this.conditions);
	}
}
