/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import blusunrize.immersiveengineering.mixin.accessors.AbstractArrowAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

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

	public SawbladeEntity(Level world, @Nonnull LivingEntity living, float velocity, float accuracy, ItemStack ammo)
	{
		super(IEEntityTypes.SAWBLADE.get(), world, living, velocity, accuracy);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickup = Pickup.ALLOWED;
		((AbstractArrowAccess)this).invokeSetPierceLevel((byte)3);
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
		return Tools.SAWBLADE.asItem().getDefaultInstance();
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
	public double getDefaultGravity()
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
		if(this.getAmmo().isEmpty()&&this.level().isClientSide)
			this.ammo = getAmmoSynced();
		super.baseTick();
	}

	private void damageSawblade()
	{
		int dmg = Math.round(getAmmo().getMaxDamage()*.05f);
		Entity shooter = getOwner();
		if(level() instanceof ServerLevel serverLevel)
			getAmmo().hurtAndBreak(dmg, serverLevel, shooter instanceof ServerPlayer?(ServerPlayer)shooter: null, i -> discard());
	}

	@Override
	protected void onHitBlock(BlockHitResult result)
	{
		super.onHitBlock(result);
		damageSawblade();
	}

	@Override
	protected void onHitEntity(EntityHitResult result)
	{
		Entity shooter = getOwner();
		Entity target = result.getEntity();
		float damage = (float)(12f*IEServerConfig.TOOLS.railgun_damage.get());
		target.hurt(IEDamageSources.causeSawbladeDamage(this, shooter), damage);
		this.handlePiecing(target);
		damageSawblade();
	}

	@Override
	protected void handlePiecing(Entity target)
	{
		super.handlePiecing(target);
		Entity owner = getOwner();
		if(this.piercedEntities.size() >= 3&&owner instanceof Player shooter)
		{
			Utils.unlockIEAdvancement(shooter, "tools/secret_ravenholm");
		}
	}

	@Override
	protected boolean canHitEntity(Entity target)
	{
		if(piercedEntities!=null&&piercedEntities.contains(target.getId()))
			return false;
		return !target.isSpectator()&&target.isAlive()&&target.isPickable();
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