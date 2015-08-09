package blusunrize.immersiveengineering.common.entities;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import blusunrize.immersiveengineering.common.Config;
import blusunrize.immersiveengineering.common.util.IEDamageSources;

public class EntityWolfpackShot extends EntityRevolvershotHoming
{
	public EntityWolfpackShot(World world)
	{
		super(world);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}
	public EntityWolfpackShot(World world, double x, double y, double z, double ax, double ay, double az, int type)
	{
		super(world, x, y, z, ax, ay, az, type);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}
	public EntityWolfpackShot(World world, EntityLivingBase living, double ax, double ay, double az, int type, ItemStack stack)
	{
		super(world, living, ax, ay, az, type, stack);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	protected void onImpact(MovingObjectPosition mop)
	{
		if(!this.worldObj.isRemote && mop.entityHit != null)
		{
			if(mop.entityHit.hurtResistantTime>0)
				mop.entityHit.hurtResistantTime=0;
			mop.entityHit.attackEntityFrom(IEDamageSources.causeWolfpackDamage(this, shootingEntity), (float)Config.getDouble("BulletDamage-WolfpackPart"));
		}
		this.setDead();
	}
}