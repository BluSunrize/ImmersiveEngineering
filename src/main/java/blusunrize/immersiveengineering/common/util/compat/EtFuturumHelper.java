package blusunrize.immersiveengineering.common.util.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import cpw.mods.fml.common.Loader;

public class EtFuturumHelper
{
	static Class c_EntityLingeringEffect;
	static Method m_IEnergyConnected;
	public static void createLingeringPotionEffect(World world, double x, double y, double z, ItemStack potion, EntityLivingBase thrower)
	{
		if(!Loader.isModLoaded("etfuturum"))
			return;
		try{
			if(c_EntityLingeringEffect==null)
				c_EntityLingeringEffect = Class.forName("ganymedes01.etfuturum.entities.EntityLingeringEffect");
			if(c_EntityLingeringEffect!=null)
			{
				Entity ent = (Entity)c_EntityLingeringEffect.getConstructor(World.class).newInstance(world);
				Field f = c_EntityLingeringEffect.getDeclaredField("thrower");
				f.setAccessible(true);
				f.set(ent, thrower);
				
				f = c_EntityLingeringEffect.getDeclaredField("stack");
				f.setAccessible(true);
				f.set(ent, potion);
				
				ent.setPosition(x, y, z);
				world.spawnEntityInWorld(ent);
			}
		}catch(Exception e){
			e.printStackTrace();
		}

	}
}
