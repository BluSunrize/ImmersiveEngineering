package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityRevolvershotHoming extends EntityRevolvershot
{
	public int trackCountdown = 5;
	public double redirectionSpeed = .25;
	public EntityLivingBase targetOverride;
	public EntityRevolvershotHoming(World world)
	{
		super(world);
	}

	public EntityRevolvershotHoming(World world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(world, x, y, z, ax, ay, az, type);
	}

	public EntityRevolvershotHoming(World world, EntityLivingBase living, double ax, double ay, double az, IBullet type, ItemStack stack)
	{
		super(world, living, ax, ay, az, type, stack);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote && this.ticksExisted>trackCountdown)
		{
			EntityLivingBase target = getTarget();
			if(target!=null)
			{
				Vec3d newMotion = new Vec3d(
						motionX*(1-redirectionSpeed)+ (target.posX-this.posX)*redirectionSpeed,
						motionY*(1-redirectionSpeed)+ ((target.posY+target.height/2)-this.posY)*redirectionSpeed,
						motionZ*(1-redirectionSpeed)+ (target.posZ-this.posZ)*redirectionSpeed).normalize();

				this.motionX = newMotion.xCoord;
				this.motionY = newMotion.yCoord;
				this.motionZ = newMotion.zCoord;
			}
		}
	}

	public EntityLivingBase getTarget()
	{
		if(targetOverride!=null && !targetOverride.isDead)
			return targetOverride;
		double r = 20D;
		AxisAlignedBB aabb = new AxisAlignedBB(posX-r,posY-r,posZ-r, posX+r,posY+r,posZ+r);
		EntityLivingBase target = null;
		for(Object o: worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb))
			if(o instanceof EntityLivingBase && !o.equals(this.shootingEntity))
				if(target==null || ((EntityLivingBase)o).getDistanceSqToEntity(this)<target.getDistanceSqToEntity(this))
					target = (EntityLivingBase)o;
		return target;
	}
}