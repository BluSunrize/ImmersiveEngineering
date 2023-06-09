/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.BulletHandler.IBullet;
import blusunrize.immersiveengineering.api.tool.ShieldDisablingHandler;
import blusunrize.immersiveengineering.client.utils.FontUtils;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.entities.RevolvershotFlareEntity;
import blusunrize.immersiveengineering.common.entities.RevolvershotHomingEntity;
import blusunrize.immersiveengineering.common.entities.WolfpackShotEntity;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.IESounds;
import blusunrize.immersiveengineering.common.util.ItemNBTHelper;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;

public class BulletItem extends IEBaseItem implements IColouredItem
{
	public static final ResourceLocation CASULL = new ResourceLocation(ImmersiveEngineering.MODID, "casull");
	public static final ResourceLocation ARMOR_PIERCING = new ResourceLocation(ImmersiveEngineering.MODID, "armor_piercing");
	public static final ResourceLocation BUCKSHOT = new ResourceLocation(ImmersiveEngineering.MODID, "buckshot");
	public static final ResourceLocation HIGH_EXPLOSIVE = new ResourceLocation(ImmersiveEngineering.MODID, "he");
	public static final ResourceLocation SILVER = new ResourceLocation(ImmersiveEngineering.MODID, "silver");
	public static final ResourceLocation DRAGONS_BREATH = new ResourceLocation(ImmersiveEngineering.MODID, "dragons_breath");
	public static final ResourceLocation POTION = new ResourceLocation(ImmersiveEngineering.MODID, "potion");
	public static final ResourceLocation FLARE = new ResourceLocation(ImmersiveEngineering.MODID, "flare");
	public static final ResourceLocation FIREWORK = new ResourceLocation(ImmersiveEngineering.MODID, "firework");
	public static final ResourceLocation HOMING = new ResourceLocation(ImmersiveEngineering.MODID, "homing");
	public static final ResourceLocation WOLFPACK = new ResourceLocation(ImmersiveEngineering.MODID, "wolfpack");
	public static final ResourceLocation WOLFPACK_PART = new ResourceLocation(ImmersiveEngineering.MODID, "wolfpack_part");

	private final IBullet type;

	public BulletItem(IBullet type)
	{
		super();
		this.type = type;
	}

	public static void initBullets()
	{
		BulletHandler.registerBullet(CASULL, new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causeCasullDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Casull::get,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				new ResourceLocation("immersiveengineering:item/bullet_casull")));

