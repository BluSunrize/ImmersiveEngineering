/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fluids.FluidStack;

import java.util.regex.Pattern;

public class ItemNBTHelper
{
	public static NBTTagCompound getTag(ItemStack stack)
	{
		if(!stack.hasTagCompound())
			stack.setTagCompound(new NBTTagCompound());
		return stack.getTagCompound();
	}

	public static boolean hasTag(ItemStack stack)
	{
		return stack.hasTagCompound();
	}

	public static boolean hasKey(ItemStack stack, String key)
	{
		return hasTag(stack)&&getTag(stack).hasKey(key);
	}

	public static void remove(ItemStack stack, String key)
	{
		if(hasKey(stack, key))
		{
			getTag(stack).removeTag(key);
			if(getTag(stack).isEmpty())
				stack.setTagCompound(null);
		}
	}


	public static void setInt(ItemStack stack, String key, int val)
	{
		getTag(stack).setInteger(key, val);
	}

	public static void modifyInt(ItemStack stack, String key, int mod)
	{
		modifyInt(getTag(stack), key, mod);
	}

	public static void modifyInt(NBTTagCompound tagCompound, String key, int mod)
	{
		tagCompound.setInteger(key, tagCompound.getInteger(key)+mod);
	}

	public static int getInt(ItemStack stack, String key)
	{
		return hasTag(stack)?getTag(stack).getInteger(key): 0;
	}

	public static void setString(ItemStack stack, String key, String val)
	{
		getTag(stack).setString(key, val);
	}

	public static String getString(ItemStack stack, String key)
	{
		return hasTag(stack)?getTag(stack).getString(key): "";
	}

	public static void setLong(ItemStack stack, String key, long val)
	{
		getTag(stack).setLong(key, val);
	}

	public static long getLong(ItemStack stack, String key)
	{
		return hasTag(stack)?getTag(stack).getLong(key): 0;
	}

	public static void setIntArray(ItemStack stack, String key, int[] val)
	{
		getTag(stack).setIntArray(key, val);
	}

	public static int[] getIntArray(ItemStack stack, String key)
	{
		return hasTag(stack)?getTag(stack).getIntArray(key): new int[0];
	}

	public static void setFloat(ItemStack stack, String key, float val)
	{
		getTag(stack).setFloat(key, val);
	}

	public static void modifyFloat(NBTTagCompound tagCompound, String key, float mod)
	{
		tagCompound.setFloat(key, tagCompound.getFloat(key)+mod);
	}

	public static float getFloat(ItemStack stack, String key)
	{
		return hasTag(stack)?getTag(stack).getFloat(key): 0;
	}

	public static void setBoolean(ItemStack stack, String key, boolean val)
	{
		getTag(stack).setBoolean(key, val);
	}

	public static boolean getBoolean(ItemStack stack, String key)
	{
		return hasTag(stack)&&getTag(stack).getBoolean(key);
	}

	public static void setTagCompound(ItemStack stack, String key, NBTTagCompound val)
	{
		getTag(stack).setTag(key, val);
	}

	public static NBTTagCompound getTagCompound(ItemStack stack, String key)
	{
		return hasTag(stack)?getTag(stack).getCompoundTag(key): new NBTTagCompound();
	}

