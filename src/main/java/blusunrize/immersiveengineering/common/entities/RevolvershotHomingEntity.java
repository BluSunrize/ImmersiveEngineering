/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class RevolvershotHomingEntity extends RevolvershotEntity
{
	public static final EntityType<RevolvershotHomingEntity> TYPE = Builder
			.<RevolvershotHomingEntity>create(RevolvershotHomingEntity::new, EntityClassification.MISC)
			.size(0.125f, 0.125f)
			.build(ImmersiveEngineering.MODID+":revolver_shot_homing");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "revolver_shot_homing");
	}

	public int trackCountdown = 5;
	public double redirectionSpeed = .25;
	public LivingEntity targetOverride;

	public RevolvershotHomingEntity(EntityType<? extends RevolvershotHomingEntity> type, World world)
	{
		super(type, world);
	}

	public RevolvershotHomingEntity(EntityType<? extends RevolvershotHomingEntity> eType, World world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(eType, world, null, x, y, z, ax, ay, az, type);
	}

	public RevolvershotHomingEntity(World world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		this(TYPE, world, x, y, z, ax, ay, az, type);
	}

	public RevolvershotHomingEntity(World world, LivingEntity living, double ax, double ay, double az, IBullet type)
	{
		super(TYPE, world, living, ax, ay, az, type);
	}

	public RevolvershotHomingEntity(EntityType<? extends RevolvershotHomingEntity> type, World world, LivingEntity living, double ax, double ay, double az, IBullet type1)
	{
		super(type, world, living, ax, ay, az, type1);
	}

	@Override
	public void tick()
	{
		super.tick();

		if(!world.isRemote&&this.ticksExisted > trackCountdown)
		{
			LivingEntity target = getTarget();
			if(target!=null)
			{
				Vector3d oldMotion = getMotion();
				Vector3d newMotion = new Vector3d(
						oldMotion.x*(1-redirectionSpeed)+(target.getPosX()-this.getPosX())*redirectionSpeed,
						oldMotion.y*(1-redirectionSpeed)+((target.getPosY()+target.getHeight()/2)-this.getPosY())*redirectionSpeed,
						oldMotion.z*(1-redirectionSpeed)+(target.getPosZ()-this.getPosZ())*redirectionSpeed).normalize();

				setMotion(newMotion);
			}
		}
	}

	public LivingEntity getTarget()
	{
		if(targetOverride!=null&&targetOverride.isAlive())
			return targetOverride;
		double r = 20D;
		AxisAlignedBB aabb = new AxisAlignedBB(getPosX()-r, getPosY()-r, getPosZ()-r, getPosX()+r, getPosY()+r, getPosZ()+r);
		LivingEntity target = null;
		for(Object o : world.getEntitiesWithinAABB(LivingEntity.class, aabb))
			if(o instanceof LivingEntity&&!((LivingEntity)o).getUniqueID().equals(this.shootingEntity))
				if(target==null||((LivingEntity)o).getDistanceSq(this) < target.getDistanceSq(this))
					target = (LivingEntity)o;
		return target;
	}
}