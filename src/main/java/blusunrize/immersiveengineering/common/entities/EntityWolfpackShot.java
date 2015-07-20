package blusunrize.immersiveengineering.common.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class EntityWolfpackShot extends EntityRevolvershot
{
	public EntityWolfpackShot(World world)
	{
		super(world);
	}
	public EntityWolfpackShot(World world, double x, double y, double z, double ax, double ay, double az, int type)
	{
		super(world, x, y, z, ax, ay, az, type);
	}
	public EntityWolfpackShot(World world, EntityLivingBase living, double ax, double ay, double az, int type, ItemStack stack)
	{
		super(world, living, ax, ay, az, type, stack);
	}

	@Override
	public void onUpdate()
	{
		super.onUpdate();

		if(!worldObj.isRemote && this.ticksExisted>15)
		{
			EntityLivingBase target = getTarget();
			if(target!=null)
			{

				double mod = .05;
				Vec3 newMotion = Vec3.createVectorHelper(
						motionX*(1-mod)+ (target.posX-this.posX)*mod,
						motionY*(1-mod)+ ((target.posY+target.height/2)-this.posY)*mod,
						motionZ*(1-mod)+ (target.posZ-this.posZ)*mod).normalize();

				this.motionX = newMotion.xCoord;
				this.motionY = newMotion.yCoord;
				this.motionZ = newMotion.zCoord;
			}
		}
	}

	public EntityLivingBase getTarget()
	{
		double r = 20D;
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(posX-r,posY-r,posZ-r, posX+r,posY+r,posZ+r);
		EntityLivingBase target = null;
		for(Object o: worldObj.getEntitiesWithinAABB(EntityLivingBase.class, aabb))
			if(o instanceof EntityLivingBase && !this.shootingEntity.equals(o))
				if(target==null || ((EntityLivingBase)o).getDistanceSqToEntity(this)<target.getDistanceSqToEntity(this))
					target = (EntityLivingBase)o;
		return target;
	}
}