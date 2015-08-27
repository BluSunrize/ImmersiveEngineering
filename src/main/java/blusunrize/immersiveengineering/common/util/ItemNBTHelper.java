package blusunrize.immersiveengineering.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

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
		return hasTag(stack) ? getTag(stack).hasKey(key) : false;
	}

	public static void remove(ItemStack stack, String key)
	{
		if(hasKey(stack, key))
		{
			getTag(stack).removeTag(key);
			if(getTag(stack).hasNoTags())
				stack.setTagCompound(null);
		}
	}


	public static void setInt(ItemStack stack, String key, int val)
	{
		getTag(stack).setInteger(key, val);
	}
	public static void modifyInt(ItemStack stack, String key, int mod)
	{
		getTag(stack).setInteger(key, getTag(stack).getInteger(key)+mod);
	}
	public static int getInt(ItemStack stack, String key)
	{
		return hasTag(stack) ? getTag(stack).getInteger(key) : 0;
	}

	public static void setString(ItemStack stack, String key, String val)
	{
		getTag(stack).setString(key, val);
	}
	public static String getString(ItemStack stack, String key)
	{
		return hasTag(stack) ? getTag(stack).getString(key) : "";
	}

	public static void setLong(ItemStack stack, String key, long val)
	{
		getTag(stack).setLong(key, val);
	}
	public static long getLong(ItemStack stack, String key)
	{
		return hasTag(stack) ? getTag(stack).getLong(key) : 0;
	}

	public static void setIntArray(ItemStack stack, String key, int[] val)
	{
		getTag(stack).setIntArray(key, val);
	}
	public static int[] getIntArray(ItemStack stack, String key)
	{
		return hasTag(stack) ? getTag(stack).getIntArray(key) : new int[0];
	}

	public static void setFloat(ItemStack stack, String key, float val)
	{
		getTag(stack).setFloat(key, val);
	}
	public static float getFloat(ItemStack stack, String key)
	{
		return hasTag(stack) ? getTag(stack).getFloat(key) : 0;
	}

	public static void setBoolean(ItemStack stack, String key, boolean val)
	{
		getTag(stack).setBoolean(key, val);
	}
	public static boolean getBoolean(ItemStack stack, String key)
	{
		return hasTag(stack) ? getTag(stack).getBoolean(key) : false;
	}

	public static void setTagCompound(ItemStack stack, String key, NBTTagCompound val)
	{
		getTag(stack).setTag(key, val);
	}
	public static NBTTagCompound getTagCompound(ItemStack stack, String key)
	{
		return hasTag(stack) ? getTag(stack).getCompoundTag(key) : new NBTTagCompound();
	}

	public static void setDelayedSoundsForStack(ItemStack stack, String nbtKey, String sound, float volume, float pitch, int amount, int baseDelay, int iDelay)
	{
		int[] delayedSounds = new int[amount];
		for(int i=0; i<delayedSounds.length; i++)
			delayedSounds[i]=baseDelay+i*iDelay;

		setIntArray(stack, "delayedSound_"+nbtKey+"_delay", delayedSounds);
		setString(stack, "delayedSound_"+nbtKey+"_sound", sound);
		setFloat(stack, "delayedSound_"+nbtKey+"_volume", volume);
		setFloat(stack, "delayedSound_"+nbtKey+"_pitch", pitch);
	}
	public static int handleDelayedSoundsForStack(ItemStack stack, String nbtKey, Entity ent)
	{
		if(!hasKey(stack, "delayedSound_"+nbtKey+"_delay"))
			return -1;
		int[] delayedSounds = ItemNBTHelper.getIntArray(stack, "delayedSound_"+nbtKey+"_delay");
		int l = 0;
		for(int i=0; i<delayedSounds.length; i++)
		{
			--delayedSounds[i];
			if(delayedSounds[i]<=0)
			{
				ent.worldObj.playSoundAtEntity(ent, getString(stack, "delayedSound_"+nbtKey+"_sound"), getFloat(stack, "delayedSound_"+nbtKey+"_volume"), getFloat(stack, "delayedSound_"+nbtKey+"_pitch"));
			}
			else
				++l;
		}
		if(l>0)
		{
			ItemNBTHelper.setIntArray(stack, "delayedSound_"+nbtKey+"_delay", delayedSounds);
		}
		else
		{
			ItemNBTHelper.remove(stack, "delayedSound_"+nbtKey+"_delay");
			ItemNBTHelper.remove(stack, "delayedSound_"+nbtKey+"_sound");
			ItemNBTHelper.remove(stack, "delayedSound_"+nbtKey+"_volume");
			ItemNBTHelper.remove(stack, "delayedSound_"+nbtKey+"_pitch");
		}
		return l;
	}

	public static void setFluidStack(ItemStack stack, String key, FluidStack val)
	{
		if(val!=null && val.getFluid()!=null)
		{
			NBTTagCompound tag = getTagCompound(stack, key);
			tag.setString("fluid", val.getFluid().getName());
			tag.setInteger("amount", val.amount);
			setTagCompound(stack, key, tag);
		}
	}
	public static FluidStack getFluidStack(ItemStack stack, String key)
	{
		if(hasTag(stack))
		{
			NBTTagCompound tag = getTagCompound(stack, key);
			String name = tag.getString("fluid");
			int amount = tag.getInteger("amount");
			if(FluidRegistry.getFluid(name)!=null)
				return new FluidStack(FluidRegistry.getFluid(name), amount);
		}
		return null;
	}

	public static void setItemStack(ItemStack stack, String key, ItemStack val)
	{
		getTag(stack).setTag(key, val.writeToNBT(new NBTTagCompound()));
	}
	public static ItemStack getItemStack(ItemStack stack, String key)
	{		
		if(hasTag(stack) && getTag(stack).hasKey(key))
			return ItemStack.loadItemStackFromNBT(getTagCompound(stack, key));
		return null;
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
}