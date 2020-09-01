/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class SawbladeEntity extends IEProjectileEntity
{
	public static final EntityType<SawbladeEntity> TYPE = Builder
			.<SawbladeEntity>create(SawbladeEntity::new, EntityClassification.MISC)
			.size(.75F, .2F)
			.build(ImmersiveEngineering.MODID+":sawblade");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "sawblade");
	}

	private ItemStack ammo = ItemStack.EMPTY;
	private static final DataParameter<ItemStack> dataMarker_ammo = EntityDataManager.createKey(SawbladeEntity.class, DataSerializers.ITEMSTACK);

	public SawbladeEntity(EntityType<SawbladeEntity> type, World world)
	{
		super(type, world);
		this.pickupStatus = PickupStatus.ALLOWED;
	}

	public SawbladeEntity(World world, LivingEntity living, double ax, double ay, double az, ItemStack ammo)
	{
		super(TYPE, world, living, ax, ay, az);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickupStatus = PickupStatus.ALLOWED;
		this.setPierceLevel((byte)3);
	}

	@Override
	protected void registerData()
	{
		super.registerData();
		this.dataManager.register(dataMarker_ammo, ItemStack.EMPTY);
	}

	@Nonnull
	@Override
	protected ItemStack getArrowStack()
	{
		return ammo;
	}

	public void setAmmoSynced()
	{
		if(!this.getAmmo().isEmpty())
			this.dataManager.set(dataMarker_ammo, getAmmo());
	}

	public ItemStack getAmmoSynced()
	{
		return this.dataManager.get(dataMarker_ammo);
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
		if(this.getAmmo().isEmpty()&&this.world.isRemote)
			this.ammo = getAmmoSynced();
		super.baseTick();
	}

	@Override
	public void onImpact(RayTraceResult mop)
	{
		if(!this.world.isRemote&&!getAmmo().isEmpty())
		{
			if(mop instanceof EntityRayTraceResult)
			{
				Entity hit = ((EntityRayTraceResult)mop).getEntity();
				Entity shooter = func_234616_v_();
				// todo: make this configurable?
				hit.attackEntityFrom(IEDamageSources.causeSawbladeDamage(this, shooter), 12.0f);
			}
			int dmg = Math.round(getAmmo().getMaxDamage()*.05f);
			Entity shooter = func_234616_v_();
			if(getAmmo().attemptDamageItem(dmg, world.rand, shooter instanceof ServerPlayerEntity?(ServerPlayerEntity)shooter: null))
				this.remove();
		}
	}

	@Override
	protected void handlePiecing(Entity target)
	{
		super.handlePiecing(target);
		if(this.piercedEntities.size() >= 3)
		{
			PlayerEntity shooter = world.getPlayerByUuid(this.getShooterUUID());
			if(shooter!=null)
				Utils.unlockIEAdvancement(shooter, "main/secret_ravenholm");
		}
	}

	@Override
	public void writeAdditional(CompoundNBT nbt)
	{
		super.writeAdditional(nbt);
		if(!this.ammo.isEmpty())
			nbt.put("ammo", this.ammo.write(new CompoundNBT()));
	}

	@Override
	public void readAdditional(CompoundNBT nbt)
	{
		super.readAdditional(nbt);
		this.ammo = ItemStack.read(nbt.getCompound("ammo"));
	}
}