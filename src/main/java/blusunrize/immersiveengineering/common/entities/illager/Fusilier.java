/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities.illager;

import blusunrize.immersiveengineering.common.entities.ai.RailgunAttackGoal;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class Fusilier extends EngineerIllager
{
	private static final EntityDataAccessor<Boolean> IS_AIMING_RAILGUN = SynchedEntityData.defineId(Fusilier.class, EntityDataSerializers.BOOLEAN);

	public Fusilier(EntityType<? extends AbstractIllager> entityType, Level level)
	{
		super(entityType, level);
	}

	@Override
	protected void registerGoals()
	{
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new Raider.HoldGroundAttackGoal(this, 10.0F));
		this.goalSelector.addGoal(3, new RailgunAttackGoal<>(this, 1.0D, 8.0F));
		this.goalSelector.addGoal(8, new RandomStrollGoal(this, 0.6D));
		this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 15.0F, 1.0F));
		this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 15.0F));
		this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
	}

	public static AttributeSupplier.Builder createAttributes()
	{
		return Monster.createMonsterAttributes()
				.add(Attributes.MOVEMENT_SPEED, 0.35F)
				.add(Attributes.MAX_HEALTH, 24.0D)
				.add(Attributes.ATTACK_DAMAGE, 5.0D)
				.add(Attributes.FOLLOW_RANGE, 32.0D);
	}

	@Override
	protected void defineSynchedData(Builder builder)
	{
		super.defineSynchedData(builder);
		builder.define(IS_AIMING_RAILGUN, false);
	}

	public void setAimingRailgun(boolean isCharging)
	{
		this.entityData.set(IS_AIMING_RAILGUN, isCharging);
	}

	public boolean isAimingRailgun()
	{
		return this.entityData.get(IS_AIMING_RAILGUN);
	}


	@Override
	public AbstractIllager.IllagerArmPose getArmPose()
	{
		if(this.isAimingRailgun())
			return IllagerArmPose.CROSSBOW_HOLD;
		return super.getArmPose();
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData)
	{
		SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, reason, spawnData);
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Weapons.RAILGUN));
		this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Misc.POWERPACK));
		// prevent left-handed, because rendering the railgun positioning is a nightmare
		this.setLeftHanded(false);
		return spawnGroupData;
	}

	@Override
	public void applyRaidBuffs(ServerLevel p_348605_, int p_37844_, boolean p_37845_)
	{
		Raid raid = this.getCurrentRaid();
		boolean flag = this.random.nextFloat() <= raid.getEnchantOdds();
		if(flag)
		{
			ItemStack railgun = new ItemStack(Weapons.RAILGUN);
			CompoundTag upgrades = new CompoundTag();
			upgrades.putFloat("speed", 1f);
			ItemNBTHelper.setTagCompound(railgun, "upgrades", upgrades);
			this.setItemSlot(EquipmentSlot.MAINHAND, railgun);
		}

	}

	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.PILLAGER_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.PILLAGER_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource)
	{
		return SoundEvents.PILLAGER_DEATH;
	}

	@Override
	public SoundEvent getCelebrateSound()
	{
		return SoundEvents.PILLAGER_CELEBRATE;
	}
}
