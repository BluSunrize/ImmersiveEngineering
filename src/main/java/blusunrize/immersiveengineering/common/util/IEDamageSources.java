/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.energy.wires.WireType;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.common.entities.EntityRailgunShot;
import blusunrize.immersiveengineering.common.entities.EntityRevolvershot;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

public class IEDamageSources
{
	public static class IEDamageSource_Indirect extends EntityDamageSourceIndirect
	{
		public IEDamageSource_Indirect(String tag, Entity shot, Entity shooter)
		{
			super(tag, shot, shooter);
		}
	}

	public static class IEDamageSource_Direct extends EntityDamageSource
	{
		public IEDamageSource_Direct(String tag, Entity attacker)
		{
			super(tag, attacker);
		}
	}

	public static class IEDamageSource extends DamageSource
	{
		public IEDamageSource(String tag)
		{
			super(tag);
		}
	}

	public static class ElectricDamageSource extends DamageSource
	{
		public IElectricEquipment.ElectricSource source;
		public float dmg;

		public ElectricDamageSource(String tag, IElectricEquipment.ElectricSource source, float amount)
		{
			super(tag);
			this.source = source;
			dmg = amount;
			setDamageBypassesArmor();
		}

		public boolean apply(Entity e)
		{
			if(e instanceof EntityLivingBase)
				IElectricEquipment.applyToEntity((EntityLivingBase)e, this, source);
			if(dmg > 0)
				e.attackEntityFrom(this, dmg);
			return dmg > 0;
		}
	}

	public static class TurretDamageSource extends IEDamageSource
	{
		public TurretDamageSource(String damageTypeIn)
		{
			super(damageTypeIn);
		}

		@Override
		public ITextComponent getDeathMessage(EntityLivingBase entityLivingBaseIn)
		{
			String s = "death.attack."+this.damageType+".turret";
			return new TextComponentTranslation(s, entityLivingBaseIn.getDisplayName());
		}
	}

	public static DamageSource causeCasullDamage(EntityRevolvershot shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverCasull);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverCasull, shot, shooter);
	}

	public static DamageSource causePiercingDamage(EntityRevolvershot shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverAP).setDamageBypassesArmor();
		return new IEDamageSource_Indirect(Lib.DMG_RevolverAP, shot, shooter).setDamageBypassesArmor();
	}

	public static DamageSource causeBuckshotDamage(EntityRevolvershot shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverBuck);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverBuck, shot, shooter);
	}

	public static DamageSource causeDragonsbreathDamage(EntityRevolvershot shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverDragon).setFireDamage();
		return new IEDamageSource_Indirect(Lib.DMG_RevolverDragon, shot, shooter).setFireDamage();
	}

	public static DamageSource causeHomingDamage(EntityRevolvershot shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverHoming);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverHoming, shot, shooter);
	}

	public static DamageSource causeWolfpackDamage(EntityRevolvershot shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverWolfpack);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverWolfpack, shot, shooter);
	}

	public static DamageSource causeSilverDamage(EntityRevolvershot shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverSilver);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverSilver, shot, shooter);
	}

	public static DamageSource causePotionDamage(EntityRevolvershot shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverPotion);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverPotion, shot, shooter);
	}

	public static DamageSource acid = new IEDamageSource(Lib.DMG_Acid);

	public static DamageSource crusher = new IEDamageSource(Lib.DMG_Crusher);

	public static DamageSource razorWire = new IEDamageSource(Lib.DMG_RazorWire);

	public static DamageSource razorShock = new IEDamageSource(Lib.DMG_RazorShock);

	// DO NOT USE EXCEPT FOR CHECKING WHETHER AN ENTITY IS VULNERABLE
	public static DamageSource wireShock = new ElectricDamageSource(Lib.DMG_WireShock, WireType.COPPER.getElectricSource(), 1);

	private static final IElectricEquipment.ElectricSource TC_LOW = new IElectricEquipment.ElectricSource(.25F);
	private static final IElectricEquipment.ElectricSource TC_HIGH = new IElectricEquipment.ElectricSource(2);

	public static ElectricDamageSource causeTeslaDamage(float amount, boolean lowPower)
	{
		return new ElectricDamageSource(Lib.DMG_Tesla, lowPower?TC_LOW: TC_HIGH, amount);
	}

	public static ElectricDamageSource causeWireDamage(float amount, IElectricEquipment.ElectricSource source)
	{
		return new ElectricDamageSource(Lib.DMG_WireShock, source, amount);
	}

	public static DamageSource causeRailgunDamage(EntityRailgunShot shot, Entity shooter)
	{
		return new IEDamageSource_Indirect(Lib.DMG_Railgun, shot, shooter).setDamageBypassesArmor();
	}

	public static DamageSource causeTeslaPrimaryDamage()
	{
		return new IEDamageSource(Lib.DMG_Tesla_prim).setDamageBypassesArmor();
	}
}