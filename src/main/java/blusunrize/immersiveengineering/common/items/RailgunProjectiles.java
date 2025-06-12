/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.Lib.DamageTypes;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.RailgunRenderColors;
import blusunrize.immersiveengineering.common.entities.SawbladeEntity;
import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.mixin.accessors.DamageSourcesAccess;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.common.Tags;

import javax.annotation.Nullable;
import java.util.UUID;

public class RailgunProjectiles
{
	public static void register()
	{
		// Iron
		RailgunHandler.registerStandardProjectile(IETags.ironRod, 16, 1.25).setColorMap(
				new RailgunRenderColors(0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868)
		);

		// Aluminum
		RailgunHandler.registerStandardProjectile(IETags.aluminumRod, 10, 1.05).setColorMap(
				new RailgunRenderColors(0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868)
		);

		// Steel
		RailgunHandler.registerStandardProjectile(IETags.steelRod, 24, 1.25).setColorMap(
				new RailgunRenderColors(0xb4b4b4, 0xb4b4b4, 0xb4b4b4, 0x7a7a7a, 0x555555, 0x555555)
		);

		// Netherite
		RailgunHandler.registerStandardProjectile(IETags.netheriteRod, 30, 1.25).setColorMap(
				new RailgunRenderColors(0x5a575a, 0x484548, 0x484548, 0x3c3232, 0x31292a, 0x31292a)
		);

		// Graphite
		RailgunHandler.registerStandardProjectile(new ItemStack(IEItems.Misc.GRAPHITE_ELECTRODE), 30, .9).setColorMap(
				new RailgunRenderColors(0x242424, 0x242424, 0x242424, 0x171717, 0x171717, 0x0a0a0a)
		);

		// Blaze Rod
		RailgunHandler.registerProjectile(() -> Ingredient.of(Tags.Items.RODS_BLAZE), new RailgunHandler.StandardRailgunProjectile(10, 1.05)
		{
			@Override
			public void onHitTarget(Level world, HitResult target, @Nullable UUID shooter, Entity projectile)
			{
				if(target instanceof EntityHitResult)
					((EntityHitResult)target).getEntity().igniteForSeconds(5);
			}

			@Override
			public double getBreakChance(@Nullable UUID shooter, ItemStack ammo)
			{
				return 1;
			}
		}.setColorMap(new RailgunRenderColors(0xfff32d, 0xffc100, 0xb36b19, 0xbf5a00, 0xbf5a00, 0x953300)));

		// Breeze Rod
		RailgunHandler.registerProjectile(() -> Ingredient.of(Tags.Items.RODS_BREEZE), new RailgunHandler.StandardRailgunProjectile(16, .9)
		{
			@Override
			public double getDamage(Level world, Entity target, @Nullable UUID shooter, Entity projectile)
			{
				double d = super.getDamage(world, target, shooter, projectile);
				if(target instanceof Breeze)
					d *= 2;
				return d;
			}

			@Override
			public void onHitTarget(Level world, HitResult target, @Nullable UUID shooter, Entity projectile)
			{
				if(target.getType()!=Type.MISS)
					world.explode(projectile, null,
							AbstractWindCharge.EXPLOSION_DAMAGE_CALCULATOR,
							target.getLocation().x, target.getLocation().y, target.getLocation().z,
							3.0F, false,
							ExplosionInteraction.TRIGGER,
							ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE,
							SoundEvents.BREEZE_WIND_CHARGE_BURST
					);
			}

			@Override
			public double getBreakChance(@Nullable UUID shooter, ItemStack ammo)
			{
				return 1;
			}
		}.setColorMap(new RailgunRenderColors(0xbfafc9, 0xb5a5ca, 0x576c9d, 0x3e5e87, 0x3e5e87, 0x2f527f)));

		// End Rod
		RailgunHandler.registerProjectile(() -> Ingredient.of(Items.END_ROD), new RailgunHandler.StandardRailgunProjectile(10, 1.05)
		{
			@Override
			public double getDamage(Level world, Entity target, @Nullable UUID shooter, Entity projectile)
			{
				double d = super.getDamage(world, target, shooter, projectile);
				if(target instanceof EnderMan)
					d *= 2;
				return d;
			}

			@Override
			public DamageSource getDamageSource(Level world, Entity target, @Nullable UUID shooter, Entity projectile)
			{
				if(target instanceof EnderMan enderMan)
				{
					enderMan.addEffect(new MobEffectInstance(IEPotions.STUNNED, 200));
					final DamageSourcesAccess sources = (DamageSourcesAccess)world.damageSources();
					Player p;
					if(shooter!=null&&(p = world.getPlayerByUUID(shooter))!=null)
						return sources.invokeSource(DamageTypes.RAILGUN.turretType(), p, null);
					return sources.invokeSource(DamageTypes.RAILGUN.playerType(), null, null);
				}
				return null;
			}
		}.setColorMap(new RailgunRenderColors(0xf6e2cd, 0xfff6e6, 0xffffff, 0xfff6f6, 0xf6e2cd, 0x736565)));

		// Sawblade
		RailgunHandler.registerProjectile(() -> Ingredient.of(IEItems.Tools.SAWBLADE), new RailgunHandler.IRailgunProjectile()
		{
			@Override
			public Entity getProjectile(@Nullable Player shooter, ItemStack ammo, Entity defaultProjectile)
			{
				return new SawbladeEntity(defaultProjectile.level(), shooter, 20, 0, ammo);
			}
		});

		// Trident
		RailgunHandler.registerProjectile(() -> Ingredient.of(Items.TRIDENT), new RailgunHandler.IRailgunProjectile()
		{
			@Override
			public boolean isValidForTurret()
			{
				return false;
			}

			@Override
			public Entity getProjectile(@Nullable Player shooter, ItemStack ammo, Entity defaultProjectile)
			{
				if(shooter!=null)
				{
					ammo.hurtAndBreak(1, shooter, EquipmentSlot.MAINHAND);
					ThrownTrident trident = new ThrownTrident(shooter.level(), shooter, ammo);
					trident.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, 5F, 0F);
					if(shooter.getAbilities().instabuild)
						trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
					return trident;
				}
				return defaultProjectile;
			}
		});

		// Enderpearl
		RailgunHandler.registerProjectile(() -> Ingredient.of(Items.ENDER_PEARL), new RailgunHandler.IRailgunProjectile()
		{
			@Override
			public boolean isValidForTurret()
			{
				return false;
			}

			@Override
			public Entity getProjectile(@Nullable Player shooter, ItemStack ammo, Entity defaultProjectile)
			{
				if(shooter!=null)
				{
					ThrownEnderpearl pearl = new ThrownEnderpearl(shooter.level(), shooter);
					pearl.setItem(ammo);
					pearl.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, 2.5F, 0);
					return pearl;
				}
				return defaultProjectile;
			}
		});
	}
}
