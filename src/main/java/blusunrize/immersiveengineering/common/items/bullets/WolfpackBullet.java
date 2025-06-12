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
import blusunrize.immersiveengineering.api.utils.PlayerUtils;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.entities.WolfpackShotEntity;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import com.mojang.datafixers.util.Unit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

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
	public void onHitTarget(Level world, HitResult hitResult, UUID shooterUUID, Entity projectile, boolean headshot, Unit bulletData)
	{
		super.onHitTarget(world, hitResult, shooterUUID, projectile, headshot, bulletData);
		// find shooter
		Entity shooter = null;
		if(shooterUUID!=null&&world instanceof ServerLevel serverLevel)
			shooter = serverLevel.getEntity(shooterUUID);
		if(shooter == null && projectile instanceof Projectile p)
			shooter = p.getOwner();
		// find hit entity
		LivingEntity livingHit = (hitResult instanceof EntityHitResult eHit&&eHit.getEntity() instanceof LivingEntity hit)?hit: null;
		// create separate projectiles
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
			if(shooter instanceof LivingEntity living)
				bullet = new WolfpackShotEntity(world, living, vecDir.x*1.5, vecDir.y*1.5, vecDir.z*1.5, this);
			else
				bullet = new WolfpackShotEntity(world, 0, 0, 0, 0, 0, 0, this);
			if(livingHit!=null)
				bullet.targetOverride = livingHit;
			bullet.setPos(hitResult.getLocation().x+vecDir.x, hitResult.getLocation().y+vecDir.y, hitResult.getLocation().z+vecDir.z);
			bullet.setDeltaMovement(vecDir.scale(.375));
			world.addFreshEntity(bullet);
		}
		// trigger & buff allied wolves
		if(shooter instanceof Player player)
			empowerWolves(world, hitResult, player, livingHit);
	}

	public static void empowerWolves(Level world, HitResult hitResult, Player player, @Nullable LivingEntity hitTarget)
	{
		LivingEntity possibleTarget = hitTarget!=null?hitTarget: player.getLastHurtMob();
		AABB searchBox = new AABB(hitResult.getLocation(), hitResult.getLocation()).inflate(32);
		world.getEntitiesOfClass(Wolf.class, searchBox, wolf -> PlayerUtils.isAllied(player, wolf)).forEach(wolf -> {
			// target setting only works for owned wolves, but buffs are given to all allied wolves!
			if(player.equals(wolf.getOwner()) && possibleTarget!=null && wolf.getTarget()==null && !PlayerUtils.isAllied(player, possibleTarget))
				wolf.setTarget(possibleTarget);
			wolf.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200));
			wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 200, 1));
		});
	}
}
