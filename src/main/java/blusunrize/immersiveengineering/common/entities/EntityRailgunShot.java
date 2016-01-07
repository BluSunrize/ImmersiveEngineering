package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.RailgunProjectileProperties;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;

public class EntityRailgunShot extends EntityIEProjectile
{
	private ItemStack ammo;
	final static int dataMarker_ammo = 13;
	private RailgunHandler.RailgunProjectileProperties ammoProperties;

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
	public RailgunProjectileProperties getAmmoProperties()
	{
		if(ammoProperties==null && ammo!=null)
			ammoProperties = RailgunHandler.getProjectileProperties(ammo);
		return ammoProperties;
	}

	@Override
	public double getGravity()
	{
		return .005*(getAmmoProperties()!=null?getAmmoProperties().gravity:1);
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
		//		if(worldObj instanceof WorldServer)
		//			((WorldServer)worldObj).func_147487_a("flame", posX,posY,posZ, 0, 0,0,0, 1);
		//		else
		//			worldObj.spawnParticle("smoke", posX, posY, posZ, 0, 0, 0);
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
				if(getAmmoProperties()!=null)
				{
					if(!getAmmoProperties().overrideHitEntity(mop.entityHit, getShooter()))
						mop.entityHit.attackEntityFrom(IEDamageSources.causeRailgunDamage(this, getShooter()), (float)getAmmoProperties().damage);
				}
			}
		}
	}

	@Override
    public void onCollideWithPlayer(EntityPlayer player)
    {
        if(!this.worldObj.isRemote && this.inGround && this.getAmmo()!=null)
            if(player.inventory.addItemStackToInventory(this.getAmmo().copy()))
            {
                this.playSound("random.pop", 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
                this.setDead();
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