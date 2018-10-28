/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

/**
 * @author BluSunrize - 12.08.2016
 * <p>
 * A registry for custom bullet types
 */
public class BulletHandler
{
	public static ItemStack emptyCasing = ItemStack.EMPTY;
	public static ItemStack emptyShell = ItemStack.EMPTY;
	public static ItemStack basicCartridge = ItemStack.EMPTY;
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
		name = BulletHandler.handleLeagcyNames(name);
		return registry.get(name);
	}

	public static String findRegistryName(IBullet bullet)
	{
		if(bullet!=null)
			for(Map.Entry<String, IBullet> entry : registry.entrySet())
				if(bullet.equals(entry.getValue()))
					return entry.getKey();
		return null;
	}

	public static ItemStack getBulletStack(String key)
	{
		key = handleLeagcyNames(key);
		ItemStack stack = basicCartridge.copy();
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setString("bullet", key);
		return stack;
	}

	public static String handleLeagcyNames(final String key)
	{
		if(key.equals("armorPiercing"))
			return "armor_piercing";
		if(key.equals("HE"))
			return "he";
		return key;
	}

	public interface IBullet
	{
		/**
		 * @return whether this cartridge should appear as an item and should be fired from revolver. Return false if this is a bullet the player can't get access to
		 */
		default boolean isProperCartridge()
		{
			return true;
		}

		default String getTranslationKey(ItemStack cartridge, String baseName)
		{
			return baseName;
		}

		default void addTooltip(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag)
		{
		}

		default int getProjectileCount(@Nullable EntityPlayer shooter)
		{
			return 1;
		}

		/**
		 * @param shooter
		 * @param cartridge
		 * @param projectile
		 * @param charged    whether the revolver has the electron tube upgrade
		 * @return the given or a custom entity
		 */
		default Entity getProjectile(@Nullable EntityPlayer shooter, ItemStack cartridge, Entity projectile, boolean charged)
		{
			return projectile;
		}

		/**
		 * called when the bullet hits a target
		 */
		void onHitTarget(World world, RayTraceResult target, @Nullable EntityLivingBase shooter, Entity projectile, boolean headshot);

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

		/**
		 * @return whether this cartridge should be allowed to be placed in, and used from a turret.<br>
		 * Bullets that rely on a player using them should return false, since turrets parse null for a player
		 */
		default boolean isValidForTurret()
		{
			return false;
		}

		/**
		 * @return a special sound for when this cartridge is fired. Return null for the default sound.
		 */
		default SoundEvent getSound()
		{
			return null;
		}
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
			return this.damage*(headshot?1.5f: 1f);
		}

		@Override
		public void onHitTarget(World world, RayTraceResult target, @Nullable EntityLivingBase shooter, Entity projectile, boolean headshot)
		{
			if(!world.isRemote&&target.entityHit!=null&&damageSourceGetter!=null)
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

		@Override
		public boolean isValidForTurret()
		{
			return true;
		}
	}
}
