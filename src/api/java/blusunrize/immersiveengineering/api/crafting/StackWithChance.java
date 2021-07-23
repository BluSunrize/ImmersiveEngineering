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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class StackWithChance
{
	private final ItemStack stack;
	private final float chance;

	public StackWithChance(ItemStack stack, float chance)
	{
		Preconditions.checkNotNull(stack);
		this.stack = stack;
		this.chance = chance;
	}

	public ItemStack getStack()
	{
		return stack;
	}

	public float getChance()
	{
		return chance;
	}

	public CompoundTag writeToNBT()
	{
		CompoundTag compoundNBT = new CompoundTag();
		compoundNBT.put("stack", stack.save(new CompoundTag()));
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
		buffer.writeItem(this.stack);
		buffer.writeFloat(this.chance);
	}

	public static StackWithChance read(FriendlyByteBuf buffer)
	{
		return new StackWithChance(buffer.readItem(), buffer.readFloat());
	}

	public StackWithChance recalculate(float totalChance)
	{
		return new StackWithChance(this.stack, this.chance/totalChance);
	}
}
