/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment;
import blusunrize.immersiveengineering.api.tool.IElectricEquipment.ElectricSource;
import blusunrize.immersiveengineering.api.wires.utils.IElectricDamageSource;
import blusunrize.immersiveengineering.common.entities.RailgunShotEntity;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.entities.SawbladeEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class IEDamageSources
{
	public static class IEDamageSource_Indirect extends IndirectEntityDamageSource
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

	public static class ElectricDamageSource extends DamageSource implements IElectricDamageSource
	{
		public IElectricEquipment.ElectricSource source;
		public float dmg;

		public ElectricDamageSource(String tag, IElectricEquipment.ElectricSource source, float amount)
		{
			super(tag);
			this.source = source;
			dmg = amount;
			bypassArmor();
		}

		@Override
		public boolean apply(Entity e)
		{
			if(e instanceof LivingEntity living)
				IElectricEquipment.applyToEntity(living, this, source);
			if(dmg > 0)
				e.hurt(this, dmg);
			return dmg > 0;
		}

		@Override
		public float getDamage()
		{
			return dmg;
		}
	}

	public static class TurretDamageSource extends IEDamageSource
	{
		public TurretDamageSource(String damageTypeIn)
		{
			super(damageTypeIn);
		}

		@Override
		public Component getLocalizedDeathMessage(LivingEntity entityLivingBaseIn)
		{
			String s = "death.attack."+this.msgId+".turret";
			return Component.translatable(s, entityLivingBaseIn.getDisplayName());
		}
	}

	public static DamageSource causeCasullDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverCasull);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverCasull, shot, shooter);
	}

	public static DamageSource causePiercingDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverAP).bypassArmor();
		return new IEDamageSource_Indirect(Lib.DMG_RevolverAP, shot, shooter).bypassArmor();
	}

	public static DamageSource causeBuckshotDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverBuck);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverBuck, shot, shooter);
	}

	public static DamageSource causeDragonsbreathDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverDragon).setIsFire();
		return new IEDamageSource_Indirect(Lib.DMG_RevolverDragon, shot, shooter).setIsFire();
	}

	public static DamageSource causeHomingDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverHoming);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverHoming, shot, shooter);
	}

	public static DamageSource causeWolfpackDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverWolfpack);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverWolfpack, shot, shooter);
	}

	public static DamageSource causeSilverDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverSilver);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverSilver, shot, shooter);
	}

	public static DamageSource causePotionDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_RevolverPotion);
		return new IEDamageSource_Indirect(Lib.DMG_RevolverPotion, shot, shooter);
	}

	public static DamageSource acid = new IEDamageSource(Lib.DMG_Acid);

	public static DamageSource crusher = new IEDamageSource(Lib.DMG_Crusher);

	public static DamageSource sawmill = new IEDamageSource(Lib.DMG_Sawmill);

	public static DamageSource razorWire = new IEDamageSource(Lib.DMG_RazorWire);

	public static DamageSource razorShock = new IEDamageSource(Lib.DMG_RazorShock);

	// DO NOT USE EXCEPT FOR CHECKING WHETHER AN ENTITY IS VULNERABLE
	public static DamageSource wireShock = new ElectricDamageSource(Lib.DMG_WireShock, new ElectricSource(1), 1);

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

	public static DamageSource causeRailgunDamage(RailgunShotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_Railgun);
		return new IEDamageSource_Indirect(Lib.DMG_Railgun, shot, shooter).bypassArmor();
	}

	public static DamageSource causeSawbladeDamage(SawbladeEntity shot, Entity shooter)
	{
		if(shooter==null)
			return new TurretDamageSource(Lib.DMG_Sawblade);
		return new IEDamageSource_Indirect(Lib.DMG_Sawblade, shot, shooter).bypassArmor();
	}

	public static DamageSource causeTeslaPrimaryDamage()
	{
		return new IEDamageSource(Lib.DMG_Tesla_prim).bypassArmor();
	}
}