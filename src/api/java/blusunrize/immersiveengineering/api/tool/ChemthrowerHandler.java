/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.utils.SetRestrictedField;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;

public class ChemthrowerHandler
{
	public static final SetRestrictedField<BiConsumer<Level, BlockPos>> SOLIDIFY_CONCRETE_POWDER = SetRestrictedField.common();
	public static List<Pair<Tag<Fluid>, ChemthrowerEffect>> effectList = new ArrayList<>();
	public static HashSet<Tag<Fluid>> flammableList = new HashSet<>();

	/**
	 * registers a special effect to a fluid based on tags.
	 * Fluids without an effect simply do damage based on temperature
	 */
	public static void registerEffect(Tag<Fluid> fluidTag, ChemthrowerEffect effect)
	{
		effectList.add(Pair.of(fluidTag, effect));
	}

	public static ChemthrowerEffect getEffect(Fluid fluid)
	{
		if(fluid!=null)
			for(Map.Entry<Tag<Fluid>, ChemthrowerEffect> entry : effectList)
				if(entry.getKey().contains(fluid))
					return entry.getValue();
		return null;
	}

	/**
	 * registers a fluid based on its registry name, to allow the chemical thrower to ignite it upon dispersal
	 */
	public static void registerFlammable(Tag<Fluid> fluidTag)
	{
		flammableList.add(fluidTag);
	}

	public static boolean isFlammable(Fluid fluid)
	{
		if(fluid!=null)
			for(Tag<Fluid> predicate : flammableList)
				if(predicate.contains(fluid))
					return true;
		return false;
	}

	public abstract static class ChemthrowerEffect
	{
		public void applyToEntity(LivingEntity target, @Nullable Player shooter, ItemStack thrower, FluidStack fluid)
		{
			applyToEntity(target, shooter, thrower, fluid.getFluid());
		}

		public abstract void applyToEntity(LivingEntity target, @Nullable Player shooter, ItemStack thrower, Fluid fluid);

		public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, ItemStack thrower, FluidStack fluid)
		{
			applyToBlock(world, mop, shooter, thrower, fluid.getFluid());
		}

		public abstract void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, ItemStack thrower, Fluid fluid);
	}

	public static class ChemthrowerEffect_Damage extends ChemthrowerEffect
	{
		DamageSource source;
		float damage;

		public ChemthrowerEffect_Damage(DamageSource source, float damage)
		{
			this.source = source;
			this.damage = damage;
		}

		@Override
		public void applyToEntity(LivingEntity target, @Nullable Player shooter, ItemStack thrower, Fluid fluid)
		{
			if(this.source!=null)
			{
				if(target.hurt(source, damage))
				{
					target.invulnerableTime = (int)(target.invulnerableTime*.75);
					if(source.isFire()&&!target.fireImmune())
						target.setSecondsOnFire(fluid.getAttributes().isGaseous()?2: 5);
				}
			}
		}

		@Override
		public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, ItemStack thrower, Fluid fluid)
		{
		}
	}

	public static class ChemthrowerEffect_Potion extends ChemthrowerEffect_Damage
	{
		MobEffectInstance[] potionEffects;
		float[] effectChances;

		public ChemthrowerEffect_Potion(DamageSource source, float damage, MobEffectInstance... effects)
		{
			super(source, damage);
			this.potionEffects = effects;
			this.effectChances = new float[potionEffects.length];
			Arrays.fill(this.effectChances, 1);
		}

		public ChemthrowerEffect_Potion(DamageSource source, float damage, MobEffect potion, int duration, int amplifier)
		{
			this(source, damage, new MobEffectInstance(potion, duration, amplifier));
		}

		public ChemthrowerEffect_Potion setEffectChance(int effectIndex, float chance)
		{
			if(effectIndex >= 0&&effectIndex < this.effectChances.length)
				this.effectChances[effectIndex] = chance;
			return this;
		}

		@Override
		public void applyToEntity(LivingEntity target, @Nullable Player shooter, ItemStack thrower, Fluid fluid)
		{
			super.applyToEntity(target, shooter, thrower, fluid);
			if(this.potionEffects!=null&&this.potionEffects.length > 0)
				for(int iEffect = 0; iEffect < this.potionEffects.length; iEffect++)
					if(target.getRandom().nextFloat() < this.effectChances[iEffect])
					{
						MobEffectInstance e = this.potionEffects[iEffect];
						MobEffectInstance newEffect = new MobEffectInstance(e.getEffect(), e.getDuration(), e.getAmplifier());
						newEffect.setCurativeItems(new ArrayList<>(e.getCurativeItems()));
						target.addEffect(newEffect);
					}
		}
	}

	public static class ChemthrowerEffect_Extinguish extends ChemthrowerEffect
	{
		private static DamageSource getPlayerDrownDamage(Player player)
		{
			if(player==null)
				return DamageSource.DROWN;
			return new EntityDamageSource(DamageSource.DROWN.getMsgId(), player).bypassArmor();
		}

		@Override
		public void applyToEntity(LivingEntity target, @Nullable Player shooter, ItemStack thrower, Fluid fluid)
		{
			if(target.isOnFire())
				target.clearFire();

			if(target instanceof Blaze||target instanceof EnderMan)
				if(target.hurt(getPlayerDrownDamage(shooter), 3))
					target.invulnerableTime = (int)(target.invulnerableTime*.75);
		}

		@Override
		public void applyToBlock(Level world, HitResult mop, @Nullable Player shooter, ItemStack thrower, Fluid fluid)
		{
			if(!(mop instanceof BlockHitResult))
				return;
			BlockHitResult rtr = (BlockHitResult)mop;
			// Interactions with block at target position
			BlockPos pos = rtr.getBlockPos();
			SOLIDIFY_CONCRETE_POWDER.getValue().accept(world, pos);

			// Interactions with block at offset position
			pos = rtr.getBlockPos().relative(rtr.getDirection());
			Block b = world.getBlockState(pos).getBlock();
			if(b instanceof FireBlock)
				world.removeBlock(pos, false);
		}
	}

	public static class ChemthrowerEffect_RandomTeleport extends ChemthrowerEffect_Damage
	{
		float chance;

		public ChemthrowerEffect_RandomTeleport(DamageSource source, float damage, float chance)
		{
			super(source, damage);
			this.chance = chance;
		}

		@Override
		public void applyToEntity(LivingEntity target, Player shooter, ItemStack thrower, Fluid fluid)
		{
			super.applyToEntity(target, shooter, thrower, fluid);
			if(ApiUtils.RANDOM.nextFloat() < chance)
			{
				double x = target.getX()-8+ApiUtils.RANDOM.nextInt(17);
				double y = target.getY()+ApiUtils.RANDOM.nextInt(8);
				double z = target.getZ()-8+ApiUtils.RANDOM.nextInt(17);
				if(!target.level.getBlockState(new BlockPos(x, y, z)).getMaterial().isSolid())
				{
					EnderTeleportEvent event = new EnderTeleportEvent(target, x, y, z, 0);
					if(MinecraftForge.EVENT_BUS.post(event))
						return;
					target.teleportTo(event.getTargetX(), event.getTargetY(), event.getTargetZ());
					target.level.playLocalSound(target.getX(), target.getY(), target.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F, false);
				}
			}
		}
	}
}
