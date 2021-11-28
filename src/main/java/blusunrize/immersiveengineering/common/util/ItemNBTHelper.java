/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.utils.ItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.regex.Pattern;

public class ItemNBTHelper
{
	public static boolean hasTag(ItemStack stack)
	{
		return stack.hasTag();
	}

	public static boolean hasKey(ItemStack stack, String key)
	{
		return hasTag(stack)&&stack.getOrCreateTag().contains(key);
	}

	public static boolean hasKey(ItemStack stack, String key, int type)
	{
		return hasTag(stack)&&stack.getOrCreateTag().contains(key, type);
	}

	public static void remove(ItemStack stack, String key)
	{
		ItemUtils.removeTag(stack, key);
	}


	public static void putInt(ItemStack stack, String key, int val)
	{
		stack.getOrCreateTag().putInt(key, val);
	}

	public static void modifyInt(CompoundTag tagCompound, String key, int mod)
	{
		tagCompound.putInt(key, tagCompound.getInt(key)+mod);
	}

	public static int getInt(ItemStack stack, String key)
	{
		return hasTag(stack)?stack.getOrCreateTag().getInt(key): 0;
	}

	public static void putString(ItemStack stack, String key, String val)
	{
		stack.getOrCreateTag().putString(key, val);
	}

	public static String getString(ItemStack stack, String key)
	{
		return hasTag(stack)?stack.getOrCreateTag().getString(key): "";
	}

	public static void putLong(ItemStack stack, String key, long val)
	{
		stack.getOrCreateTag().putLong(key, val);
	}

	public static long getLong(ItemStack stack, String key)
	{
		return hasTag(stack)?stack.getOrCreateTag().getLong(key): 0;
	}

	public static void putIntArray(ItemStack stack, String key, int[] val)
	{
		stack.getOrCreateTag().putIntArray(key, val);
	}

	public static int[] getIntArray(ItemStack stack, String key)
	{
		return hasTag(stack)?stack.getOrCreateTag().getIntArray(key): new int[0];
	}

	public static void putFloat(ItemStack stack, String key, float val)
	{
		stack.getOrCreateTag().putFloat(key, val);
	}

	public static void modifyFloat(CompoundTag tagCompound, String key, float mod)
	{
		tagCompound.putFloat(key, tagCompound.getFloat(key)+mod);
	}

	public static float getFloat(ItemStack stack, String key)
	{
		return hasTag(stack)?stack.getOrCreateTag().getFloat(key): 0;
	}

	public static void putBoolean(ItemStack stack, String key, boolean val)
	{
		stack.getOrCreateTag().putBoolean(key, val);
	}

	public static boolean getBoolean(ItemStack stack, String key)
	{
		return hasTag(stack)&&stack.getOrCreateTag().getBoolean(key);
	}

	public static void setTagCompound(ItemStack stack, String key, CompoundTag val)
	{
		stack.getOrCreateTag().put(key, val);
	}

	public static CompoundTag getTagCompound(ItemStack stack, String key)
	{
		return hasTag(stack)?stack.getOrCreateTag().getCompound(key): new CompoundTag();
	}

	public static void setFluidStack(ItemStack stack, String key, FluidStack val)
	{
		if(val!=null&&val.getFluid()!=null)
			setTagCompound(stack, key, val.writeToNBT(new CompoundTag()));
		else
			remove(stack, key);
	}

	public static FluidStack getFluidStack(ItemStack stack, String key)
	{
		if(hasTag(stack))
			return FluidStack.loadFluidStackFromNBT(getTagCompound(stack, key));
		return null;
	}

	public static void setItemStack(ItemStack stack, String key, ItemStack val)
	{
		stack.getOrCreateTag().put(key, val.save(new CompoundTag()));
	}

	public static ItemStack getItemStack(ItemStack stack, String key)
	{
		if(hasTag(stack)&&stack.getOrCreateTag().contains(key))
			return ItemStack.of(getTagCompound(stack, key));
		return ItemStack.EMPTY;
	}

	public static void setLore(ItemStack stack, Component... lore)
	{
		CompoundTag displayTag = getTagCompound(stack, "display");
		ListTag list = new ListTag();
		for(Component s : lore)
			list.add(StringTag.valueOf(Component.Serializer.toJson(s)));
		displayTag.put("Lore", list);
		setTagCompound(stack, "display", displayTag);
	}

	public static CompoundTag combineTags(CompoundTag target, CompoundTag add, Pattern pattern)
	{
		if(target==null||target.isEmpty())
			return add.copy();
		for(String key : add.getAllKeys())
			if(pattern==null||pattern.matcher(key).matches())
				if(!target.contains(key))
					target.put(key, add.get(key));
				else
				{
					switch(add.getTagType(key))
					{
						case Tag.TAG_BYTE -> target.putByte(key, (byte)(target.getByte(key)+add.getByte(key)));
						case Tag.TAG_SHORT -> target.putShort(key, (short)(target.getShort(key)+add.getShort(key)));
						case Tag.TAG_INT -> target.putInt(key, (target.getInt(key)+add.getInt(key)));
						case Tag.TAG_LONG -> target.putLong(key, (target.getLong(key)+add.getLong(key)));
						case Tag.TAG_FLOAT -> target.putFloat(key, (target.getFloat(key)+add.getFloat(key)));
						case Tag.TAG_DOUBLE -> target.putDouble(key, (target.getDouble(key)+add.getDouble(key)));
						case Tag.TAG_BYTE_ARRAY -> {
							byte[] bytesTarget = target.getByteArray(key);
							byte[] bytesAdd = add.getByteArray(key);
							byte[] bytes = new byte[bytesTarget.length+bytesAdd.length];
							System.arraycopy(bytesTarget, 0, bytes, 0, bytesTarget.length);
							System.arraycopy(bytesAdd, 0, bytes, bytesTarget.length, bytesAdd.length);
							target.putByteArray(key, bytes);
						}
						case Tag.TAG_STRING -> target.putString(key, (target.getString(key)+add.getString(key)));
						case Tag.TAG_LIST -> {
							ListTag listTarget = (ListTag)target.get(key);
							ListTag listAdd = (ListTag)add.get(key);
							listTarget.addAll(listAdd);
							target.put(key, listTarget);
						}
						case Tag.TAG_COMPOUND -> combineTags(target.getCompound(key), add.getCompound(key), null);
						case Tag.TAG_INT_ARRAY -> {
							int[] intsTarget = target.getIntArray(key);
							int[] intsAdd = add.getIntArray(key);
							int[] ints = new int[intsTarget.length+intsAdd.length];
							System.arraycopy(intsTarget, 0, ints, 0, intsTarget.length);
							System.arraycopy(intsAdd, 0, ints, intsTarget.length, intsAdd.length);
							target.putIntArray(key, ints);
						}
					}
				}
		return target;
	}
}