package blusunrize.immersiveengineering.common.util;

import com.emoniph.witchery.util.EntityDamageSourceIndirectSilver;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.IChatComponent;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;

public class IEDamageSources extends EntityDamageSourceIndirect
{
	public IEDamageSources(String tag, Entity shot, Entity shooter)
	{
		super(tag, shot, shooter);
	}


	public static DamageSource causeCasullDamage(EntityRevolvershot shot, Entity shooter)
	{
		return (new IEDamageSources(Lib.DMG_RevolverCasull, shot, shooter)).setProjectile();
	}
	public static DamageSource causePiercingDamage(EntityRevolvershot shot, Entity shooter)
	{
		return (new IEDamageSources(Lib.DMG_RevolverAP, shot, shooter)).setDamageBypassesArmor().setProjectile();
	}
	public static DamageSource causeBuckshotDamage(EntityRevolvershot shot, Entity shooter)
	{
		return (new IEDamageSources(Lib.DMG_RevolverBuck, shot, shooter)).setProjectile();
	}
	public static DamageSource causeDragonsbreathDamage(EntityRevolvershot shot, Entity shooter)
	{
		return (new IEDamageSources(Lib.DMG_RevolverDragon, shot, shooter)).setFireDamage().setProjectile();
	}
	public static DamageSource causeHomingDamage(EntityRevolvershot shot, Entity shooter)
	{
		return (new IEDamageSources(Lib.DMG_RevolverHoming, shot, shooter)).setProjectile();
	}
	public static DamageSource causeWolfpackDamage(EntityRevolvershot shot, Entity shooter)
	{
		return (new IEDamageSources(Lib.DMG_RevolverWolfpack, shot, shooter)).setProjectile();
	}
	public static DamageSource causeSilverDamage(EntityRevolvershot shot, Entity shooter)
	{
		EntityDamageSourceIndirectSilver silver = new EntityDamageSourceIndirectSilver(shot, shooter);
		silver.setProjectile();
		silver.damageType = Lib.DMG_RevolverSilver;
		return silver;
	}
	public static DamageSource causePotionDamage(EntityRevolvershot shot, EntityLivingBase shooter)
	{
		return (new IEDamageSources(Lib.DMG_RevolverPotion, shot, shooter)).setProjectile();
	}
	
	public static DamageSource causeCrusherDamage()
	{
		return new CrusherDamage();
	}

	public static class CrusherDamage extends DamageSource
	{
		public CrusherDamage()
		{
			super(Lib.DMG_Crusher);
		}
		
		@Override
		public IChatComponent func_151519_b(EntityLivingBase p_151519_1_)
		{
//			return new ChatComponentText(null);
			return super.func_151519_b(p_151519_1_);
		}
	}
}