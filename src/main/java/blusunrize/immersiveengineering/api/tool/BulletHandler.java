package blusunrize.immersiveengineering.api.tool;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * @author BluSunrize - 12.08.2016
 *         <p>
 *         A registry for custom bullet types
 */
public class BulletHandler
{
	public static ItemStack emptyCasing;
	public static ItemStack emptyShell;
	public static ItemStack basicCartridge;
	/**
	 * A list of all cartridges that shoot a homing bullet. Used to add Wolfpack Cartridges
	 */
	public static List<String> homingCartridges = new ArrayList<String>();

	public static HashMap<String, IBullet> registry = new LinkedHashMap<String, IBullet>();

	public static void registerBullet(String name, IBullet bullet)
	{
		registry.put(name, bullet);
	}
	public static IBullet getBullet(String name)
	{
		return registry.get(name);
	}

	public static ItemStack getBulletStack(String key)
	{
		ItemStack stack = basicCartridge.copy();
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setString("bullet", key);
		return stack;
	}

	public interface IBullet
	{
		/**
		 * @return whether this cartridge should appear as an item and should be fired from revolver. Return false if this is a bullet hte player can't get access to
		 */
		default boolean isProperCartridge()
		{
			return true;
		}

		default String getUnlocalizedName(ItemStack cartridge, String baseName)
		{
			return baseName;
		}

		default void addTooltip(ItemStack stack, EntityPlayer player, List<String> list, boolean advanced)
		{
		}

		default int getProjectileCount(EntityPlayer shooter, ItemStack cartridge)
		{
			return 1;
		}

		/**
		 * @param shooter
		 * @param cartridge
		 * @param protetile
		 * @param charged   whether the revolver has the electron tube upgrade
		 * @return the given or a custom entity
		 */
		default Entity getProjectile(EntityPlayer shooter, ItemStack cartridge, Entity protetile, boolean charged)
		{
			return protetile;
		}

		/**
		 * called when the bullet hits a target
		 */
		void onHitTarget(World world, RayTraceResult target, EntityLivingBase shooter, Entity projectile, boolean headshot);

		/**
		 * @return the casing left when fired. Can return the static ItemStacks in BulletHandler
		 */
		ItemStack getCasing(ItemStack stack);

		/**
		 * @return the textures (layers) for the item
		 */
		ResourceLocation[] getTextures();

		/**
		 * @return the colour applied to the given layer
		 */
		int getColour(ItemStack stack, int layer);
	}

	public static class DamagingBullet implements IBullet
	{
		/**
		 * The entities in the array are Projectile, Shooter, Target
		 */
		final Function<Entity[], DamageSource> damageSourceGetter;
		final float damage;
		boolean resetHurt = false;
		boolean setFire = false;
		ItemStack casing;
		ResourceLocation[] textures;

		public DamagingBullet(Function<Entity[], DamageSource> damageSourceGetter, float damage, ItemStack casing, ResourceLocation... textures)
		{
			this(damageSourceGetter, damage, false, false, casing, textures);
		}

		public DamagingBullet(Function<Entity[], DamageSource> damageSourceGetter, float damage, boolean resetHurt, boolean setFire, ItemStack casing, ResourceLocation... textures)
		{
			this.damageSourceGetter = damageSourceGetter;
			this.damage = damage;
			this.resetHurt = resetHurt;
			this.setFire = setFire;
			this.casing = casing;
			this.textures = textures;
		}

		float getDamage(boolean headshot)
		{
			return this.damage * (headshot ? 1.5f : 1f);
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, EntityLivingBase shooter, Entity projectile, boolean headshot)
		{
			if(!world.isRemote && target.entityHit != null && damageSourceGetter != null)
				if(target.entityHit.attackEntityFrom(damageSourceGetter.apply(new Entity[]{projectile, shooter, target.entityHit}), getDamage(headshot)))
				{
					if(resetHurt)
						target.entityHit.hurtResistantTime = 0;
					if(setFire)
						target.entityHit.setFire(3);
				}
		}

		@Override
		public ItemStack getCasing(ItemStack stack)
		{
			return casing;
		}

		@Override
		public ResourceLocation[] getTextures()
		{
			return textures;
		}

		@Override
		public int getColour(ItemStack stack, int layer)
		{
			return 0xffffffff;
		}
	}
}
