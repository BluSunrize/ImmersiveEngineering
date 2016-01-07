package blusunrize.immersiveengineering.api.tool;

import java.util.HashMap;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.ComparableItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

public class RailgunHandler
{
	public static HashMap<ComparableItemStack, RailgunProjectileProperties> projectilePropertyMap = new HashMap<ComparableItemStack, RailgunProjectileProperties>();

	public static RailgunProjectileProperties registerProjectileProperties(ComparableItemStack stack, double damage, double gravity)
	{
		RailgunProjectileProperties properties = new RailgunProjectileProperties(damage, gravity);
		projectilePropertyMap.put(stack, properties);
		return properties;
	}
	public static RailgunProjectileProperties registerProjectileProperties(ItemStack stack, double damage, double gravity)
	{
		return registerProjectileProperties(ApiUtils.createComparableItemStack(stack), damage, gravity);
	}
	public static RailgunProjectileProperties getProjectileProperties(ItemStack stack)
	{
		return projectilePropertyMap.get(ApiUtils.createComparableItemStack(stack));
	}

	public static class RailgunProjectileProperties
	{
		public double damage;
		public double gravity;
		public int[][] colourMap = {{0x686868,0xa4a4a4,0xa4a4a4,0xa4a4a4,0x686868}};
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