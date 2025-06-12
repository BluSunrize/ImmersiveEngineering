/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.utils.Color4;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Unit;
import malte0811.dualcodecs.DualCodec;
import malte0811.dualcodecs.DualCodecs;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
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
	public static final SetRestrictedField<Function<IBullet<?>, Item>> GET_BULLET_ITEM = SetRestrictedField.common();
	public static ItemLike emptyCasing = Items.AIR;
	public static ItemLike emptyShell = Items.AIR;

	private final static BiMap<ResourceLocation, IBullet<?>> REGISTRY = HashBiMap.create();

	public static void registerBullet(ResourceLocation name, IBullet<?> bullet)
	{
		Preconditions.checkState(!REGISTRY.containsKey(name), name+" is already registered");
		Preconditions.checkState(!REGISTRY.containsValue(bullet));
		REGISTRY.put(name, bullet);
	}

	public static IBullet<?> getBullet(ResourceLocation name)
	{
		return REGISTRY.get(name);
	}

	public static ResourceLocation findRegistryName(IBullet<?> bullet)
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
		return GET_BULLET_ITEM.get().apply(getBullet(key));
	}

	public static Collection<ResourceLocation> getAllKeys()
	{
		return REGISTRY.keySet();
	}

	public interface IBullet<StackData>
	{
		CodecsAndDefault<StackData> getCodec();

		/**
		 * @return whether this cartridge should appear as an item and should be fired from revolver. Return false if this is a bullet the player can't get access to
		 */
		default boolean isProperCartridge()
		{
			return true;
		}

		default String getTranslationKey(StackData data, String baseName)
		{
			return baseName;
		}

		default void addTooltip(StackData data, @Nullable TooltipContext world, List<Component> list, TooltipFlag flag)
		{
		}

		default int getProjectileCount(@Nullable Player shooter)
		{
			return 1;
		}

		/**
		 * @param shooter
		 * @param data
		 * @param projectile
		 * @param charged    whether the revolver has the electron tube upgrade
		 * @return the given or a custom entity
		 */
		default Entity getProjectile(@Nullable Player shooter, StackData data, Entity projectile, boolean charged)
		{
			// TODO why is charged not passed through globally?
			return projectile;
		}

		/**
		 * called when the bullet hits a target
		 */
		void onHitTarget(
				Level world, HitResult target, @Nullable UUID shooter, Entity projectile, boolean headshot,
				StackData bulletData);

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
		default Color4 getColour(StackData data, int layer)
		{
			return Color4.WHITE;
		}

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

	public static class DamagingBullet<StackData> implements IBullet<StackData>
	{
		private final CodecsAndDefault<StackData> codec;
		private final DamageSourceProvider damageSourceGetter;
		private final DoubleSupplier damage;
		private final boolean resetHurt;
		private final boolean setFire;
		private final Supplier<ItemStack> casing;
		private final ResourceLocation[] textures;

		public DamagingBullet(CodecsAndDefault<StackData> codec, DamageSourceProvider damageSourceGetter, float damage, Supplier<ItemStack> casing, ResourceLocation... textures)
		{
			this(codec, damageSourceGetter, damage, false, false, casing, textures);
		}

		public DamagingBullet(CodecsAndDefault<StackData> codec, DamageSourceProvider damageSourceGetter, DoubleSupplier damage, Supplier<ItemStack> casing, ResourceLocation... textures)
		{
			this(codec, damageSourceGetter, damage, false, false, casing, textures);
		}

		public DamagingBullet(CodecsAndDefault<StackData> codec, DamageSourceProvider damageSourceGetter, float damage, boolean resetHurt, boolean setFire, Supplier<ItemStack> casing, ResourceLocation... textures)
		{
			this(codec, damageSourceGetter, () -> damage, resetHurt, setFire, casing, textures);
		}

		public DamagingBullet(CodecsAndDefault<StackData> codec, DamageSourceProvider damageSourceGetter, DoubleSupplier damage, boolean resetHurt, boolean setFire, Supplier<ItemStack> casing, ResourceLocation... textures)
		{
			this.codec = codec;
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
		public void onHitTarget(Level world, HitResult rtr, @Nullable UUID shooterUUID, Entity projectile, boolean headshot, StackData bulletData)
		{
			if(!(rtr instanceof EntityHitResult))
				return;
			EntityHitResult target = (EntityHitResult)rtr;
			Entity hitEntity = target.getEntity();
			if(!world.isClientSide&&hitEntity!=null&&damageSourceGetter!=null)
			{
				Entity shooter = null;
				if(shooterUUID!=null&&world instanceof ServerLevel serverLevel)
					shooter = serverLevel.getEntity(shooterUUID);
				if(shooter == null && projectile instanceof Projectile p)
					shooter = p.getOwner();
				if(hitEntity.hurt(damageSourceGetter.getSource(projectile, shooter, hitEntity), getDamage(hitEntity, headshot)))
				{
					if(resetHurt)
						hitEntity.invulnerableTime = 0;
					if(setFire)
						hitEntity.igniteForSeconds(3);
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
		public boolean isValidForTurret()
		{
			return true;
		}

		@Override
		public CodecsAndDefault<StackData> getCodec()
		{
			return codec;
		}

		public interface DamageSourceProvider
		{
			DamageSource getSource(Entity projectile, Entity shooter, Entity hit);
		}
	}

	public record CodecsAndDefault<T>(DualCodec<? super RegistryFriendlyByteBuf, T> codecs, T defaultValue,
									  DataComponentType<T> vanillaDataComponent)
	{
		public CodecsAndDefault(DualCodec<? super RegistryFriendlyByteBuf, T> codecs, T defaultValue)
		{
			this(codecs, defaultValue, null);
		}

		public static final CodecsAndDefault<Unit> UNIT = new CodecsAndDefault<>(DualCodecs.unit(Unit.INSTANCE), Unit.INSTANCE);
	}
}
