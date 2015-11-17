package blusunrize.immersiveengineering.api.tool;

import java.util.HashMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fluids.Fluid;

public class ChemthrowerHandler
{
	public static HashMap<Fluid, ChemthrowerEffect> effectMap = new HashMap<Fluid, ChemthrowerEffect>();

	public static void registerEffect(Fluid fluid, ChemthrowerEffect effect)
	{
		effectMap.put(fluid, effect);
	}
	public static ChemthrowerEffect getEffect(Fluid fluid)
	{
		return effectMap.get(fluid);
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
				if(source.isFireDamage() && !target.isImmuneToFire())
					target.setFire(fluid.isGaseous()?2:5);
			}
		}
	}
	public static class ChemthrowerEffect_Potion extends ChemthrowerEffect_Damage
	{
		Potion potion;
		int duration;
		int amplifier;
		public ChemthrowerEffect_Potion(DamageSource source, float damage, Potion potion, int duration, int amplifier)
		{
			super(source, damage);
			this.potion = potion;
			this.duration = duration;
			this.amplifier = amplifier;
		}
		public void apply(EntityLivingBase target, EntityPlayer shooter, ItemStack thrower, Fluid fluid)
		{
			super.apply(target, shooter, thrower, fluid);
			if(this.potion!=null)
				target.addPotionEffect(new PotionEffect(this.potion.id,this.duration,this.amplifier));
		}
	}
}
