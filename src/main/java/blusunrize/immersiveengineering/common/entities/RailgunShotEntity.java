/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.RailgunProjectileProperties;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;

public class RailgunShotEntity extends IEProjectileEntity
{
	public static final EntityType<RailgunShotEntity> TYPE = Builder
			.<RailgunShotEntity>create(RailgunShotEntity::new, EntityClassification.MISC)
			.size(.5F, .5F)
			.build(ImmersiveEngineering.MODID+":railgun_shot");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "railgun_shot");
	}

	private ItemStack ammo = ItemStack.EMPTY;
	private static final DataParameter<ItemStack> dataMarker_ammo = EntityDataManager.createKey(RailgunShotEntity.class, DataSerializers.ITEMSTACK);
	private RailgunProjectileProperties ammoProperties;

	public RailgunShotEntity(EntityType<RailgunShotEntity> type, World world)
	{
		super(type, world);
		this.pickupStatus = PickupStatus.ALLOWED;
	}

	public RailgunShotEntity(World world, double x, double y, double z, double ax, double ay, double az, ItemStack ammo)
	{
		super(TYPE, world, x, y, z);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickupStatus = PickupStatus.ALLOWED;
	}

	public RailgunShotEntity(World world, LivingEntity living, double ax, double ay, double az, ItemStack ammo)
	{
		super(TYPE, world, living, ax, ay, az);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickupStatus = PickupStatus.ALLOWED;
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

	public RailgunProjectileProperties getAmmoProperties()
	{
		if(ammoProperties==null&&!ammo.isEmpty())
			ammoProperties = RailgunHandler.getProjectileProperties(ammo);
		return ammoProperties;
	}

	@Override
	public double getGravity()
	{
		return .005*(getAmmoProperties()!=null?getAmmoProperties().gravity: 1);
	}

	@Override
	public int getMaxTicksInGround()
	{
		return 500;
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
				if(getAmmoProperties()!=null)
				{
					Entity shooter = getShooter();
					if(!getAmmoProperties().overrideHitEntity(hit, shooter))
						hit.attackEntityFrom(IEDamageSources.causeRailgunDamage(this, shooter),
								(float)(getAmmoProperties().damage*IEConfig.TOOLS.railgun_damage.get()));
				}
			}
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

	@Override
	public IPacket<?> createSpawnPacket()
	{
		return NetworkHooks.getEntitySpawningPacket(this);
	}
}