/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.api.tool;

import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.tags.Tag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class ChemthrowerHandler
{
	public static HashMap<Tag<Fluid>, ChemthrowerEffect> effectMap = new HashMap<>();
	public static HashSet<Tag<Fluid>> flammableList = new HashSet<>();
	public static HashSet<ResourceLocation> gasList = new HashSet<>();

	/**
	 * registers a special effect to a fluid based on tags.
	 * Fluids without an effect simply do damage based on temperature
	 */
	public static void registerEffect(Tag<Fluid> fluidTag, ChemthrowerEffect effect)
	{
		effectMap.put(fluidTag, effect);
	}

	public static ChemthrowerEffect getEffect(Fluid fluid)
	{
		if(fluid!=null)
			for(Map.Entry<Tag<Fluid>, ChemthrowerEffect> entry : effectMap.entrySet())
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

	/**
	 * registers a fluid to be dispersed like a gas. This is only necessary if the fluid itself isn't designated as a gas
	 */
	public static void registerGas(Fluid fluid)
	{
		if(fluid!=null)
			registerGas(fluid.getRegistryName());
	}

	/**
	 * registers a fluid to be dispersed like a gas. This is only necessary if the fluid itself isn't designated as a gas
	 */
	public static void registerGas(ResourceLocation fluidName)
	{
		gasList.add(fluidName);
	}

	public static boolean isGas(Fluid fluid)
	{
		if(fluid!=null)
			return gasList.contains(fluid.getRegistryName());
		return false;
	}

	public static boolean isGas(ResourceLocation fluidName)
	{
		return gasList.contains(fluidName);
	}

	public abstract static class ChemthrowerEffect
	{
		public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
		{
			applyToEntity(target, shooter, thrower, fluid.getFluid());
		}

		public abstract void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid);

		public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, FluidStack fluid)
		{
			applyToBlock(world, mop, shooter, thrower, fluid.getFluid());
		}

		public abstract void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid);
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
		public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
		{
			if(this.source!=null)
			{
				if(target.attackEntityFrom(source, damage))
				{
					target.hurtResistantTime = (int)(target.hurtResistantTime*.75);
					if(source.isFireDamage()&&!target.isImmuneToFire())
						target.setFire(fluid.getAttributes().isGaseous()?2: 5);
				}
			}
		}

		@Override
		public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
		{
		}
	}

	public static class ChemthrowerEffect_Potion extends ChemthrowerEffect_Damage
	{
		EffectInstance[] potionEffects;
		float[] effectChances;

		public ChemthrowerEffect_Potion(DamageSource source, float damage, EffectInstance... effects)
		{
			super(source, damage);
			this.potionEffects = effects;
			this.effectChances = new float[potionEffects.length];
			for(int i = 0; i < this.effectChances.length; i++)
				this.effectChances[i] = 1;
		}

		public ChemthrowerEffect_Potion(DamageSource source, float damage, Effect potion, int duration, int amplifier)
		{
			this(source, damage, new EffectInstance(potion, duration, amplifier));
		}

		public ChemthrowerEffect_Potion setEffectChance(int effectIndex, float chance)
		{
			if(effectIndex >= 0&&effectIndex < this.effectChances.length)
				this.effectChances[effectIndex] = chance;
			return this;
		}

		@Override
		public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
		{
			super.applyToEntity(target, shooter, thrower, fluid);
			if(this.potionEffects!=null&&this.potionEffects.length > 0)
				for(int iEffect = 0; iEffect < this.potionEffects.length; iEffect++)
					if(target.getRNG().nextFloat() < this.effectChances[iEffect])
					{
						EffectInstance e = this.potionEffects[iEffect];
						EffectInstance newEffect = new EffectInstance(e.getPotion(), e.getDuration(), e.getAmplifier());
						newEffect.setCurativeItems(new ArrayList(e.getCurativeItems()));
						target.addPotionEffect(newEffect);
					}
		}
	}

	public static class ChemthrowerEffect_Extinguish extends ChemthrowerEffect
	{
		private static DamageSource getPlayerDrownDamage(PlayerEntity player)
		{
			if(player==null)
				return DamageSource.DROWN;
			return new EntityDamageSource(DamageSource.DROWN.getDamageType(), player).setDamageBypassesArmor();
		}

		@Override
		public void applyToEntity(LivingEntity target, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
		{
			if(target.isBurning())
				target.extinguish();

			if(target instanceof BlazeEntity||target instanceof EndermanEntity)
				if(target.attackEntityFrom(getPlayerDrownDamage(shooter), 3))
					target.hurtResistantTime = (int)(target.hurtResistantTime*.75);
		}

		@Override
		public void applyToBlock(World world, RayTraceResult mop, @Nullable PlayerEntity shooter, ItemStack thrower, Fluid fluid)
		{
			if(!(mop instanceof BlockRayTraceResult))
				return;
			BlockRayTraceResult rtr = (BlockRayTraceResult)mop;
			// Interactions with block at target position
			BlockPos pos = rtr.getPos();
			Block b = world.getBlockState(pos).getBlock();
			if(b instanceof ConcretePowderBlock)
				world.setBlockState(pos, ((ConcretePowderBlock)b).solidifiedState, 3);

			// Interactions with block at offset position
			pos = rtr.getPos().offset(rtr.getFace());
			b = world.getBlockState(pos).getBlock();
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
		public void applyToEntity(LivingEntity target, PlayerEntity shooter, ItemStack thrower, Fluid fluid)
		{
			super.applyToEntity(target, shooter, thrower, fluid);
			if(Utils.RAND.nextFloat() < chance)
			{
				double x = target.posX-8+Utils.RAND.nextInt(17);
				double y = target.posY+Utils.RAND.nextInt(8);
				double z = target.posZ-8+Utils.RAND.nextInt(17);
				if(!target.world.getBlockState(new BlockPos(x, y, z)).getMaterial().isSolid())
				{
					EnderTeleportEvent event = new EnderTeleportEvent(target, x, y, z, 0);
					if(MinecraftForge.EVENT_BUS.post(event))
						return;
					target.setPositionAndUpdate(event.getTargetX(), event.getTargetY(), event.getTargetZ());
					target.world.playSound(target.posX, target.posY, target.posZ, SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
				}
			}
		}
	}
}