	public static void setFluidStack(ItemStack stack, String key, FluidStack val)
	{
		if(val!=null&&val.getFluid()!=null)
		{
			setTagCompound(stack, key, val.writeToNBT(new NBTTagCompound()));
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
		getTag(stack).setTag(key, val.writeToNBT(new NBTTagCompound()));
	}

	public static ItemStack getItemStack(ItemStack stack, String key)
	{
		if(hasTag(stack)&&getTag(stack).hasKey(key))
			return new ItemStack(getTagCompound(stack, key));
		return ItemStack.EMPTY;
	}

	public static void setLore(ItemStack stack, String... lore)
	{
		NBTTagCompound displayTag = getTagCompound(stack, "display");
		NBTTagList list = new NBTTagList();
		for(String s : lore)
			list.appendTag(new NBTTagString(s));
		displayTag.setTag("Lore", list);
		setTagCompound(stack, "display", displayTag);
	}

	public static int insertFluxItem(ItemStack container, int energy, int maxEnergy, boolean simulate)
	{
		int stored = getFluxStoredInItem(container);
		int accepted = Math.min(energy, maxEnergy-stored);
		if(!simulate)
		{
			stored += accepted;
			ItemNBTHelper.setInt(container, "energy", stored);
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
			ItemNBTHelper.setInt(container, "energy", stored);
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
					setBoolean(stack, (String)key, (Boolean)value);
				else if(value instanceof Integer)
					setInt(stack, (String)key, (Integer)value);
				else if(value instanceof Float)
					setFloat(stack, (String)key, (Float)value);
				else if(value instanceof Long)
					setLong(stack, (String)key, (Long)value);
				else if(value instanceof String)
					setString(stack, (String)key, (String)value);
				else if(value instanceof NBTTagCompound)
					setTagCompound(stack, (String)key, (NBTTagCompound)value);
				else if(value instanceof int[])
					setIntArray(stack, (String)key, (int[])value);
				else if(value instanceof ItemStack)
					setItemStack(stack, (String)key, (ItemStack)value);
				else if(value instanceof FluidStack)
					setFluidStack(stack, (String)key, (FluidStack)value);
			}
		}
		return stack;
	}

	public static NBTTagCompound combineTags(NBTTagCompound target, NBTTagCompound add, Pattern pattern)
	{
		if(target==null||target.isEmpty())
			return add.copy();
		for(String key : add.getKeySet())
			if(pattern==null||pattern.matcher(key).matches())
				if(!target.hasKey(key))
					target.setTag(key, add.getTag(key));
				else
				{
					switch(add.getTagId(key))
					{
						case 1: //Byte
							target.setByte(key, (byte)(target.getByte(key)+add.getByte(key)));
							break;
						case 2: //Short
							target.setShort(key, (short)(target.getShort(key)+add.getShort(key)));
							break;
						case 3: //Int
							target.setInteger(key, (target.getInteger(key)+add.getInteger(key)));
							break;
						case 4: //Long
							target.setLong(key, (target.getLong(key)+add.getLong(key)));
							break;
						case 5: //Float
							target.setFloat(key, (target.getFloat(key)+add.getFloat(key)));
							break;
						case 6: //Double
							target.setDouble(key, (target.getDouble(key)+add.getDouble(key)));
							break;
						case 7: //ByteArray
							byte[] bytesTarget = target.getByteArray(key);
							byte[] bytesAdd = add.getByteArray(key);
							byte[] bytes = new byte[bytesTarget.length+bytesAdd.length];
							System.arraycopy(bytesTarget, 0, bytes, 0, bytesTarget.length);
							System.arraycopy(bytesAdd, 0, bytes, bytesTarget.length, bytesAdd.length);
							target.setByteArray(key, bytes);
							break;
						case 8: //String
							target.setString(key, (target.getString(key)+add.getString(key)));
							break;
						case 9: //List
							NBTTagList listTarget = (NBTTagList)target.getTag(key);
							NBTTagList listAdd = (NBTTagList)add.getTag(key);
							for(int i = 0; i < listAdd.tagCount(); i++)
								listTarget.appendTag(listAdd.get(i));
							target.setTag(key, listTarget);
							break;
						case 10: //Compound
							combineTags(target.getCompoundTag(key), add.getCompoundTag(key), null);
							break;
						case 11: //IntArray
							int[] intsTarget = target.getIntArray(key);
							int[] intsAdd = add.getIntArray(key);
							int[] ints = new int[intsTarget.length+intsAdd.length];
							System.arraycopy(intsTarget, 0, ints, 0, intsTarget.length);
							System.arraycopy(intsAdd, 0, ints, intsTarget.length, intsAdd.length);
							target.setIntArray(key, ints);
							break;
					}
				}
		return target;
	}
}