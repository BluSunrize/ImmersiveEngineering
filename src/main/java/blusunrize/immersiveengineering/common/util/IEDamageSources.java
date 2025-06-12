/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.Lib.TurretDamageType;
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
import net.neoforged.neoforge.common.util.FakePlayer;

import javax.annotation.Nullable;

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
		return maybeTurret(shot, shooter, Lib.DamageTypes.REVOLVER_CASULL);
	}

	public static DamageSource causePiercingDamage(RevolvershotEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.REVOLVER_ARMORPIERCING);
	}

	public static DamageSource causeBuckshotDamage(RevolvershotEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.REVOLVER_BUCKSHOT);
	}

	public static DamageSource causeDragonsbreathDamage(RevolvershotEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.REVOLVER_DRAGONSBREATH);
	}

	public static DamageSource causeHomingDamage(RevolvershotEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.REVOLVER_HOMING);
	}

	public static DamageSource causeWolfpackDamage(RevolvershotEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.REVOLVER_WOLFPACK);
	}

	public static DamageSource causeSilverDamage(RevolvershotEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.REVOLVER_SILVER);
	}

	public static DamageSource causePotionDamage(RevolvershotEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.REVOLVER_POTION);
	}

	public static DamageSource acid(Level level)
	{
		return sources(level).invokeSource(Lib.DamageTypes.ACID, null, null);
	}

	public static DamageSource crusher(Level level)
	{
		return sources(level).invokeSource(Lib.DamageTypes.CRUSHER, null, null);
	}

	public static DamageSource sawmill(Level level)
	{
		return sources(level).invokeSource(Lib.DamageTypes.SAWMILL, null, null);
	}

	public static DamageSource razorWire(Level level)
	{
		return sources(level).invokeSource(Lib.DamageTypes.RAZOR_WIRE, null, null);
	}

	public static DamageSource razorShock(Level level)
	{
		return sources(level).invokeSource(Lib.DamageTypes.RAZOR_SHOCK, null, null);
	}

	// DO NOT USE EXCEPT FOR CHECKING WHETHER AN ENTITY IS VULNERABLE
	public static DamageSource wireShock(Level level)
	{
		return new ElectricDamageSource(typeHolder(level, Lib.DamageTypes.WIRE_SHOCK), new ElectricSource(1), 1);
	}

	private static final IElectricEquipment.ElectricSource TC_LOW = new IElectricEquipment.ElectricSource(.25F);
	private static final IElectricEquipment.ElectricSource TC_HIGH = new IElectricEquipment.ElectricSource(2);

	public static ElectricDamageSource causeTeslaDamage(Level level, float amount, boolean lowPower)
	{
		return new ElectricDamageSource(typeHolder(level, Lib.DamageTypes.TESLA), lowPower?TC_LOW: TC_HIGH, amount);
	}

	public static ElectricDamageSource causeWireDamage(Level level, float amount, IElectricEquipment.ElectricSource source)
	{
		return new ElectricDamageSource(typeHolder(level, Lib.DamageTypes.WIRE_SHOCK), source, amount);
	}

	public static DamageSource causeRailgunDamage(RailgunShotEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.RAILGUN);
	}

	public static DamageSource causeSawbladeDamage(SawbladeEntity shot, Entity shooter)
	{
		return maybeTurret(shot, shooter, Lib.DamageTypes.SAWBLADE);
	}

	public static DamageSource causeTeslaPrimaryDamage(Level level)
	{
		return sources(level).invokeSource(Lib.DamageTypes.TESLA_PRIMARY, null, null);
	}

	private static DamageSource maybeTurret(Entity shot, @Nullable Entity shooter, TurretDamageType type)
	{
		if(shooter==null || shooter instanceof FakePlayer)
			return sources(shot).invokeSource(type.turretType(), shot, shooter);
		return sources(shot).invokeSource(type.playerType(), shot, shooter);
	}

	private static DamageSourcesAccess sources(Level level)
	{
		return (DamageSourcesAccess)level.damageSources();
	}

	private static DamageSourcesAccess sources(Entity entity)
	{
		return sources(entity.level());
	}

	private static Holder<DamageType> typeHolder(Level level, ResourceKey<DamageType> typeKey)
	{
		final Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
		return registry.getHolderOrThrow(typeKey);
	}
}