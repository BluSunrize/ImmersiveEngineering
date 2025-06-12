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
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ShieldDisablingHandler;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import com.mojang.datafixers.util.Unit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.UUID;

public class IEBullets
{
	public static final ResourceLocation CASULL = IEApi.ieLoc("casull");
	public static final ResourceLocation ARMOR_PIERCING = IEApi.ieLoc("armor_piercing");
	public static final ResourceLocation BUCKSHOT = IEApi.ieLoc("buckshot");
	public static final ResourceLocation HIGH_EXPLOSIVE = IEApi.ieLoc("he");
	public static final ResourceLocation SILVER = IEApi.ieLoc("silver");
	public static final ResourceLocation DRAGONS_BREATH = IEApi.ieLoc("dragons_breath");
	public static final ResourceLocation POTION = IEApi.ieLoc("potion");
	public static final ResourceLocation FLARE = IEApi.ieLoc("flare");
	public static final ResourceLocation FIREWORK = IEApi.ieLoc("firework");
	public static final ResourceLocation HOMING = IEApi.ieLoc("homing");
	public static final ResourceLocation WOLFPACK = IEApi.ieLoc("wolfpack");
	public static final ResourceLocation WOLFPACK_PART = IEApi.ieLoc("wolfpack_part");

	public static final IBullet<Color4> FLARE_TYPE = new FlareBullet();

	public static void initBullets()
	{
		BulletHandler.registerBullet(CASULL, new BulletHandler.DamagingBullet<>(
				CodecsAndDefault.UNIT,
				(projectile, shooter, hit) -> IEDamageSources.causeCasullDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Casull::get,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				IEApi.ieLoc("item/bullet_casull")));

		BulletHandler.registerBullet(ARMOR_PIERCING, new BulletHandler.DamagingBullet<>(
				CodecsAndDefault.UNIT,
				(projectile, shooter, hit) -> IEDamageSources.causePiercingDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_AP::get,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				IEApi.ieLoc("item/bullet_armor_piercing")));

		BulletHandler.registerBullet(BUCKSHOT, new BulletHandler.DamagingBullet<>(
				CodecsAndDefault.UNIT,
				(projectile, shooter, hit) -> IEDamageSources.causeBuckshotDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Buck::get,
				true,
				false,
				() -> BulletHandler.emptyShell.asItem().getDefaultInstance(),
				IEApi.ieLoc("item/bullet_buckshot"))
		{
			@Override
			public int getProjectileCount(Player shooter)
			{
				return 10;
			}

			@Override
			public void onHitTarget(Level world, HitResult rtr, @Nullable UUID shooterUUID, Entity projectile, boolean headshot, Unit bulletData)
			{
				super.onHitTarget(world, rtr, shooterUUID, projectile, headshot, bulletData);
				if(rtr instanceof EntityHitResult target&&target.getEntity() instanceof LivingEntity livingTarget)
					if(livingTarget.isBlocking()&&livingTarget.getRandom().nextFloat() < .15f)
						ShieldDisablingHandler.attemptDisabling(livingTarget);
			}
		});

		BulletHandler.registerBullet(HIGH_EXPLOSIVE, new BulletHandler.DamagingBullet<>(
				CodecsAndDefault.UNIT, null, 0, () -> BulletHandler.emptyShell.asItem().getDefaultInstance(), IEApi.ieLoc("item/bullet_he")
		)
		{
			@Override
			public void onHitTarget(Level world, HitResult target, UUID shooterId, Entity projectile, boolean headshot, Unit bulletData)
			{
				Entity shooter = null;
				if(shooterId!=null&&world instanceof ServerLevel serverLevel)
					shooter = serverLevel.getEntity(shooterId);
				if(shooter == null && projectile instanceof Projectile p)
					shooter = p.getOwner();
				world.explode(shooter, projectile.getX(), projectile.getY(), projectile.getZ(), 2, ExplosionInteraction.MOB);
			}

			@Override
			public Entity getProjectile(@Nullable Player shooter, Unit ignored, Entity projectile, boolean charged)
			{
				if(projectile instanceof RevolvershotEntity)
				{
					((RevolvershotEntity)projectile).setGravity(0.05f);
					((RevolvershotEntity)projectile).setMovementDecay(0.9f);
				}
				return projectile;
			}

			@Override
			public SoundEvent getSound()
			{
				return IESounds.revolverFireThump.value();
			}
		});

		BulletHandler.registerBullet(SILVER, new BulletHandler.DamagingBullet<>(
				CodecsAndDefault.UNIT,
				(projectile, shooter, hit) -> IEDamageSources.causeSilverDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Silver::get,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				IEApi.ieLoc("item/bullet_silver"))
		{
			@Override
			protected float getDamage(Entity hitEntity, boolean headshot)
			{
				float dmg = super.getDamage(hitEntity, headshot);
				if(hitEntity instanceof LivingEntity&&((LivingEntity)hitEntity).isInvertedHealAndHarm())
					dmg *= 1.5;
				return dmg;
			}
		});

		BulletHandler.registerBullet(DRAGONS_BREATH, new BulletHandler.DamagingBullet<>(
				CodecsAndDefault.UNIT,
				(projectile, shooter, hit) -> IEDamageSources.causeDragonsbreathDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Dragon::get,
				true,
				true,
				() -> BulletHandler.emptyShell.asItem().getDefaultInstance(),
				IEApi.ieLoc("item/bullet_dragons_breath"))
		{
			@Override
			public int getProjectileCount(Player shooter)
			{
				return 30;
			}

			@Override
			public Entity getProjectile(Player shooter, Unit ignored, Entity projectile, boolean electro)
			{
				((RevolvershotEntity)projectile).setTickLimit(10);
				projectile.igniteForSeconds(3);
				return projectile;
			}
		});

		BulletHandler.registerBullet(POTION, new PotionBullet());

		BulletHandler.registerBullet(FLARE, FLARE_TYPE);

		BulletHandler.registerBullet(FIREWORK, new FireworkBullet());

		BulletHandler.registerBullet(HOMING, new HomingBullet(IEServerConfig.TOOLS.bulletDamage_Homing::get,
				IEApi.ieLoc("item/bullet_homing")));

		BulletHandler.registerBullet(WOLFPACK, new WolfpackBullet());

		BulletHandler.registerBullet(WOLFPACK_PART, new WolfpackPartBullet());
	}
}
