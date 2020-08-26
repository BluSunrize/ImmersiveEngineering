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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.UUID;

public class RailgunProjectiles
{
	public static void register()
	{
		// Iron
		RailgunHandler.registerStandardProjectile(Ingredient.fromTag(IETags.ironRod), 16, 1.25).setColorMap(
				new RailgunRenderColors(0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868)
		);

		// Aluminum
		RailgunHandler.registerStandardProjectile(Ingredient.fromTag(IETags.aluminumRod), 10, 1.05).setColorMap(
				new RailgunRenderColors(0xd8d8d8, 0xd8d8d8, 0xd8d8d8, 0xa8a8a8, 0x686868, 0x686868)
		);

		// Steel
		RailgunHandler.registerStandardProjectile(Ingredient.fromTag(IETags.steelRod), 24, 1.25).setColorMap(
				new RailgunRenderColors(0xb4b4b4, 0xb4b4b4, 0xb4b4b4, 0x7a7a7a, 0x555555, 0x555555)
		);

		// Graphite
		RailgunHandler.registerStandardProjectile(new ItemStack(IEItems.Misc.graphiteElectrode), 30, .9).setColorMap(
				new RailgunRenderColors(0x242424, 0x242424, 0x242424, 0x171717, 0x171717, 0x0a0a0a)
		);

		// Blaze Rod
		RailgunHandler.registerProjectile(Ingredient.fromTag(Tags.Items.RODS_BLAZE), new RailgunHandler.StandardRailgunProjectile(10, 1.05)
		{
			@Override
			public void onHitTarget(World world, RayTraceResult target, @Nullable UUID shooter, Entity projectile)
			{
				if(target instanceof EntityRayTraceResult)
					((EntityRayTraceResult)target).getEntity().setFire(5);
			}

			@Override
			public double getBreakChance(@Nullable UUID shooter, ItemStack ammo)
			{
				return 1;
			}
		}.setColorMap(new RailgunRenderColors(0xfff32d, 0xffc100, 0xb36b19, 0xbf5a00, 0xbf5a00, 0x953300)));

		// Sawblade
		RailgunHandler.registerProjectile(Ingredient.fromItems(IEItems.Tools.sawblade), new RailgunHandler.IRailgunProjectile()
		{
			@Override
			public Entity getProjectile(@Nullable PlayerEntity shooter, ItemStack ammo, Entity defaultProjectile)
			{
				Vec3d look = shooter.getLookVec();
				return new SawbladeEntity(shooter.getEntityWorld(), shooter, look.x*20, look.y*20, look.z*20, ammo);
			}
		});

		// Trident
		RailgunHandler.registerProjectile(Ingredient.fromItems(Items.TRIDENT), new RailgunHandler.IRailgunProjectile()
		{
			@Override
			public boolean isValidForTurret()
			{
				return false;
			}

			@Override
			public Entity getProjectile(@Nullable PlayerEntity shooter, ItemStack ammo, Entity defaultProjectile)
			{
				if(shooter!=null)
				{
					ammo.damageItem(1, shooter, (p_220047_1_) -> p_220047_1_.sendBreakAnimation(shooter.getActiveHand()));
					TridentEntity trident = new TridentEntity(shooter.world, shooter, ammo);
					trident.shoot(shooter, shooter.rotationPitch, shooter.rotationYaw, 0.0F, 2.5F, 1.0F);
					if(shooter.abilities.isCreativeMode)
						trident.pickupStatus = AbstractArrowEntity.PickupStatus.CREATIVE_ONLY;
					return trident;
				}
				return defaultProjectile;
			}
		});
	}
}
