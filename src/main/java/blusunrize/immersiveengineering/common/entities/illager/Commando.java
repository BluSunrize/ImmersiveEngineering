/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities.illager;

import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.common.entities.ai.RevolverAttackGoal;
import blusunrize.immersiveengineering.common.items.BulletItem;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;

public class Commando extends EngineerIllager
{
	private static final EntityDataAccessor<Boolean> IS_AIMING = SynchedEntityData.defineId(Commando.class, EntityDataSerializers.BOOLEAN);

	private RevolverAttackGoal<?> revolverGoal;

	private ItemStack revolverAmmo = ItemStack.EMPTY;

	public Commando(EntityType<? extends AbstractIllager> entityType, Level level)
	{
		super(entityType, level);
	}

	@Override
	protected void registerGoals()
	{
		super.registerGoals();
		this.goalSelector.addGoal(0, new FloatGoal(this));
		this.goalSelector.addGoal(2, new HoldGroundAttackGoal(this, 10.0F));
		this.revolverGoal = new RevolverAttackGoal<>(this, 18.0F, 4);
		this.goalSelector.addGoal(3, revolverGoal);
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
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(IS_AIMING, false);
	}

	public void setAiming(boolean isCharging)
	{
		this.entityData.set(IS_AIMING, isCharging);
	}

	public boolean isAiming()
	{
		return this.entityData.get(IS_AIMING);
	}


	@Override
	public void addAdditionalSaveData(CompoundTag compound)
	{
		super.addAdditionalSaveData(compound);
		if(!this.revolverAmmo.isEmpty())
			compound.put("revolverAmmo", this.revolverAmmo.save(new CompoundTag()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound)
	{
		super.readAdditionalSaveData(compound);
		if(compound.contains("revolverAmmo"))
			this.revolverAmmo = ItemStack.of(compound.getCompound("revolverAmmo"));
	}

	public ItemStack getRevolverAmmo()
	{
		return this.revolverAmmo;
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag)
	{
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Weapons.REVOLVER));
		this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
		float rng = level.getRandom().nextFloat();
		if(rng < .02f) // 2% chance for a funny joke
			this.revolverAmmo = BulletHandler.getBulletStack(BulletItem.FIREWORK);
		else if(rng < .12f) // 10% chance for a commando that will oneshot you
			this.revolverAmmo = BulletHandler.getBulletStack(BulletItem.DRAGONS_BREATH);
		else // and the rest are just normal guys
			this.revolverAmmo = BulletHandler.getBulletStack(BulletItem.CASULL);
		return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
	}

	@Override
	public void applyRaidBuffs(int wave, boolean unusedFalse)
	{
		Raid raid = this.getCurrentRaid();
		boolean flag = this.random.nextFloat() <= raid.getEnchantOdds();
		if(flag)
			revolverGoal.setMaxBullets(8);
	}


	@Override
	protected SoundEvent getAmbientSound()
	{
		return SoundEvents.VINDICATOR_AMBIENT;
	}

	@Override
	protected SoundEvent getDeathSound()
	{
		return SoundEvents.VINDICATOR_DEATH;
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource)
	{
		return SoundEvents.VINDICATOR_DEATH;
	}

	@Override
	public SoundEvent getCelebrateSound()
	{
		return SoundEvents.VINDICATOR_CELEBRATE;
	}
}
