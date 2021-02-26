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
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EntityType.Builder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class WolfpackShotEntity extends RevolvershotHomingEntity
{
	public static final EntityType<WolfpackShotEntity> TYPE = Builder
			.<WolfpackShotEntity>create(WolfpackShotEntity::new, EntityClassification.MISC)
			.size(0.125f, 0.125f)
			.build(ImmersiveEngineering.MODID+":revolver_shot_wolfpack");

	static
	{
		TYPE.setRegistryName(ImmersiveEngineering.MODID, "revolver_shot_wolfpack");
	}

	public WolfpackShotEntity(EntityType<WolfpackShotEntity> type, World world)
	{
		super(type, world);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	public WolfpackShotEntity(World world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(TYPE, world, x, y, z, ax, ay, az, type);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	public WolfpackShotEntity(World world, LivingEntity living, double ax, double ay, double az, IBullet type)
	{
		super(TYPE, world, living, ax, ay, az, type);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	@Override
	public void onImpact(RayTraceResult mop)
	{
		if(!this.world.isRemote&&mop instanceof EntityRayTraceResult)
		{
			Entity hit = ((EntityRayTraceResult)mop).getEntity();
			if(hit.hurtResistantTime > 0)
				hit.hurtResistantTime = 0;
			Entity shooter = shooterUUID!=null?world.getPlayerByUuid(shooterUUID): null;
			hit.attackEntityFrom(IEDamageSources.causeWolfpackDamage(this, shooter),
					IEServerConfig.TOOLS.bulletDamage_WolfpackPart.get().floatValue());
		}
		this.remove();
	}
}