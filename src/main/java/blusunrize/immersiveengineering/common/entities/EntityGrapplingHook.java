package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.common.util.ManeuverGearHelper;
import blusunrize.immersiveengineering.common.util.ManeuverGearHelper.HookMode;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityGrapplingHook extends EntityIEProjectile
{
	final static int dataMarker_hookNr = 13;
	final static int dataMarker_mode = 14;

	int hookNr = -1;
	HookMode hookMode = HookMode.LAUNCHING;
	double speed = .5;

	public EntityGrapplingHook(World world)
	{
		super(world);
		this.setSize(.5f, .5f);
	}
	public EntityGrapplingHook(World world, double x, double y, double z, double ax, double ay, double az)
	{
		super(world, x,y,z, ax,ay,az);
		this.setSize(.5f, .5f);
	}
	public EntityGrapplingHook(World world, EntityLivingBase living, double ax, double ay, double az)
	{
		super(world, living, ax, ay, az);
		this.setSize(.5f, .5f);
	}
	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject(dataMarker_hookNr, Integer.valueOf(0));
		this.dataWatcher.addObject(dataMarker_mode, Integer.valueOf(0));
	}


	public void setHookNrSynced()
	{
		this.dataWatcher.updateObject(dataMarker_hookNr, this.hookNr);
	}
	public int getHookNrSynced()
	{
		return this.dataWatcher.getWatchableObjectInt(dataMarker_hookNr);
	}
	public int getHookNr()
	{
		return hookNr;
	}
	public void setHookNr(int nr)
	{
		this.hookNr=nr;
	}

	public void setHookModeSynced()
	{
		this.dataWatcher.updateObject(dataMarker_mode, this.hookMode.ordinal());
	}
	public HookMode getHookModeSynced()
	{
		int i = this.dataWatcher.getWatchableObjectInt(dataMarker_mode);
		if(i>=0 && i<HookMode.values().length)
			return HookMode.values()[i];
		return null;
	}
	public HookMode getHookMode()
	{
		return hookMode;
	}
	public void setHookMode(HookMode mode)
	{
		this.hookMode=mode;
	}

	public double getHookSpeed()
	{
		return speed;
	}

	@Override
	public double getGravity()
	{
		return 0;
	}

	@Override
	public int getMaxTicksInGround()
	{
		return 500;
	}

	@Override
	public void onEntityUpdate()
	{
		super.onEntityUpdate();

		//		if(this.getHookMode()==null)
		this.hookMode = this.getHookModeSynced();
		if(this.getHookNr()<0)
			this.hookNr = this.getHookNrSynced();

		Entity target = this.getShooter();
		if(target!=null && target instanceof EntityPlayer)
		{
			if(this.getDistanceSqToEntity(target)>4096)
				this.setDead();

			if(this.hookNr>=0&&this.hookNr<2 && ManeuverGearHelper.getHooks((EntityPlayer)target)[this.hookNr]!=this)
				ManeuverGearHelper.getHooks((EntityPlayer)target)[this.hookNr] = this;
			if(this.hookMode==HookMode.RETURNING)
			{
				this.blockX = this.blockY = this.blockZ = -1;
				this.inGround=false;
				this.inBlock=null;
				this.inMeta=-1;

				double redirectionSpeed = .25;
				Vec3 newMotion = new Vec3(
						motionX*(1-redirectionSpeed)+ (target.posX-this.posX)*redirectionSpeed,
						motionY*(1-redirectionSpeed)+ ((target.posY+target.height/2)-this.posY)*redirectionSpeed,
						motionZ*(1-redirectionSpeed)+ (target.posZ-this.posZ)*redirectionSpeed).normalize();
				this.motionX = newMotion.xCoord*1.5;
				this.motionY = newMotion.yCoord*1.5;
				this.motionZ = newMotion.zCoord*1.5;
			}
			else if(this.hookMode==HookMode.REELING && this.getDistanceSqToEntity(target)>4 && speed<1)
				speed += .125;
			else if(this.hookMode==HookMode.LAUNCHING)
				speed = .5;
		}
	}

	@Override
	public void setDead()
	{
		super.setDead();
		if(getShooter() instanceof EntityPlayer)
		{
			EntityGrapplingHook[] hooks = ManeuverGearHelper.getHooks((EntityPlayer)getShooter());
			int iHook = this.getHookNrSynced();
			if(hooks!=null && iHook>0&&iHook<hooks.length)
				hooks[iHook] = null;
		}
	}


	@Override
	public void onImpact(MovingObjectPosition mop)
	{
	}

	@Override
	public void onCollideWithPlayer(EntityPlayer player)
	{
	}


	@Override
	public void writeEntityToNBT(NBTTagCompound nbt)
	{
		super.writeEntityToNBT(nbt);
		nbt.setInteger("hookNr", hookNr);
		nbt.setInteger("mode", hookMode.ordinal());
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt)
	{
		super.readEntityFromNBT(nbt);
		hookNr = nbt.getInteger("hookNr");
		hookMode = HookMode.values()[nbt.getInteger("mode")];
	}
}