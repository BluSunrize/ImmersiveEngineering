package blusunrize.immersiveengineering.api.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fluids.Fluid;

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
			registerEffect(fluid.getName(),effect);
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
		ChemthrowerEffect eff = effectMap.get(fluidName);
		System.out.println("get effect for "+fluidName+" "+eff);
		return eff;
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

	public static abstract class ChemthrowerEffect
	{
		public abstract void apply(EntityLivingBase target, EntityPlayer shooter, ItemStack thrower, Fluid fluid);
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
		public void apply(EntityLivingBase target, EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			if(this.source!=null)
			{
				target.attackEntityFrom(source, damage);
//				target.hurtResistantTime = 12;
				if(source.isFireDamage() && !target.isImmuneToFire())
					target.setFire(fluid.isGaseous()?2:5);
			}
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
			for(int i=0; i<this.effectChances.length; i++)
				this.effectChances[i] = 1;
		}
		public ChemthrowerEffect_Potion(DamageSource source, float damage, Potion potion, int duration, int amplifier)
		{
			this(source, damage, new PotionEffect(potion.id,duration,amplifier));
		}
		public ChemthrowerEffect_Potion setEffectChance(int effectIndex, float chance)
		{
			if(effectIndex>=0 && effectIndex<this.effectChances.length)
				this.effectChances[effectIndex] = chance;
			return this;
		}

		@Override
		public void apply(EntityLivingBase target, EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			super.apply(target, shooter, thrower, fluid);
			if(this.potionEffects!=null && this.potionEffects.length>0)
				for(int iEffect=0; iEffect<this.potionEffects.length; iEffect++)
					if(target.getRNG().nextFloat() < this.effectChances[iEffect])
					{
						PotionEffect e = this.potionEffects[iEffect];
						PotionEffect newEffect = new PotionEffect(e.getPotionID(),e.getDuration(),e.getAmplifier());
						newEffect.setCurativeItems(new ArrayList(e.getCurativeItems()));
						target.addPotionEffect(newEffect);
					}
		}
	}
}
