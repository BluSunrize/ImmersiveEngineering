/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.entities;

import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.items.bullets.WolfpackBullet;
import blusunrize.immersiveengineering.common.register.IEEntityTypes;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import com.mojang.datafixers.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class WolfpackShotEntity extends RevolvershotHomingEntity
{
	public WolfpackShotEntity(EntityType<WolfpackShotEntity> type, Level world)
	{
		super(type, world);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	public WolfpackShotEntity(Level world, double x, double y, double z, double ax, double ay, double az, IBullet<Unit> type)
	{
		super(IEEntityTypes.WOLFPACK_SHOT.get(), world, x, y, z, ax, ay, az, type);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	public WolfpackShotEntity(Level world, LivingEntity living, double ax, double ay, double az, IBullet<Unit> type)
	{
		super(IEEntityTypes.WOLFPACK_SHOT.get(), world, living, ax, ay, az, type);
		trackCountdown = 15;
		redirectionSpeed = .1875;
	}

	@Override
	public void onHit(HitResult mop)
	{
		if(!this.level().isClientSide&&mop instanceof EntityHitResult)
		{
			Entity hit = ((EntityHitResult)mop).getEntity();
			if(hit.invulnerableTime > 0)
				hit.invulnerableTime = 0;
			Entity shooter = getOwner();
			hit.hurt(IEDamageSources.causeWolfpackDamage(this, shooter),
					IEServerConfig.TOOLS.bulletDamage_WolfpackPart.get().floatValue());
			if(shooter instanceof Player player)
				WolfpackBullet.empowerWolves(this.level(), mop, player, hit instanceof LivingEntity?(LivingEntity)hit: null);
		}
		this.discard();
	}
}