		BulletHandler.registerBullet(ARMOR_PIERCING, new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causePiercingDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_AP::get,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				new ResourceLocation("immersiveengineering:item/bullet_armor_piercing")));

		BulletHandler.registerBullet(BUCKSHOT, new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causeBuckshotDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Buck::get,
				true,
				false,
				() -> BulletHandler.emptyShell.asItem().getDefaultInstance(),
				new ResourceLocation("immersiveengineering:item/bullet_buckshot"))
		{
			@Override
			public int getProjectileCount(Player shooter)
			{
				return 10;
			}

			@Override
			public void onHitTarget(Level world, HitResult rtr, @Nullable UUID shooterUUID, Entity projectile, boolean headshot)
			{
				super.onHitTarget(world, rtr, shooterUUID, projectile, headshot);
				if(rtr instanceof EntityHitResult target&&target.getEntity() instanceof LivingEntity livingTarget)
					if(livingTarget.isBlocking() && livingTarget.getRandom().nextFloat()<.15f)
						ShieldDisablingHandler.attemptDisabling(livingTarget);
			}
		});

		BulletHandler.registerBullet(HIGH_EXPLOSIVE, new BulletHandler.DamagingBullet(null, 0, () -> BulletHandler.emptyCasing.asItem().getDefaultInstance(), new ResourceLocation("immersiveengineering:item/bullet_he"))
		{
			@Override
			public void onHitTarget(Level world, HitResult target, UUID shooterId, Entity projectile, boolean headshot)
			{
				Entity shooter = null;
				if(shooterId!=null&&world instanceof ServerLevel serverLevel)
					shooter = serverLevel.getEntity(shooterId);
				world.explode(shooter, projectile.getX(), projectile.getY(), projectile.getZ(), 2, ExplosionInteraction.MOB);
			}

			@Override
			public Entity getProjectile(@Nullable Player shooter, ItemStack cartridge, Entity projectile, boolean charged)
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
				return IESounds.revolverFireThump.get();
			}
		});

		BulletHandler.registerBullet(SILVER, new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causeSilverDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Silver::get,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				new ResourceLocation("immersiveengineering:item/bullet_silver"))
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

		BulletHandler.registerBullet(DRAGONS_BREATH, new BulletHandler.DamagingBullet(
				(projectile, shooter, hit) -> IEDamageSources.causeDragonsbreathDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Dragon::get,
				true,
				true,
				() -> BulletHandler.emptyShell.asItem().getDefaultInstance(),
				new ResourceLocation("immersiveengineering:item/bullet_dragons_breath"))
		{
			@Override
			public int getProjectileCount(Player shooter)
			{
				return 30;
			}

			@Override
			public Entity getProjectile(Player shooter, ItemStack cartridge, Entity projectile, boolean electro)
			{
				((RevolvershotEntity)projectile).setTickLimit(10);
				projectile.setSecondsOnFire(3);
				return projectile;
			}
		});

		BulletHandler.registerBullet(POTION, new PotionBullet());

		BulletHandler.registerBullet(FLARE, new FlareBullet());

		BulletHandler.registerBullet(FIREWORK, new FireworkBullet());

		BulletHandler.registerBullet(HOMING, new HomingBullet(IEServerConfig.TOOLS.bulletDamage_Homing::get,
				new ResourceLocation("immersiveengineering:item/bullet_homing")));

		BulletHandler.registerBullet(WOLFPACK, new WolfpackBullet());

		BulletHandler.registerBullet(WOLFPACK_PART, new WolfpackPartBullet());
	}

	@Override
	public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
	{
		type.addTooltip(stack, world, list, flag);
	}

	@Nonnull
	@Override
	public Component getName(@Nonnull ItemStack stack)
	{
		String s = "item.immersiveengineering.bullet.";
		String key = BuiltInRegistries.ITEM.getKey(this).getPath();
		s += key;
		s = type.getTranslationKey(stack, s);
		return Component.translatable(s);
	}

	@Override
	public int getColourForIEItem(ItemStack stack, int pass)
	{
		return type.getColour(stack, pass);
	}

	public IBullet getType()
	{
		return type;
	}

	public static class PotionBullet extends BulletHandler.DamagingBullet
	{
		public PotionBullet()
		{
			super((projectile, shooter, hit) -> IEDamageSources.causePotionDamage((RevolvershotEntity)projectile, shooter),
					IEServerConfig.TOOLS.bulletDamage_Potion::get,
					() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
					new ResourceLocation("immersiveengineering:item/bullet_potion"), new ResourceLocation("immersiveengineering:item/bullet_potion_layer"));
		}

		@Override
		public String getTranslationKey(ItemStack cartridge, String baseName)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(cartridge, "potion");
			if(!pot.isEmpty())
				if(pot.getItem() instanceof LingeringPotionItem)
					baseName += ".linger";
				else if(pot.getItem() instanceof SplashPotionItem)
					baseName += ".splash";
			return baseName;
		}

		@Override
		public Entity getProjectile(Player shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			((RevolvershotEntity)projectile).bulletPotion = ItemNBTHelper.getItemStack(cartridge, "potion");
			return projectile;
		}

		@Override
		public void onHitTarget(Level world, HitResult target, UUID shooterUUID, Entity projectile, boolean headshot)
		{
			super.onHitTarget(world, target, shooterUUID, projectile, headshot);
			RevolvershotEntity bullet = (RevolvershotEntity)projectile;
			if(!bullet.bulletPotion.isEmpty()&&bullet.bulletPotion.hasTag())
			{
				Potion potionType = PotionUtils.getPotion(bullet.bulletPotion);
				List<MobEffectInstance> effects = PotionUtils.getMobEffects(bullet.bulletPotion);
				LivingEntity shooter = null;
				if(shooterUUID!=null&&world instanceof ServerLevel serverLevel)
				{
					Entity e = serverLevel.getEntity(shooterUUID);
					if(e instanceof LivingEntity)
						shooter = (LivingEntity)e;
				}
				if(effects!=null)
					if(bullet.bulletPotion.getItem() instanceof LingeringPotionItem)
					{
						AreaEffectCloud entityareaeffectcloud = new AreaEffectCloud(bullet.level(), bullet.getX(), bullet.getY(), bullet.getZ());
						entityareaeffectcloud.setOwner(shooter);
						entityareaeffectcloud.setRadius(3.0F);
						entityareaeffectcloud.setRadiusOnUse(-0.5F);
						entityareaeffectcloud.setWaitTime(10);
						entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius()/(float)entityareaeffectcloud.getDuration());
						entityareaeffectcloud.setPotion(potionType);
						for(MobEffectInstance potioneffect : effects)
							entityareaeffectcloud.addEffect(new MobEffectInstance(potioneffect.getEffect(), potioneffect.getDuration(), potioneffect.getAmplifier()));
						bullet.level().addFreshEntity(entityareaeffectcloud);
					}
					else if(bullet.bulletPotion.getItem() instanceof SplashPotionItem)
					{
						List<LivingEntity> livingEntities = bullet.level().getEntitiesOfClass(LivingEntity.class, bullet.getBoundingBox().inflate(4.0D, 2.0D, 4.0D));
						if(livingEntities!=null&&!livingEntities.isEmpty())
							for(LivingEntity living : livingEntities)
								if(living.isAffectedByPotions())
								{
									double dist = bullet.distanceToSqr(living);
									if(dist < 16D)
									{
										double dist2 = 1-Math.sqrt(dist)/4D;
										if(target instanceof EntityHitResult&&living==((EntityHitResult)target).getEntity())
											dist2 = 1D;
										for(MobEffectInstance p : effects)
											if(p.getEffect().isInstantenous())
												p.getEffect().applyInstantenousEffect(bullet, shooter, living, p.getAmplifier(), dist2);
											else
											{
												int j = (int)(dist2*p.getDuration()+.5D);
												if(j > 20)
													living.addEffect(new MobEffectInstance(p.getEffect(), j, p.getAmplifier()));
											}
									}
								}

					}
					else if(target instanceof EntityHitResult&&((EntityHitResult)target).getEntity() instanceof LivingEntity)
						for(MobEffectInstance p : effects)
						{
							if(p.getDuration() < 1)
								p = new MobEffectInstance(p.getEffect(), 1);
							((LivingEntity)((EntityHitResult)target).getEntity()).addEffect(p);
						}
				world.levelEvent(2002, bullet.blockPosition(), PotionUtils.getColor(potionType));
			}
		}


		@Override
		public void addTooltip(ItemStack stack, Level world, List<Component> list, TooltipFlag flag)
		{
			ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
			if(!pot.isEmpty()&&pot.getItem() instanceof PotionItem)
				PotionUtils.addPotionTooltip(pot, list, 1f);
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			if(layer==1)
			{
				ItemStack pot = ItemNBTHelper.getItemStack(stack, "potion");
				return pot.isEmpty()?0xff385dc6: PotionUtils.getColor(PotionUtils.getMobEffects(pot));
			}
			return 0xffffffff;
		}
	}

	public static class FlareBullet implements BulletHandler.IBullet
	{
		static ResourceLocation[] textures = {new ResourceLocation("immersiveengineering:item/bullet_flare"), new ResourceLocation("immersiveengineering:item/bullet_flare_layer")};

		public FlareBullet()
		{
		}

		@Override
		public Entity getProjectile(Player shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			RevolvershotFlareEntity flare = shooter!=null?new RevolvershotFlareEntity(projectile.level(), shooter,
					projectile.getDeltaMovement().x*1.5,
					projectile.getDeltaMovement().y*1.5,
					projectile.getDeltaMovement().z*1.5, this, cartridge):
					new RevolvershotFlareEntity(projectile.level(), projectile.getX(), projectile.getY(), projectile.getZ(), 0, 0, 0, this);
			flare.setDeltaMovement(projectile.getDeltaMovement());
			flare.bulletElectro = electro;
			flare.colour = this.getColour(cartridge, 1);
			flare.setColourSynced();
			return flare;
		}

		@Override
		public void onHitTarget(Level world, HitResult target, UUID shooter, Entity projectile, boolean headshot)
		{
		}

		@Override
		public ItemStack getCasing(ItemStack stack)
		{
			return BulletHandler.emptyShell.asItem().getDefaultInstance();
		}

		@Override
		public ResourceLocation[] getTextures()
		{
			return textures;
		}

		@Override
		public void addTooltip(ItemStack stack, Level world, List<Component> list, TooltipFlag flag)
		{
			if(stack.getItem() instanceof IColouredItem)
			{
				int color = ((IColouredItem)stack.getItem()).getColourForIEItem(stack, 1);
				list.add(FontUtils.withAppendColoredColour(
						Component.translatable(Lib.DESC_INFO+"bullet.flareColour"),
						color
				));
			}
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			if(layer!=1)
				return 0xffffffff;
			return ItemNBTHelper.hasKey(stack, "flareColour")?ItemNBTHelper.getInt(stack, "flareColour"): 0xcc2e06;
		}

		@Override
		public boolean isValidForTurret()
		{
			return true;
		}
	}

	public static class FireworkBullet implements BulletHandler.IBullet
	{
		static ResourceLocation[] textures = {new ResourceLocation("immersiveengineering:item/bullet_firework")};

		public FireworkBullet()
		{
		}

		@Override
		public Entity getProjectile(Player shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			ItemStack fireworkStack = new ItemStack(Items.FIREWORK_ROCKET);
			fireworkStack.setTag(cartridge.hasTag()?cartridge.getTag().copy(): null);
			FireworkRocketEntity firework = new FireworkRocketEntity(projectile.level(), fireworkStack, projectile.getX(), projectile.getY(), projectile.getZ(), true);
			Vec3 vector = projectile.getDeltaMovement();
			firework.shoot(vector.x(), vector.y(), vector.z(), 1.6f, 1.0f);
			return firework;
		}

		@Override
		public SoundEvent getSound()
		{
			return IESounds.revolverFireThump.get();
		}

		@Override
		public void onHitTarget(Level world, HitResult target, UUID shooter, Entity projectile, boolean headshot)
		{
		}

		@Override
		public ItemStack getCasing(ItemStack stack)
		{
			return BulletHandler.emptyShell.asItem().getDefaultInstance();
		}

		@Override
		public ResourceLocation[] getTextures()
		{
			return textures;
		}

		@Override
		public void addTooltip(ItemStack stack, Level world, List<Component> list, TooltipFlag flag)
		{
			Items.FIREWORK_ROCKET.appendHoverText(stack, world, list, flag);
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}

		@Override
		public boolean isValidForTurret()
		{
			return true;
		}
	}

	public static class HomingBullet extends BulletHandler.DamagingBullet
	{
		public HomingBullet(DoubleSupplier damage, ResourceLocation... textures)
		{
			super((projectile, shooter, hit) -> IEDamageSources.causeHomingDamage((RevolvershotEntity)projectile, shooter),
					damage,
					() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
					textures);
		}

		@Override
		public Entity getProjectile(Player shooter, ItemStack cartridge, Entity projectile, boolean electro)
		{
			RevolvershotHomingEntity shot = shooter!=null?new RevolvershotHomingEntity(projectile.level(), shooter,
					projectile.getDeltaMovement().x*1.5, projectile.getDeltaMovement().y*1.5, projectile.getDeltaMovement().z*1.5, this): new RevolvershotHomingEntity(projectile.level(), projectile.getX(), projectile.getY(), projectile.getZ(), 0, 0, 0, this);
			shot.setDeltaMovement(projectile.getDeltaMovement());
			shot.bulletElectro = electro;
			return shot;
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}

	public static class WolfpackBullet extends BulletHandler.DamagingBullet
	{
		public WolfpackBullet()
		{
			super((projectile, shooter, hit) -> IEDamageSources.causeWolfpackDamage((RevolvershotEntity)projectile, shooter),
					IEServerConfig.TOOLS.bulletDamage_Wolfpack::get,
					() -> BulletHandler.emptyShell.asItem().getDefaultInstance(),
					new ResourceLocation("immersiveengineering:item/bullet_wolfpack"));
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

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}

	public static class WolfpackPartBullet extends BulletHandler.DamagingBullet
	{
		public WolfpackPartBullet()
		{
			super((projectile, shooter, hit) -> IEDamageSources.causeWolfpackDamage((RevolvershotEntity)projectile, shooter),
					IEServerConfig.TOOLS.bulletDamage_WolfpackPart::get,
					() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
					new ResourceLocation("immersiveengineering:item/bullet_wolfpack"));
		}

		@Override
		public boolean isProperCartridge()
		{
			return false;
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}
}