/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.RailgunProjectileProperties;
import blusunrize.immersiveengineering.common.Config.IEConfig.Tools;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityRailgunShot extends EntityIEProjectile
{
	private ItemStack ammo = ItemStack.EMPTY;
	private static final DataParameter<ItemStack> dataMarker_ammo = EntityDataManager.createKey(EntityRailgunShot.class, DataSerializers.ITEM_STACK);
	private RailgunProjectileProperties ammoProperties;

	public EntityRailgunShot(World world)
	{
		super(world);
		this.setSize(.5f, .5f);
		this.pickupStatus = PickupStatus.ALLOWED;
	}

	public EntityRailgunShot(World world, double x, double y, double z, double ax, double ay, double az, ItemStack ammo)
	{
		super(world, x, y, z, ax, ay, az);
		this.setSize(.5f, .5f);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickupStatus = PickupStatus.ALLOWED;
	}

	public EntityRailgunShot(World world, EntityLivingBase living, double ax, double ay, double az, ItemStack ammo)
	{
		super(world, living, ax, ay, az);
		this.setSize(.5f, .5f);
		this.ammo = ammo;
		this.setAmmoSynced();
		this.pickupStatus = PickupStatus.ALLOWED;
	}

	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataManager.register(dataMarker_ammo, ItemStack.EMPTY);
	}

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
	public void onEntityUpdate()
	{
		// For testign Desync
		//		if(world instanceof WorldServer)
		//			((WorldServer)world).func_147487_a("flame", posX,posY,posZ, 0, 0,0,0, 1);
		//		else
		//			world.spawnParticle("smoke", posX, posY, posZ, 0, 0, 0);
		if(this.getAmmo().isEmpty()&&this.world.isRemote)
			this.ammo = getAmmoSynced();
		super.onEntityUpdate();
	}

	@Override
	public void onImpact(RayTraceResult mop)
	{
		if(!this.world.isRemote&&!getAmmo().isEmpty())
		{
			if(mop.entityHit!=null)
			{
				if(getAmmoProperties()!=null)
				{
					if(!getAmmoProperties().overrideHitEntity(mop.entityHit, getShooter()))
						mop.entityHit.attackEntityFrom(IEDamageSources.causeRailgunDamage(this, getShooter()), (float)getAmmoProperties().damage*Tools.railgun_damage);
				}
			}
		}
	}

//	@Override
//    public void onCollideWithPlayer(EntityPlayer player)
//    {
//        if(!this.world.isRemote && this.inGround && this.getAmmo()!=null)
//            if(player.inventory.addItemStackToInventory(this.getAmmo().copy()))
//            {
//                this.playSound("random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
//                this.setDead();
//            }
//    }


	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		if(!this.ammo.isEmpty())
			nbt.setTag("ammo", this.ammo.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.ammo = new ItemStack(nbt.getCompoundTag("ammo"));
	}
}