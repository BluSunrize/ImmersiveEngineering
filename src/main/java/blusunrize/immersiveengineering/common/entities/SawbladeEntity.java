/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nonnull;

public class SawbladeEntity extends IEProjectileEntity
{
	private ItemStack ammo = ItemStack.EMPTY;
	private static final EntityDataAccessor<ItemStack> dataMarker_ammo = SynchedEntityData.defineId(SawbladeEntity.class, EntityDataSerializers.ITEM_STACK);

	public SawbladeEntity(EntityType<SawbladeEntity> type, Level world)
	{
		super(type, world);
		this.pickup = Pickup.ALLOWED;
	}

	public SawbladeEntity(Level world, LivingEntity living, double ax, double ay, double az, ItemStack ammo)
	{
		super(IEEntityTypes.SAWBLADE.get(), world, living, ax, ay, az);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickup = Pickup.ALLOWED;
		this.setPierceLevel((byte)3);
	}

	@Override
	protected void defineSynchedData()
	{
		super.defineSynchedData();
		this.entityData.define(dataMarker_ammo, ItemStack.EMPTY);
	}

	@Nonnull
	@Override
	protected ItemStack getPickupItem()
	{
		return ammo;
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

	@Override
	public double getGravity()
	{
		return .005;
	}

	@Override
	public int getMaxTicksInGround()
	{
		return 1200;
	}

	@Override
	public void baseTick()
	{
		if(this.getAmmo().isEmpty()&&this.level.isClientSide)
			this.ammo = getAmmoSynced();
		super.baseTick();
	}

	@Override
	public void onHit(HitResult mop)
	{
		if(!this.level.isClientSide&&!getAmmo().isEmpty())
		{
			if(mop instanceof EntityHitResult)
			{
				Entity hit = ((EntityHitResult)mop).getEntity();
				Entity shooter = getOwner();
				// todo: make this configurable?
				hit.hurt(IEDamageSources.causeSawbladeDamage(this, shooter), 12.0f);
			}
			int dmg = Math.round(getAmmo().getMaxDamage()*.05f);
			Entity shooter = getOwner();
			if(getAmmo().hurt(dmg, level.random, shooter instanceof ServerPlayer?(ServerPlayer)shooter: null))
				this.discard();
			if(mop instanceof BlockHitResult)
				this.onHitBlock((BlockHitResult)mop);
		}
	}

	@Override
	protected void handlePiecing(Entity target)
	{
		super.handlePiecing(target);
		if(this.piercedEntities.size() >= 3 && getShooterUUID() != null)
		{
			Player shooter = level.getPlayerByUUID(this.getShooterUUID());
			if(shooter!=null)
				Utils.unlockIEAdvancement(shooter, "tools/secret_ravenholm");
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag nbt)
	{
		super.addAdditionalSaveData(nbt);
		if(!this.ammo.isEmpty())
			nbt.put("ammo", this.ammo.save(new CompoundTag()));
	}

	@Override
	public void readAdditionalSaveData(CompoundTag nbt)
	{
		super.readAdditionalSaveData(nbt);
		this.ammo = ItemStack.of(nbt.getCompound("ammo"));
	}
}