/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items.bullets;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.CodecsAndDefault;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.entities.WolfpackShotEntity;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.datafixers.util.Unit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class WolfpackBullet extends BulletHandler.DamagingBullet<Unit>
{
	public WolfpackBullet()
	{
		super(
				CodecsAndDefault.UNIT,
				(projectile, shooter, hit) -> IEDamageSources.causeWolfpackDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Wolfpack::get,
				() -> BulletHandler.emptyShell.asItem().getDefaultInstance(),
				IEApi.ieLoc("item/bullet_wolfpack")
		);
	}

	@Override
	public void onHitTarget(Level world, HitResult target, UUID shooterUUID, Entity projectile, boolean headshot)
	{
		super.onHitTarget(world, target, shooterUUID, projectile, headshot);
		Vec3 v = projectile.getDeltaMovement().scale(-1);
		int split = 6;
		for(int i = 0; i < split; i++)
		{
			float angle = i*(360f/split);
			Matrix4 matrix = new Matrix4();
			matrix.rotate(angle, v.x, v.y, v.z);
			Vec3 vecDir = new Vec3(0, 1, 0);
			vecDir = matrix.apply(vecDir);

			WolfpackShotEntity bullet;
			Entity shooter = null;
			if(shooterUUID!=null&&world instanceof ServerLevel serverLevel)
				shooter = serverLevel.getEntity(shooterUUID);
			if(shooter instanceof LivingEntity living)
				bullet = new WolfpackShotEntity(world, living, vecDir.x*1.5, vecDir.y*1.5, vecDir.z*1.5, this);
			else
				bullet = new WolfpackShotEntity(world, 0, 0, 0, 0, 0, 0, this);
			if(target instanceof EntityHitResult)
			{
				EntityHitResult eTarget = (EntityHitResult)target;
				if(eTarget.getEntity() instanceof LivingEntity)
					bullet.targetOverride = (LivingEntity)eTarget.getEntity();
			}
			bullet.setPos(target.getLocation().x+vecDir.x, target.getLocation().y+vecDir.y, target.getLocation().z+vecDir.z);
			bullet.setDeltaMovement(vecDir.scale(.375));
			world.addFreshEntity(bullet);
		}
	}
}
