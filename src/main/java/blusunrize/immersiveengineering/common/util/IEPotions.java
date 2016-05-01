package blusunrize.immersiveengineering.common.util;

import java.lang.reflect.Field;
import java.util.UUID;

import blusunrize.immersiveengineering.api.IEApi;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class IEPotions
{
	public static Potion flammable;
	public static Potion slippery;
	public static Potion conductive;
	public static Potion sticky;

	public static void init()
	{
		long potionUUIDBase = 109406000905L;

		flammable = new IEPotion(new ResourceLocation("ie.flammable"), true,0x8f3f1f,0, false,0).setPotionName("immersiveengineering.potion.flammable");
		slippery = new IEPotion(new ResourceLocation("ie.slippery"), true,0x171003,0, false,1).setPotionName("immersiveengineering.potion.slippery");
		conductive = new IEPotion(new ResourceLocation("ie.conductive"), true,0x690000,0, false,2).setPotionName("immersiveengineering.potion.conductive");
		sticky = new IEPotion(new ResourceLocation("ie.sticky"), true,0x9c6800,0, false,3).setPotionName("immersiveengineering.potion.sticky").registerPotionAttributeModifier(SharedMonsterAttributes.movementSpeed, new UUID(potionUUIDBase,01L).toString(), -0.50000000298023224D, 2);

		IEApi.potions = new Potion[]{flammable,slippery,conductive,sticky};
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
			//			IELogger.info("Variable "+Potion.potionTypes.length);
			//			IELogger.info("Reflection "+((Potion[])Potion.class.getFields()[0].get(null)).length);
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

	public static class IEPotion extends Potion
	{
		static ResourceLocation tex = new ResourceLocation("immersiveengineering","textures/gui/potioneffects.png");
		final int tickrate;
		final boolean halfTickRateWIthAmplifier;
		public IEPotion(ResourceLocation resource, boolean isBad, int colour, int tick, boolean halveTick, int icon)
		{
			super(resource, isBad, colour);
			this.tickrate = tick;
			this.halfTickRateWIthAmplifier = halveTick;
			this.setIconIndex(icon%8, icon/8);
		}

		@Override
		public int getStatusIconIndex()
		{
			Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
			return super.getStatusIconIndex();
		}
		@Override
		public boolean isReady(int duration, int amplifier)
		{
			if(tickrate<0)
				return false;
			int k = tickrate >> amplifier;
			return k>0 ? duration%k == 0 : true;
		}
		@Override
		public void performEffect(EntityLivingBase living, int amplifier)
		{
			if(this==IEPotions.slippery)
			{
				if(living.onGround)
					living.moveFlying(0,1, 0.005F);
				if(!living.worldObj.isRemote && living.getRNG().nextInt(300)==0 && living.getEquipmentInSlot(0)!=null)
				{
					EntityItem dropped = living.entityDropItem(living.getEquipmentInSlot(0).copy(), 1);
					dropped.setPickupDelay(20);
					living.setCurrentItemOrArmor(0, null);
				}
			}
		}
	}
}