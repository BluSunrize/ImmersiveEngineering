package blusunrize.immersiveengineering.common.util;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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
//				System.out.println("sounds "+nbtKey);
		int[] delayedSounds = ItemNBTHelper.getIntArray(stack, "delayedSound_"+nbtKey+"_delay");
		int l = 0;
		for(int i=0; i<delayedSounds.length; i++)
		{
//			System.out.println(nbtKey+", "+i+" pre: "+delayedSounds[i]);
			--delayedSounds[i];
//			System.out.println(nbtKey+", "+i+" post: "+delayedSounds[i]);
			if(delayedSounds[i]<=0)
			{
				System.out.println("play sound "+getString(stack, "delayedSound_"+nbtKey+"_sound"));
				ent.worldObj.playSoundAtEntity(ent, getString(stack, "delayedSound_"+nbtKey+"_sound"), getFloat(stack, "delayedSound_"+nbtKey+"_volume"), getFloat(stack, "delayedSound_"+nbtKey+"_pitch"));
			}
			else
				++l;
		}
//		for(int i:delayedSounds)
//			System.out.println(" new "+i);
		if(l>0)
		{
//			System.out.println(nbtKey+" resetArray");
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

	public static void setBoolean(ItemStack stack, String key, boolean val)
	{
		getTag(stack).setBoolean(key, val);
	}
	public static boolean getBoolean(ItemStack stack, String key)
	{
		return hasTag(stack) ? getTag(stack).getBoolean(key) : false;
	}
}