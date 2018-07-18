/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class EntityWolfpackShot extends EntityRevolvershotHoming
{
	public EntityWolfpackShot(World world)
	{
		super(world);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	public EntityWolfpackShot(World world, double x, double y, double z, double ax, double ay, double az, IBullet type)
	{
		super(world, x, y, z, ax, ay, az, type);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	public EntityWolfpackShot(World world, EntityLivingBase living, double ax, double ay, double az, IBullet type, ItemStack stack)
	{
		super(world, living, ax, ay, az, type, stack);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	@Override
	protected void onImpact(RayTraceResult mop)
	{
		if(!this.world.isRemote&&mop.entityHit!=null)
		{
			if(mop.entityHit.hurtResistantTime > 0)
				mop.entityHit.hurtResistantTime = 0;
			mop.entityHit.attackEntityFrom(IEDamageSources.causeWolfpackDamage(this, shootingEntity), IEConfig.Tools.bulletDamage_WolfpackPart);
		}
		this.setDead();
	}
}