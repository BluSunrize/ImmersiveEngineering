/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.ApiUtils;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

public class RailgunHandler
{
	public static List<Pair<Ingredient, RailgunProjectileProperties>> projectilePropertyMap = new ArrayList<>();

	public static RailgunProjectileProperties registerProjectileProperties(Ingredient stack, double damage, double gravity)
	{
		RailgunProjectileProperties properties = new RailgunProjectileProperties(damage, gravity);
		projectilePropertyMap.add(Pair.of(stack, properties));
		return properties;
	}

	public static RailgunProjectileProperties registerProjectileProperties(ItemStack stack, double damage, double gravity)
	{
		return registerProjectileProperties(Ingredient.fromStacks(stack), damage, gravity);
	}

	public static RailgunProjectileProperties getProjectileProperties(ItemStack stack)
	{
		for(Pair<Ingredient, RailgunProjectileProperties> pair : projectilePropertyMap)
			if(pair.getLeft().test(stack))
				return pair.getRight();
		return null;
	}

	public static class RailgunProjectileProperties
	{
		public double damage;
		public double gravity;
		public int[][] colourMap = {{0x686868, 0xa4a4a4, 0xa4a4a4, 0xa4a4a4, 0x686868}};

		public RailgunProjectileProperties(double damage, double gravity)
		{
			this.damage = damage;
			this.gravity = gravity;
		}

		public RailgunProjectileProperties setColourMap(int[][] map)
		{
			this.colourMap = map;
			return this;
		}

		/**
		 * @return true to cancel normal damage application
		 */
		public boolean overrideHitEntity(Entity entityHit, Entity shooter)
		{
			return false;
		}
	}
}