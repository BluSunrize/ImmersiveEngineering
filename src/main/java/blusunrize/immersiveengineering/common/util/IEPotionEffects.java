package blusunrize.immersiveengineering.common.util;

import java.lang.reflect.Field;

import net.minecraft.potion.Potion;

public class IEPotionEffects
{
	public static void init()
	{
		//Oil coated potion goes here
	}
	
	public static void extendPotionArray(int extendBy)
	{
		IELogger.info("Attempting to extend PotionArray by "+extendBy);
		Potion[] potions = new Potion[Potion.potionTypes.length + extendBy];
		for (int i = 0; i < Potion.potionTypes.length; i++)
			potions[i] = Potion.potionTypes[i];
		try
		{
			Field field = null;
			Field[] fields = Potion.class.getDeclaredFields();
			for (Field f : fields)
				if (f.getType().toString().equals("class [Lnet.minecraft.potion.Potion;"))
				{
					field = f;
					break;
				}

			field.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & 0xFFFFFFEF);
			field.set(null, potions);
			IELogger.info("Variable "+Potion.potionTypes.length);
			IELogger.info("Reflection "+((Potion[])Potion.class.getFields()[0].get(null)).length);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public static int getNextPotionId(int start)
	{
		if((Potion.potionTypes != null) && (start > 0) && (start < Potion.potionTypes.length) && (Potion.potionTypes[start] == null))
			return start;
		start++;
		if(start < 256)
			start = getNextPotionId(start);
		else
			start = -1;
		return start;
	}
}