/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.IRailgunProjectile;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEItems.Ingredients;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nonnull;
import java.util.UUID;

public class RailgunShotEntity extends IEProjectileEntity
{
	private ItemStack ammo = ItemStack.EMPTY;
	private static final EntityDataAccessor<ItemStack> dataMarker_ammo = SynchedEntityData.defineId(RailgunShotEntity.class, EntityDataSerializers.ITEM_STACK);
	private IRailgunProjectile ammoProperties;

	public RailgunShotEntity(EntityType<RailgunShotEntity> type, Level world)
	{
		super(type, world);
		this.pickup = Pickup.ALLOWED;
	}

	public RailgunShotEntity(Level world, @Nonnull LivingEntity living, float velocity, float accuracy, ItemStack ammo)
	{
		super(IEEntityTypes.RAILGUN_SHOT.get(), world, living, velocity, accuracy);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickup = Pickup.ALLOWED;
	}

	@Override
	protected void defineSynchedData(Builder builder)
	{
		super.defineSynchedData(builder);
		builder.define(dataMarker_ammo, ItemStack.EMPTY);
	}

	@Nonnull
	@Override
	protected ItemStack getPickupItem()
	{
		return ammo;
	}

	@Nonnull
	@Override
	protected ItemStack getDefaultPickupItem()
	{
		return Ingredients.STICK_STEEL.asItem().getDefaultInstance();
	}

	public void setAmmoSynced()
	{
		if(!this.getAmmo().isEmpty())
			this.entityData.set(dataMarker_ammo, getAmmo());
	}

	public ItemStack getAmmoSynced()
	{
		return this.entityData.get(dataMarker_ammo);
	}

	public ItemStack getAmmo()
	{
		return ammo;
	}

	public IRailgunProjectile getProjectileProperties()
	{
		if(ammoProperties==null&&!ammo.isEmpty())
			ammoProperties = RailgunHandler.getProjectile(ammo);
		return ammoProperties;
	}

	@Override
	public double getDefaultGravity()
	{
		return .005*(getProjectileProperties()!=null?getProjectileProperties().getGravity(): 1);
	}

	@Override
	public int getMaxTicksInGround()
	{
		return 500;
	}

	@Override
	public void baseTick()
	{
		if(this.getAmmo().isEmpty()&&this.level().isClientSide)
			this.ammo = getAmmoSynced();
		super.baseTick();
	}

	@Override
	protected void onHitEntity(EntityHitResult result)
	{
		if(!this.level().isClientSide&&!getAmmo().isEmpty())
		{
			IRailgunProjectile projectileProperties = getProjectileProperties();
			if(projectileProperties!=null)
			{
				Entity shooter = this.getOwner();
				UUID shooterUuid = shooter!=null?shooter.getUUID():null;
				Entity hit = result.getEntity();
				double damage = projectileProperties.getDamage(this.level(), hit, shooterUuid, this);
				DamageSource source = projectileProperties.getDamageSource(this.level(), hit, shooterUuid, this);
				if(source==null)
					source = IEDamageSources.causeRailgunDamage(this, shooter);
				if(shooter instanceof LivingEntity livingShooter)
					livingShooter.setLastHurtMob(hit);
				hit.hurt(source, (float)(damage*IEServerConfig.TOOLS.railgun_damage.get()));
				projectileProperties.onHitTarget(this.level(), result, shooterUuid, this);
			}
		}
		this.discard();
	}

	@Override
	protected void onHitBlock(BlockHitResult result)
	{
		super.onHitBlock(result);
		if(!this.level().isClientSide&&!getAmmo().isEmpty())
		{
			IRailgunProjectile projectileProperties = getProjectileProperties();
			if(projectileProperties!=null)
			{
				Entity owner = getOwner();
				UUID shooterUuid = owner!=null?owner.getUUID():null;
				double breakRoll = this.random.nextDouble();
				if(breakRoll <= getProjectileProperties().getBreakChance(shooterUuid, ammo))
					this.discard();
				projectileProperties.onHitTarget(this.level(), result, shooterUuid, this);
			}
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt)
	{
		super.addAdditionalSaveData(nbt);
		if(!this.ammo.isEmpty())
			nbt.put("ammo", this.ammo.save(level().registryAccess()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt)
	{
		super.readAdditionalSaveData(nbt);
		this.ammo = ItemStack.parseOptional(level().registryAccess(), nbt.getCompound("ammo"));
	}
}