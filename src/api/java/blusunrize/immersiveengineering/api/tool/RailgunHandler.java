/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import com.google.common.base.Preconditions;
import net.minecraft.tags.Tag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class RailgunHandler
{
	public static List<Pair<Supplier<Ingredient>, IRailgunProjectile>> projectilePropertyMap = new ArrayList<>();

	public static IRailgunProjectile registerProjectile(Supplier<Ingredient> stack, IRailgunProjectile properties)
	{
		projectilePropertyMap.add(Pair.of(stack, properties));
		return properties;
	}

	public static StandardRailgunProjectile registerStandardProjectile(Tag<Item> tag, double damage, double gravity)
	{
		return (StandardRailgunProjectile)registerProjectile(() -> Ingredient.of(tag), new StandardRailgunProjectile(damage, gravity));
	}

	public static StandardRailgunProjectile registerStandardProjectile(ItemStack stack, double damage, double gravity)
	{
		return (StandardRailgunProjectile)registerProjectile(() -> Ingredient.of(stack), new StandardRailgunProjectile(damage, gravity));
	}

	public static IRailgunProjectile getProjectile(ItemStack stack)
	{
		for(Pair<Supplier<Ingredient>, IRailgunProjectile> pair : projectilePropertyMap)
			if(pair.getLeft().get().test(stack))
				return pair.getRight();
		return null;
	}

	public interface IRailgunProjectile
	{
		/**
		 * @param shooter           the player who shot the projectile. In the case of a turret, this is null
		 * @param ammo              the ItemStack used as ammo
		 * @param defaultProjectile the default projectile that should be returned if no custom one is created
		 * @return the given or a custom entity
		 */
		default Entity getProjectile(@Nullable Player shooter, ItemStack ammo, Entity defaultProjectile)
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
		default double getDamage(Level world, Entity target, @Nullable UUID shooter, Entity projectile)
		{
			return 0;
		}

		/**
		 * Called when the projectile hits a target, either block or entity
		 * When hitting entities, this is executed <b>after</b> damage is applied!
		 */
		default void onHitTarget(Level world, HitResult target, @Nullable UUID shooter, Entity projectile)
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
		private RailgunRenderColors colorMap;

		public StandardRailgunProjectile(double damage, double gravity)
		{
			this.damage = damage;
			this.gravity = gravity;
		}

		public StandardRailgunProjectile setColorMap(RailgunRenderColors map)
		{
			this.colorMap = map;
			return this;
		}

		public RailgunRenderColors getColorMap()
		{
			return colorMap;
		}

		@Override
		public double getGravity()
		{
			return this.gravity;
		}

		@Override
		public double getDamage(Level world, Entity target, @Nullable UUID shooter, Entity projectile)
		{
			return this.damage;
		}

		@Override
		public boolean isValidForTurret()
		{
			return true;
		}
	}

	public static class RailgunRenderColors
	{
		// A standard railgun projectile is rendered as a set of rings stacked on eachother
		// Each array in this list describes the gradient of one of those rings
		private final List<int[]> rings;

		private final int gradientLength;

		public RailgunRenderColors(int[]... rings)
		{
			Preconditions.checkArgument(rings.length > 0, "Railgun render colors can not be instantiated with no data");
			this.rings = Arrays.asList(rings);
			this.gradientLength = rings[0].length;
			for(int[] ring : rings)
				Preconditions.checkArgument(ring.length==this.gradientLength, "All rings in Railgun render must have the same length");
		}

		public RailgunRenderColors(int... color)
		{
			this(new int[][]{color});
		}

		public int getRingCount()
		{
			return this.rings.size();
		}

		public int getGradientLength()
		{
			return this.gradientLength;
		}

		private static int[] splitRGB(int rgb)
		{
			return new int[]{(rgb >> 16)&255, (rgb >> 8)&255, rgb&255};
		}

		public int[] getRingColor(int lengthIdx, int widthIdx)
		{
			return splitRGB(this.rings.get(lengthIdx)[widthIdx]);
		}

		public int[] getFrontColor(int widthIdx)
		{
			return splitRGB(this.rings.get(0)[widthIdx]);
		}

		public int[] getBackColor(int widthIdx)
		{
			return splitRGB(this.rings.get(this.rings.size()-1)[widthIdx]);
		}
	}
}