/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RailgunHandler
{
	public static List<Pair<Ingredient, IRailgunProjectile>> projectilePropertyMap = new ArrayList<>();

	public static IRailgunProjectile registerProjectile(Ingredient stack, IRailgunProjectile properties)
	{
		projectilePropertyMap.add(Pair.of(stack, properties));
		return properties;
	}

	public static StandardRailgunProjectile registerStandardProjectile(Ingredient stack, double damage, double gravity)
	{
		return (StandardRailgunProjectile)registerProjectile(stack, new StandardRailgunProjectile(damage, gravity));
	}

	public static StandardRailgunProjectile registerStandardProjectile(ItemStack stack, double damage, double gravity)
	{
		return registerStandardProjectile(Ingredient.fromStacks(stack), damage, gravity);
	}

	public static IRailgunProjectile getProjectile(ItemStack stack)
	{
		for(Pair<Ingredient, IRailgunProjectile> pair : projectilePropertyMap)
			if(pair.getLeft().test(stack))
				return pair.getRight();
		return null;
	}

	public interface IRailgunProjectile
	{
		/**
		 * @param shooter the player who shot the projectile. In the case of a turret, this is null
		 * @param ammo the ItemStack used as ammo
		 * @param defaultProjectile the default projectile that should be returned if no custom one is created
		 * @return the given or a custom entity
		 */
		default Entity getProjectile(@Nullable PlayerEntity shooter, ItemStack ammo, Entity defaultProjectile)
		{
			return defaultProjectile;
		}

		/**
		 * @return the gravity by which the projectile is affected
		 */
		default double getGravity()
		{
			return 1;
		}

		/**
		 * @return the damage dealt when impacting an entity
		 */
		default double getDamage(World world, Entity target, @Nullable UUID shooter, Entity projectile)
		{
			return 0;
		}

		/**
		 * Called when the projectile hits a target, either block or entity
		 * When hitting entities, this is executed <b>after</b> damage is applied!
		 */
		default void onHitTarget(World world, RayTraceResult target, @Nullable UUID shooter, Entity projectile)
		{
		}

		/**
		 * @return the chance for the projectile to break after impacting a wall
		 */
		default double getBreakChance(@Nullable UUID shooter, ItemStack ammo)
		{
			return .25;
		}

		/**
		 * @return whether this projectile should be allowed to be placed in, and used from a turret.<br>
		 * Projectiles that rely on a player using them should return false, since turrets pass null for a player
		 */
		default boolean isValidForTurret()
		{
			return false;
		}
	}

	public static class StandardRailgunProjectile implements IRailgunProjectile
	{
		private final double damage;
		private final double gravity;
		private int[][] colourMap = {{0x686868, 0xa4a4a4, 0xa4a4a4, 0xa4a4a4, 0x686868}};

		public StandardRailgunProjectile(double damage, double gravity)
		{
			this.damage = damage;
			this.gravity = gravity;
		}

		public StandardRailgunProjectile setColourMap(int[][] map)
		{
			this.colourMap = map;
			return this;
		}

		public int[][] getColourMap()
		{
			return colourMap;
		}

		@Override
		public double getGravity()
		{
			return this.gravity;
		}

		@Override
		public double getDamage(World world, Entity target, @Nullable UUID shooter, Entity projectile)
		{
			return this.damage;
		}

		@Override
		public boolean isValidForTurret()
		{
			return true;
		}
	}
}