package blusunrize.immersiveengineering.common.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityRailgunShot extends EntityIEProjectile
{
	private ItemStack ammo;
	final static int dataMarker_ammo = 13;

	public EntityRailgunShot(World world)
	{
		super(world);
		this.setSize(.5f, .5f);
	}
	public EntityRailgunShot(World world, double x, double y, double z, double ax, double ay, double az, ItemStack ammo)
	{
		super(world, x,y,z, ax,ay,az);
		this.setSize(.5f, .5f);
		this.ammo = ammo;
		this.setAmmoSynced();
	}
	public EntityRailgunShot(World world, EntityLivingBase living, double ax, double ay, double az, ItemStack ammo)
	{
		super(world, living, ax, ay, az);
		this.setSize(.5f, .5f);
		this.ammo = ammo;
		this.setAmmoSynced();
	}
	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObjectByDataType(dataMarker_ammo, 5);
	}

	public void setAmmoSynced()
	{
		if(this.getAmmo()!=null)
			this.dataWatcher.updateObject(dataMarker_ammo, getAmmo());
	}
	public ItemStack getAmmoSynced()
	{
		return this.dataWatcher.getWatchableObjectItemStack(dataMarker_ammo);
	}
	public ItemStack getAmmo()
	{
		return ammo;
	}

	@Override
	public double getGravity()
	{
		return .01;
	}

	@Override
	public int getMaxTicksInGround()
	{
		return 500;
	}

	@Override
	public void onEntityUpdate()
	{
		if(this.getAmmo() == null && this.worldObj.isRemote)
			this.ammo = getAmmoSynced();
		super.onEntityUpdate();
	}

	@Override
	public void onImpact(MovingObjectPosition mop)
	{
		if(!this.worldObj.isRemote && getAmmo()!=null)
		{
			if(mop.entityHit!=null)
			{
				if(mop.entityHit.attackEntityFrom(DamageSource.inFire, 2))
					mop.entityHit.hurtResistantTime = (int)(mop.entityHit.hurtResistantTime*.75);
			}
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		if(this.ammo!=null)
			nbt.setTag("ammo", this.ammo.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		this.ammo = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("ammo"));
	}
}