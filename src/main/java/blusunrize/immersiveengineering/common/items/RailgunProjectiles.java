/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.api.IETags;
import blusunrize.immersiveengineering.api.tool.RailgunHandler;
import blusunrize.immersiveengineering.api.tool.RailgunHandler.RailgunRenderColors;
import blusunrize.immersiveengineering.common.entities.SawbladeEntity;
import blusunrize.immersiveengineering.common.register.IEItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractArrow.Pickup;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.fml.ModList;
import se.mickelus.tetra.blocks.forged.chthonic.ChthonicExtractorBlock;
import se.mickelus.tetra.blocks.forged.chthonic.ExtractorProjectileEntity;
import se.mickelus.tetra.items.forged.ItemBeam;

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
					((EntityHitResult)target).getEntity().setSecondsOnFire(5);
			}

			@Override
			public double getBreakChance(@Nullable UUID shooter, ItemStack ammo)
			{
				return 1;
			}
		}.setColorMap(new RailgunRenderColors(0xfff32d, 0xffc100, 0xb36b19, 0xbf5a00, 0xbf5a00, 0x953300)));

		// Sawblade
		RailgunHandler.registerProjectile(() -> Ingredient.of(IEItems.Tools.SAWBLADE), new RailgunHandler.IRailgunProjectile()
		{
			@Override
			public Entity getProjectile(@Nullable Player shooter, ItemStack ammo, Entity defaultProjectile)
			{
				Vec3 look = shooter.getLookAngle();
				return new SawbladeEntity(shooter.getCommandSenderWorld(), shooter, look.x*20, look.y*20, look.z*20, ammo);
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
					ammo.hurtAndBreak(1, shooter, (player) -> player.broadcastBreakEvent(shooter.getUsedItemHand()));
					ThrownTrident trident = new ThrownTrident(shooter.level, shooter, ammo);
					trident.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, 2.5F, 1.0F);
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
					ThrownEnderpearl pearl = new ThrownEnderpearl(shooter.level, shooter);
					pearl.setItem(ammo);
					pearl.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, 2.5F, 1.0F);
					return pearl;
				}
				return defaultProjectile;
			}
		});

		if(ModList.get().isLoaded("tetra"))
		{
			// Salvaged Beam
			RailgunHandler.registerProjectile(() -> Ingredient.of(ItemBeam.instance), new RailgunHandler.StandardRailgunProjectile(40, 1.5)
					.setColorMap(new RailgunRenderColors(0x383838, 0x383838, 0x383838, 0x383838, 0x2f2f2f, 0x252525)));

			// Extractor
			RailgunHandler.registerProjectile(() -> Ingredient.of(ChthonicExtractorBlock.item, ChthonicExtractorBlock.usedItem), new RailgunHandler.IRailgunProjectile()
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
						ExtractorProjectileEntity extractor = new ExtractorProjectileEntity(shooter.level, shooter, ammo);
						if(shooter.getAbilities().instabuild)
							extractor.pickup = Pickup.CREATIVE_ONLY;
						extractor.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot(), 0.0F, 2.5F, 1.0F);
						return extractor;
					}
					return defaultProjectile;
				}
			});
		}
	}
}
