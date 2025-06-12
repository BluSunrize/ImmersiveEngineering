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
import blusunrize.immersiveengineering.api.tool.BulletHandler.DamagingBullet;
import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.api.utils.codec.IEDualCodecs;
import blusunrize.immersiveengineering.common.config.IEServerConfig;
import blusunrize.immersiveengineering.common.entities.RevolvershotEntity;
import blusunrize.immersiveengineering.common.items.bullets.PotionBullet.Data;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import io.netty.buffer.ByteBuf;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import malte0811.dualcodecs.DualCompositeCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.UUID;

public class PotionBullet extends DamagingBullet<Data>
{
	public PotionBullet()
	{
		super(
				new CodecsAndDefault<>(Data.CODECS, Data.EMPTY),
				(projectile, shooter, hit) -> IEDamageSources.causePotionDamage((RevolvershotEntity)projectile, shooter),
				IEServerConfig.TOOLS.bulletDamage_Potion::get,
				() -> BulletHandler.emptyCasing.asItem().getDefaultInstance(),
				IEApi.ieLoc("item/bullet_potion"), IEApi.ieLoc("item/bullet_potion_layer")
		);
	}

	@Override
	public String getTranslationKey(Data potion, String baseName)
	{
		if(potion.contents.hasEffects())
		{
			if(potion.type==PotionType.LINGERING)
				baseName += ".linger";
			else if(potion.type==PotionType.SPLASH)
				baseName += ".splash";
		}
		return baseName;
	}

	@Override
	public void onHitTarget(Level world, HitResult target, UUID shooterUUID, Entity projectile, boolean headshot, Data bulletData)
	{
		super.onHitTarget(world, target, shooterUUID, projectile, headshot, bulletData);
		RevolvershotEntity bullet = (RevolvershotEntity)projectile;
		if(bulletData.contents!=null&&bulletData.contents.hasEffects())
		{
			var effects = bulletData.contents.getAllEffects();
			LivingEntity shooter = null;
			if(shooterUUID!=null&&world instanceof ServerLevel serverLevel)
			{
				Entity e = serverLevel.getEntity(shooterUUID);
				if(e == null && projectile instanceof Projectile p)
					e = p.getOwner();
				if(e instanceof LivingEntity)
					shooter = (LivingEntity)e;
			}
			if(bulletData.type==PotionType.LINGERING)
			{
				AreaEffectCloud entityareaeffectcloud = new AreaEffectCloud(bullet.level(), bullet.getX(), bullet.getY(), bullet.getZ());
				entityareaeffectcloud.setOwner(shooter);
				entityareaeffectcloud.setRadius(3.0F);
				entityareaeffectcloud.setRadiusOnUse(-0.5F);
				entityareaeffectcloud.setWaitTime(10);
				entityareaeffectcloud.setRadiusPerTick(-entityareaeffectcloud.getRadius()/(float)entityareaeffectcloud.getDuration());
				entityareaeffectcloud.setPotionContents(bulletData.contents);
				for(MobEffectInstance potioneffect : effects)
					entityareaeffectcloud.addEffect(new MobEffectInstance(potioneffect.getEffect(), potioneffect.getDuration(), potioneffect.getAmplifier()));
				bullet.level().addFreshEntity(entityareaeffectcloud);
			}
			else if(bulletData.type==PotionType.SPLASH)
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
									if(p.getEffect().value().isInstantenous())
										p.getEffect().value().applyInstantenousEffect(bullet, shooter, living, p.getAmplifier(), dist2);
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
			world.levelEvent(2002, bullet.blockPosition(), bulletData.contents.getColor());
		}
	}


	@Override
	public void addTooltip(Data data, TooltipContext world, List<Component> list, TooltipFlag flag)
	{
		if(data.contents.hasEffects())
			data.contents.addPotionTooltip(list::add, 0.25F, world.tickRate());
	}

	@Override
	public Color4 getColour(Data data, int layer)
	{
		if(layer==1)
			return Color4.fromRGB(data.contents.getColor());
		return Color4.WHITE;
	}

	public record Data(PotionContents contents, PotionType type)
	{
		public static final DualCodec<RegistryFriendlyByteBuf, Data> CODECS = DualCompositeCodecs.composite(
				DualCodecs.POTION_CONTENTS.fieldOf("contents"), Data::contents,
				PotionType.CODECS.fieldOf("type"), Data::type,
				Data::new
		);
		public static final Data EMPTY = new Data(PotionContents.EMPTY, PotionType.DEFAULT);

		public ItemStack makePotion()
		{
			ItemStack potion = new ItemStack(switch(this.type)
			{
				case SPLASH -> Items.SPLASH_POTION;
				case LINGERING -> Items.LINGERING_POTION;
				case DEFAULT -> Items.POTION;
			});
			potion.set(DataComponents.POTION_CONTENTS, contents);
			return potion;
		}
	}

	public enum PotionType
	{
		SPLASH,
		LINGERING,
		DEFAULT;

		public static final DualCodec<ByteBuf, PotionType> CODECS = IEDualCodecs.forEnum(values());
	}
}
