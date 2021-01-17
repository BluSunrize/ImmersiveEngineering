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
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;

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

	public CompoundNBT writeToNBT()
	{
		CompoundNBT compoundNBT = new CompoundNBT();
		compoundNBT.put("stack", stack.write(new CompoundNBT()));
		compoundNBT.putFloat("chance", chance);
		return compoundNBT;
	}

	public static StackWithChance readFromNBT(CompoundNBT compoundNBT)
	{
		Preconditions.checkNotNull(compoundNBT);
		Preconditions.checkArgument(compoundNBT.contains("chance"));
		Preconditions.checkArgument(compoundNBT.contains("stack"));
		final ItemStack stack = ItemStack.read(compoundNBT.getCompound("stack"));
		final float chance = compoundNBT.getFloat("chance");
		return new StackWithChance(stack, chance);
	}

	public void write(PacketBuffer buffer)
	{
		buffer.writeItemStack(this.stack);
		buffer.writeFloat(this.chance);
	}

	public static StackWithChance read(PacketBuffer buffer)
	{
		return new StackWithChance(buffer.readItemStack(), buffer.readFloat());
	}

	public StackWithChance recalculate(float totalChance)
	{
		return new StackWithChance(this.stack, this.chance/totalChance);
	}
}
