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
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.network.NetworkHooks;

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

	public RailgunShotEntity(Level world, LivingEntity living, double ax, double ay, double az, ItemStack ammo)
	{
		super(IEEntityTypes.RAILGUN_SHOT.get(), world, living, ax, ay, az);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickup = Pickup.ALLOWED;
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

	public IRailgunProjectile getProjectileProperties()
	{
		if(ammoProperties==null&&!ammo.isEmpty())
			ammoProperties = RailgunHandler.getProjectile(ammo);
		return ammoProperties;
	}

	@Override
	public double getGravity()
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
		if(this.getAmmo().isEmpty()&&this.level.isClientSide)
			this.ammo = getAmmoSynced();
		super.baseTick();
	}

	@Override
	public void onHit(HitResult mop)
	{
		if(!this.level.isClientSide&&!getAmmo().isEmpty())
		{
			IRailgunProjectile projectileProperties = getProjectileProperties();
			if(projectileProperties!=null)
			{
				Entity shooter = this.getOwner();
				UUID shooterUuid = this.getShooterUUID();
				if(mop instanceof EntityHitResult)
				{
					Entity hit = ((EntityHitResult)mop).getEntity();
					double damage = projectileProperties.getDamage(this.level, hit, shooterUuid, this);
					hit.hurt(
							IEDamageSources.causeRailgunDamage(this, shooter),
							(float)(damage*IEServerConfig.TOOLS.railgun_damage.get())
					);
				}
				else if(mop instanceof BlockHitResult)
				{
					double breakRoll = this.random.nextDouble();
					if(breakRoll <= getProjectileProperties().getBreakChance(shooterUuid, ammo))
						this.remove();
				}
				projectileProperties.onHitTarget(this.level, mop, shooterUuid, this);
			}
			if(mop instanceof BlockHitResult)
				this.onHitBlock((BlockHitResult)mop);
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

	@Override
	public Packet<?> getAddEntityPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}