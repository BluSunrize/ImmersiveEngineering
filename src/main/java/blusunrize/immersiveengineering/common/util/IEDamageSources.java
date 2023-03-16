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
import blusunrize.immersiveengineering.mixin.accessors.DamageSourcesAccess;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

// TODO tag various types as bypassing armor and fire
public class IEDamageSources
{
	public static class ElectricDamageSource extends DamageSource implements IElectricDamageSource
	{
		public IElectricEquipment.ElectricSource source;
		public float dmg;

		public ElectricDamageSource(Holder<DamageType> tag, IElectricEquipment.ElectricSource source, float amount)
		{
			super(tag);
			this.source = source;
			dmg = amount;
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

	public static DamageSource causeCasullDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_RevolverCasull);
		return sources(shot).source3(Lib.DMG_RevolverCasull, shot, shooter);
	}

	public static DamageSource causePiercingDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_RevolverAP);
		return sources(shot).source3(Lib.DMG_RevolverAP, shot, shooter);
	}

	public static DamageSource causeBuckshotDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_RevolverBuck);
		return sources(shot).source3(Lib.DMG_RevolverBuck, shot, shooter);
	}

	public static DamageSource causeDragonsbreathDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_RevolverDragon);
		return sources(shot).source3(Lib.DMG_RevolverDragon, shot, shooter);
	}

	public static DamageSource causeHomingDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_RevolverHoming);
		return sources(shot).source3(Lib.DMG_RevolverHoming, shot, shooter);
	}

	public static DamageSource causeWolfpackDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_RevolverWolfpack);
		return sources(shot).source3(Lib.DMG_RevolverWolfpack, shot, shooter);
	}

	public static DamageSource causeSilverDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_RevolverSilver);
		return sources(shot).source3(Lib.DMG_RevolverSilver, shot, shooter);
	}

	public static DamageSource causePotionDamage(RevolvershotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_RevolverPotion);
		return sources(shot).source3(Lib.DMG_RevolverPotion, shot, shooter);
	}

	public static DamageSource acid(Level level)
	{
		return sources(level).source1(Lib.DMG_Acid);
	}

	public static DamageSource crusher(Level level)
	{
		return sources(level).source1(Lib.DMG_Crusher);
	}

	public static DamageSource sawmill(Level level)
	{
		return sources(level).source1(Lib.DMG_Sawmill);
	}

	public static DamageSource razorWire(Level level)
	{
		return sources(level).source1(Lib.DMG_RazorWire);
	}

	public static DamageSource razorShock(Level level)
	{
		return sources(level).source1(Lib.DMG_RazorShock);
	}

	// DO NOT USE EXCEPT FOR CHECKING WHETHER AN ENTITY IS VULNERABLE
	public static DamageSource wireShock(Level level)
	{
		return new ElectricDamageSource(typeHolder(level, Lib.DMG_WireShock), new ElectricSource(1), 1);
	}

	private static final IElectricEquipment.ElectricSource TC_LOW = new IElectricEquipment.ElectricSource(.25F);
	private static final IElectricEquipment.ElectricSource TC_HIGH = new IElectricEquipment.ElectricSource(2);

	public static ElectricDamageSource causeTeslaDamage(Level level, float amount, boolean lowPower)
	{
		return new ElectricDamageSource(typeHolder(level, Lib.DMG_Tesla), lowPower?TC_LOW: TC_HIGH, amount);
	}

	public static ElectricDamageSource causeWireDamage(Level level, float amount, IElectricEquipment.ElectricSource source)
	{
		return new ElectricDamageSource(typeHolder(level, Lib.DMG_WireShock), source, amount);
	}

	public static DamageSource causeRailgunDamage(RailgunShotEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_Railgun);
		return sources(shot).source3(Lib.DMG_Railgun, shot, shooter);
	}

	public static DamageSource causeSawbladeDamage(SawbladeEntity shot, Entity shooter)
	{
		if(shooter==null)
			return turret(shot, Lib.DMG_Sawblade);
		return sources(shot).source3(Lib.DMG_Sawblade, shot, shooter);
	}

	public static DamageSource causeTeslaPrimaryDamage(Level level)
	{
		return sources(level).source1(Lib.DMG_Tesla_prim);
	}

	private static DamageSource turret(Entity projectile, ResourceKey<DamageType> type)
	{
		// TODO re-add special message for turrets
		return sources(projectile).source1(type);
	}

	private static DamageSourcesAccess sources(Level level)
	{
		return (DamageSourcesAccess)level.damageSources();
	}

	private static DamageSourcesAccess sources(Entity entity)
	{
		return sources(entity.level);
	}

	private static Holder<DamageType> typeHolder(Level level, ResourceKey<DamageType> typeKey)
	{
		final Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
		return registry.getHolderOrThrow(typeKey);
	}
}