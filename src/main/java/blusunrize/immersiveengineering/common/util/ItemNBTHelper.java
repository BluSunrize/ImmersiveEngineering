/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants.NBT;
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
		if(hasKey(stack, key))
		{
			CompoundNBT tag = stack.getOrCreateTag();
			tag.remove(key);
			if(tag.isEmpty())
				stack.setTag(null);
		}
	}


	public static void putInt(ItemStack stack, String key, int val)
	{
		stack.getOrCreateTag().putInt(key, val);
	}

	public static void modifyInt(ItemStack stack, String key, int mod)
	{
		modifyInt(stack.getOrCreateTag(), key, mod);
	}

	public static void modifyInt(CompoundNBT tagCompound, String key, int mod)
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

	public static void modifyFloat(CompoundNBT tagCompound, String key, float mod)
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

	public static void setTagCompound(ItemStack stack, String key, CompoundNBT val)
	{
		stack.getOrCreateTag().put(key, val);
	}

	public static CompoundNBT getTagCompound(ItemStack stack, String key)
	{
		return hasTag(stack)?stack.getOrCreateTag().getCompound(key): new CompoundNBT();
	}

	public static void setFluidStack(ItemStack stack, String key, FluidStack val)
	{
		if(val!=null&&val.getFluid()!=null)
		{
			setTagCompound(stack, key, val.writeToNBT(new CompoundNBT()));
		}
		else
			remove(stack, key);
	}

	public static FluidStack getFluidStack(ItemStack stack, String key)
	{
		if(hasTag(stack))
		{
			return FluidStack.loadFluidStackFromNBT(getTagCompound(stack, key));
		}
		return null;
	}

	public static void setItemStack(ItemStack stack, String key, ItemStack val)
	{
		stack.getOrCreateTag().put(key, val.write(new CompoundNBT()));
	}

	public static ItemStack getItemStack(ItemStack stack, String key)
	{
		if(hasTag(stack)&&stack.getOrCreateTag().contains(key))
			return ItemStack.read(getTagCompound(stack, key));
		return ItemStack.EMPTY;
	}

	public static void setLore(ItemStack stack, ITextComponent... lore)
	{
		CompoundNBT displayTag = getTagCompound(stack, "display");
		ListNBT list = new ListNBT();
		for(ITextComponent s : lore)
			list.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(s)));
		displayTag.put("Lore", list);
		setTagCompound(stack, "display", displayTag);
	}

	public static int insertFluxItem(ItemStack container, int energy, int maxEnergy, boolean simulate)
	{
		int stored = getFluxStoredInItem(container);
		int accepted = Math.min(energy, maxEnergy-stored);
		if(!simulate)
		{
			stored += accepted;
			ItemNBTHelper.putInt(container, "energy", stored);
		}
		return accepted;
	}

	public static int extractFluxFromItem(ItemStack container, int energy, boolean simulate)
	{
		int stored = getFluxStoredInItem(container);
		int extracted = Math.min(energy, stored);
		if(!simulate)
		{
			stored -= extracted;
			ItemNBTHelper.putInt(container, "energy", stored);
		}
		return extracted;
	}

	public static int getFluxStoredInItem(ItemStack container)
	{
		return getInt(container, "energy");
	}

	public static ItemStack stackWithData(ItemStack stack, Object... data)
	{
		assert (data.length%2==0);
		for(int i = 0; i < data.length/2; i++)
		{
			Object key = data[i];
			Object value = data[i+1];
			if(key instanceof String)
			{
				if(value instanceof Boolean)
					putBoolean(stack, (String)key, (Boolean)value);
				else if(value instanceof Integer)
					putInt(stack, (String)key, (Integer)value);
				else if(value instanceof Float)
					putFloat(stack, (String)key, (Float)value);
				else if(value instanceof Long)
					putLong(stack, (String)key, (Long)value);
				else if(value instanceof String)
					putString(stack, (String)key, (String)value);
				else if(value instanceof CompoundNBT)
					setTagCompound(stack, (String)key, (CompoundNBT)value);
				else if(value instanceof int[])
					putIntArray(stack, (String)key, (int[])value);
				else if(value instanceof ItemStack)
					setItemStack(stack, (String)key, (ItemStack)value);
				else if(value instanceof FluidStack)
					setFluidStack(stack, (String)key, (FluidStack)value);
			}
		}
		return stack;
	}

	public static CompoundNBT combineTags(CompoundNBT target, CompoundNBT add, Pattern pattern)
	{
		if(target==null||target.isEmpty())
			return add.copy();
		for(String key : add.keySet())
			if(pattern==null||pattern.matcher(key).matches())
				if(!target.contains(key))
					target.put(key, add.get(key));
				else
				{
					switch(add.getTagId(key))
					{
						case NBT.TAG_BYTE:
							target.putByte(key, (byte)(target.getByte(key)+add.getByte(key)));
							break;
						case NBT.TAG_SHORT:
							target.putShort(key, (short)(target.getShort(key)+add.getShort(key)));
							break;
						case NBT.TAG_INT:
							target.putInt(key, (target.getInt(key)+add.getInt(key)));
							break;
						case NBT.TAG_LONG:
							target.putLong(key, (target.getLong(key)+add.getLong(key)));
							break;
						case NBT.TAG_FLOAT:
							target.putFloat(key, (target.getFloat(key)+add.getFloat(key)));
							break;
						case NBT.TAG_DOUBLE:
							target.putDouble(key, (target.getDouble(key)+add.getDouble(key)));
							break;
						case NBT.TAG_BYTE_ARRAY:
							byte[] bytesTarget = target.getByteArray(key);
							byte[] bytesAdd = add.getByteArray(key);
							byte[] bytes = new byte[bytesTarget.length+bytesAdd.length];
							System.arraycopy(bytesTarget, 0, bytes, 0, bytesTarget.length);
							System.arraycopy(bytesAdd, 0, bytes, bytesTarget.length, bytesAdd.length);
							target.putByteArray(key, bytes);
							break;
						case NBT.TAG_STRING:
							target.putString(key, (target.getString(key)+add.getString(key)));
							break;
						case NBT.TAG_LIST:
							ListNBT listTarget = (ListNBT)target.get(key);
							ListNBT listAdd = (ListNBT)add.get(key);
							for(int i = 0; i < listAdd.size(); i++)
								listTarget.add(listAdd.get(i));
							target.put(key, listTarget);
							break;
						case NBT.TAG_COMPOUND:
							combineTags(target.getCompound(key), add.getCompound(key), null);
							break;
						case NBT.TAG_INT_ARRAY:
							int[] intsTarget = target.getIntArray(key);
							int[] intsAdd = add.getIntArray(key);
							int[] ints = new int[intsTarget.length+intsAdd.length];
							System.arraycopy(intsTarget, 0, ints, 0, intsTarget.length);
							System.arraycopy(intsAdd, 0, ints, intsTarget.length, intsAdd.length);
							target.putIntArray(key, ints);
							break;
					}
				}
		return target;
	}
}