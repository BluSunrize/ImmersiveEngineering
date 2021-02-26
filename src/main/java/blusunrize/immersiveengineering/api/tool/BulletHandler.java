/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.common.items.IEItems.Weapons;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;

/**
 * @author BluSunrize - 12.08.2016
 * <p>
 * A registry for custom bullet types
 */

public class BulletHandler
{
	public static ItemStack emptyCasing = ItemStack.EMPTY;
	public static ItemStack emptyShell = ItemStack.EMPTY;

	private final static BiMap<ResourceLocation, IBullet> REGISTRY = HashBiMap.create();

	public static void registerBullet(ResourceLocation name, IBullet bullet)
	{
		Preconditions.checkState(!REGISTRY.containsKey(name), name+" is already registered");
		Preconditions.checkState(!REGISTRY.containsValue(bullet));
		REGISTRY.put(name, bullet);
	}

	public static IBullet getBullet(ResourceLocation name)
	{
		return REGISTRY.get(name);
	}

	public static ResourceLocation findRegistryName(IBullet bullet)
	{
		if(bullet!=null)
			return REGISTRY.inverse().get(bullet);
		return null;
	}

	public static ItemStack getBulletStack(ResourceLocation key)
	{
		return new ItemStack(getBulletItem(key));
	}

	public static Item getBulletItem(ResourceLocation key)
	{
		return Weapons.bullets.get(getBullet(key));
	}

	public static Collection<ResourceLocation> getAllKeys()
	{
		return REGISTRY.keySet();
	}

	public static Collection<IBullet> getAllValues()
	{
		return REGISTRY.values();
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

		default void addTooltip(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag)
		{
		}

		default int getProjectileCount(@Nullable PlayerEntity shooter)
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
		default Entity getProjectile(@Nullable PlayerEntity shooter, ItemStack cartridge, Entity projectile, boolean charged)
		{
			return projectile;
		}

		/**
		 * called when the bullet hits a target
		 */
		void onHitTarget(World world, RayTraceResult target, @Nullable UUID shooter, Entity projectile, boolean headshot);

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
		final DamageSourceProvider damageSourceGetter;
		final DoubleSupplier damage;
		boolean resetHurt = false;
		boolean setFire = false;
		Supplier<ItemStack> casing;
		ResourceLocation[] textures;

		public DamagingBullet(DamageSourceProvider damageSourceGetter, float damage, Supplier<ItemStack> casing, ResourceLocation... textures)
		{
			this(damageSourceGetter, damage, false, false, casing, textures);
		}

		public DamagingBullet(DamageSourceProvider damageSourceGetter, DoubleSupplier damage, Supplier<ItemStack> casing, ResourceLocation... textures)
		{
			this(damageSourceGetter, damage, false, false, casing, textures);
		}

		public DamagingBullet(DamageSourceProvider damageSourceGetter, float damage, boolean resetHurt, boolean setFire, Supplier<ItemStack> casing, ResourceLocation... textures)
		{
			this(damageSourceGetter, () -> damage, resetHurt, setFire, casing, textures);
		}

		public DamagingBullet(DamageSourceProvider damageSourceGetter, DoubleSupplier damage, boolean resetHurt, boolean setFire, Supplier<ItemStack> casing, ResourceLocation... textures)
		{
			this.damageSourceGetter = damageSourceGetter;
			this.damage = damage;
			this.resetHurt = resetHurt;
			this.setFire = setFire;
			this.casing = casing;
			this.textures = textures;
		}

		protected float getDamage(Entity hitEntity, boolean headshot)
		{
			return (float)(this.damage.getAsDouble()*(headshot?1.5f: 1f));
		}

		@Override
		public void onHitTarget(World world, RayTraceResult rtr, @Nullable UUID shooterUUID, Entity projectile, boolean headshot)
		{
			if(!(rtr instanceof EntityRayTraceResult))
				return;
			EntityRayTraceResult target = (EntityRayTraceResult)rtr;
			Entity hitEntity = target.getEntity();
			if(!world.isRemote&&hitEntity!=null&&damageSourceGetter!=null)
			{
				Entity shooter = null;
				if(shooterUUID!=null)
					shooter = world.getPlayerByUuid(shooterUUID);
				if(hitEntity.attackEntityFrom(damageSourceGetter.getSource(projectile, shooter, hitEntity), getDamage(hitEntity, headshot)))
				{
					if(resetHurt)
						hitEntity.hurtResistantTime = 0;
					if(setFire)
						hitEntity.setFire(3);
				}
			}
		}

		@Override
		public ItemStack getCasing(ItemStack stack)
		{
			return casing.get();
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

		public interface DamageSourceProvider
		{
			DamageSource getSource(Entity projectile, Entity shooter, Entity hit);
		}
	}
}
