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
import net.minecraft.block.BlockFire;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.EnderTeleportEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class ChemthrowerHandler
{
	public static HashMap<String, ChemthrowerEffect> effectMap = new HashMap<String, ChemthrowerEffect>();
	public static HashSet<String> flammableList = new HashSet<String>();
	public static HashSet<String> gasList = new HashSet<String>();

	/**
	 * registers a special effect to a fluid. Fluids without an effect simply do damage based on temperature
	 */
	public static void registerEffect(Fluid fluid, ChemthrowerEffect effect)
	{
		if(fluid!=null)
			registerEffect(fluid.getName(), effect);
	}

	/**
	 * registers a special effect to a fluid. Fluids without an effect simply do damage based on temperature
	 */
	public static void registerEffect(String fluidName, ChemthrowerEffect effect)
	{
		effectMap.put(fluidName, effect);
	}

	public static ChemthrowerEffect getEffect(Fluid fluid)
	{
		if(fluid!=null)
			return getEffect(fluid.getName());
		return null;
	}

	public static ChemthrowerEffect getEffect(String fluidName)
	{
		return effectMap.get(fluidName);
	}

	/**
	 * registers a fluid to allow the chemical thrower to ignite it upon dispersal
	 */
	public static void registerFlammable(Fluid fluid)
	{
		if(fluid!=null)
			registerFlammable(fluid.getName());
	}

	/**
	 * registers a fluid to allow the chemical thrower to ignite it upon dispersal
	 */
	public static void registerFlammable(String fluidName)
	{
		flammableList.add(fluidName);
	}

	public static boolean isFlammable(Fluid fluid)
	{
		if(fluid!=null)
			return flammableList.contains(fluid.getName());
		return false;
	}

	public static boolean isFlammable(String fluidName)
	{
		return flammableList.contains(fluidName);
	}

	/**
	 * registers a fluid to be dispersed like a gas. This is only necessary if the fluid itself isn't designated as a gas
	 */
	public static void registerGas(Fluid fluid)
	{
		if(fluid!=null)
			registerGas(fluid.getName());
	}

	/**
	 * registers a fluid to be dispersed like a gas. This is only necessary if the fluid itself isn't designated as a gas
	 */
	public static void registerGas(String fluidName)
	{
		gasList.add(fluidName);
	}

	public static boolean isGas(Fluid fluid)
	{
		if(fluid!=null)
			return gasList.contains(fluid.getName());
		return false;
	}

	public static boolean isGas(String fluidName)
	{
		return gasList.contains(fluidName);
	}

	public abstract static class ChemthrowerEffect
	{
		public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, FluidStack fluid)
		{
			applyToEntity(target, shooter, thrower, fluid.getFluid());
		}

		public abstract void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid);

		public void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, FluidStack fluid)
		{
			applyToBlock(world, mop, shooter, thrower, fluid.getFluid());
		}

		public abstract void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid);
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
		public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			if(this.source!=null)
			{
				if(target.attackEntityFrom(source, damage))
				{
					target.hurtResistantTime = (int)(target.hurtResistantTime*.75);
					if(source.isFireDamage()&&!target.isImmuneToFire())
						target.setFire(fluid.isGaseous()?2: 5);
				}
			}
		}

		@Override
		public void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
		}
	}

	public static class ChemthrowerEffect_Potion extends ChemthrowerEffect_Damage
	{
		PotionEffect[] potionEffects;
		float[] effectChances;

		public ChemthrowerEffect_Potion(DamageSource source, float damage, PotionEffect... effects)
		{
			super(source, damage);
			this.potionEffects = effects;
			this.effectChances = new float[potionEffects.length];
			for(int i = 0; i < this.effectChances.length; i++)
				this.effectChances[i] = 1;
		}

		public ChemthrowerEffect_Potion(DamageSource source, float damage, Potion potion, int duration, int amplifier)
		{
			this(source, damage, new PotionEffect(potion, duration, amplifier));
		}

		public ChemthrowerEffect_Potion setEffectChance(int effectIndex, float chance)
		{
			if(effectIndex >= 0&&effectIndex < this.effectChances.length)
				this.effectChances[effectIndex] = chance;
			return this;
		}

		@Override
		public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			super.applyToEntity(target, shooter, thrower, fluid);
			if(this.potionEffects!=null&&this.potionEffects.length > 0)
				for(int iEffect = 0; iEffect < this.potionEffects.length; iEffect++)
					if(target.getRNG().nextFloat() < this.effectChances[iEffect])
					{
						PotionEffect e = this.potionEffects[iEffect];
						PotionEffect newEffect = new PotionEffect(e.getPotion(), e.getDuration(), e.getAmplifier());
						newEffect.setCurativeItems(new ArrayList(e.getCurativeItems()));
						target.addPotionEffect(newEffect);
					}
		}
	}

	public static class ChemthrowerEffect_Extinguish extends ChemthrowerEffect
	{
		private static DamageSource getPlayerDrownDamage(EntityPlayer player)
		{
			if(player==null)
				return DamageSource.DROWN;
			return new EntityDamageSource(DamageSource.DROWN.getDamageType(), player).setDamageBypassesArmor();
		}

		@Override
		public void applyToEntity(EntityLivingBase target, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			if(target.isBurning())
				target.extinguish();

			if(target instanceof EntityBlaze||target instanceof EntityEnderman)
				if(target.attackEntityFrom(getPlayerDrownDamage(shooter), 3))
					target.hurtResistantTime = (int)(target.hurtResistantTime*.75);
		}

		@Override
		public void applyToBlock(World world, RayTraceResult mop, @Nullable EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			Block b = world.getBlockState(mop.getBlockPos().offset(mop.sideHit)).getBlock();
			if(b instanceof BlockFire)
				world.setBlockToAir(mop.getBlockPos().offset(mop.sideHit));
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
		public void applyToEntity(EntityLivingBase target, EntityPlayer shooter, ItemStack thrower, Fluid fluid)
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
					target.world.playSound(target.posX, target.posY, target.posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
				}
			}
		}
	}
}
