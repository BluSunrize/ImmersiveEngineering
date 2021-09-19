/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

/**
 * @author BluSunrize - 12.08.2016
 * <p>
 * A registry for custom bullet types
 */

public class BulletHandler
{
	public static final SetRestrictedField<Function<IBullet, Item>> GET_BULLET_ITEM = SetRestrictedField.common();
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
		return GET_BULLET_ITEM.getValue().apply(getBullet(key));
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

		default void addTooltip(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag)
		{
		}

		default int getProjectileCount(@Nullable Player shooter)
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
		default Entity getProjectile(@Nullable Player shooter, ItemStack cartridge, Entity projectile, boolean charged)
		{
			return projectile;
		}

		/**
		 * called when the bullet hits a target
		 */
		void onHitTarget(Level world, HitResult target, @Nullable UUID shooter, Entity projectile, boolean headshot);

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
		public void onHitTarget(Level world, HitResult rtr, @Nullable UUID shooterUUID, Entity projectile, boolean headshot)
		{
			if(!(rtr instanceof EntityHitResult))
				return;
			EntityHitResult target = (EntityHitResult)rtr;
			Entity hitEntity = target.getEntity();
			if(!world.isClientSide&&hitEntity!=null&&damageSourceGetter!=null)
			{
				Entity shooter = null;
				if(shooterUUID!=null)
					shooter = world.getPlayerByUUID(shooterUUID);
				if(hitEntity.hurt(damageSourceGetter.getSource(projectile, shooter, hitEntity), getDamage(hitEntity, headshot)))
				{
					if(resetHurt)
						hitEntity.invulnerableTime = 0;
					if(setFire)
						hitEntity.setSecondsOnFire(3);
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
